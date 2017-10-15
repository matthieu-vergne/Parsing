package fr.vergne.parsing.layer.standard.impl;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.definition.impl.StandardDefinitionFactory;
import fr.vergne.parsing.definition.impl.StandardDefinitionFactory.DefinitionProxy;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.Layer.ContentListener;
import fr.vergne.parsing.layer.ModifiableComposedLayerTest;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.layer.standard.impl.Loop.BoundException;

@RunWith(JUnitPlatform.class)
public class LoopTest implements ModifiableComposedLayerTest<Loop<Regex>> {

	private static final StandardDefinitionFactory factory = new StandardDefinitionFactory();

	@Override
	public Map<String, Loop<Regex>> instantiateLayers(Collection<String> specialCharacters) {
		StringBuilder builder = new StringBuilder();
		for (String character : specialCharacters) {
			builder.append(character);
		}

		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("(?s:.)"));
		Map<String, Loop<Regex>> map = new HashMap<>();
		map.put(builder.toString(), loop);

		Loop<Regex> loop2 = new Loop<Regex>(factory.defineRegex("[a-zA-Z\n]"));
		map.put("test", loop2);
		map.put("test\ntest", loop2);
		map.put("", loop2);

		return map;
	}

	@Override
	public Collection<Layer> getUsedSubLayers(Loop<Regex> loop) {
		Collection<Layer> sublayers = new LinkedList<>();
		for (int i = 0; i < loop.size(); i++) {
			sublayers.add(loop.get(i));
		}
		return sublayers;
	}

	@Override
	public Collection<Update> getSublayersUpdatesFunctions(Loop<Regex> loop) {
		Collection<Update> updates = new LinkedList<>();

		if (loop.size() > 0) {
			// Addition+removal at the start
			updates.add(new Update() {

				Regex initial = loop.get(0);
				Regex replacement = loop.getItemDefinition().create();

				@Override
				public Layer getInitial() {
					return initial;
				}

				@Override
				public Layer getReplacement() {
					replacement.setContent(initial.getContent());
					return replacement;
				}

				@Override
				public void execute() {
					loop.add(0, replacement);
					loop.remove(1);
				}

				@Override
				public void revert() {
					loop.add(0, initial);
					loop.remove(1);
				}

			});

			// Addition+removal at the end
			updates.add(new Update() {

				int index = loop.size() - 1;
				Regex current = loop.get(index);
				Regex replacement = loop.getItemDefinition().create();

				@Override
				public Layer getInitial() {
					return current;
				}

				@Override
				public Layer getReplacement() {
					replacement.setContent(current.getContent());
					return replacement;
				}

				@Override
				public void execute() {
					loop.add(index + 1, replacement);
					loop.remove(index);
				}

				@Override
				public void revert() {
					loop.add(index + 1, current);
					loop.remove(index);
				}

			});
		} else {
			// Irrelevant updates
		}

		return updates;
	}

	@Override
	public Loop<UnsafeRecursiveLayer> instantiateRecursiveLayer() {
		DefinitionProxy<Loop<UnsafeRecursiveLayer>> loopProxy = factory.prepareDefinition();
		Definition<Loop<UnsafeRecursiveLayer>> loop = loopProxy.getDefinition();
		loopProxy.defineAs(factory.defineLoop(UnsafeRecursiveLayer.defineOn(loop)));

		return loop.create();
	}

	@Override
	public String getValidRecursiveContent(Layer layer) {
		return "-----";
	}

	@Test
	public void testGeneratorInstanceProperlyGeneratesInstancesWhenRequired() {
		final Collection<Regex> generated = new HashSet<Regex>();
		Loop<Regex> loop = new Loop<Regex>(new Definition<Regex>() {

			@Override
			public String getRegex() {
				return create().getRegex();
			}

			@Override
			public Regex create() {
				Regex formula = new Regex("[a-zA-Z]");
				generated.add(formula);
				return formula;
			}

			@Override
			public boolean isCompatibleWith(Regex layer) {
				return getRegex().equals(layer.getRegex());
			}

		});
		loop.setContent("abc");

		assertSame(loop.get(0), loop.get(0));
		assertNotSame(loop.get(0), loop.get(1));
		assertNotSame(loop.get(0), loop.get(2));

		assertNotSame(loop.get(1), loop.get(0));
		assertSame(loop.get(1), loop.get(1));
		assertNotSame(loop.get(1), loop.get(2));

		assertNotSame(loop.get(2), loop.get(0));
		assertNotSame(loop.get(2), loop.get(1));
		assertSame(loop.get(2), loop.get(2));

		assertTrue(generated.contains(loop.get(0)));
		assertTrue(generated.contains(loop.get(1)));
		assertTrue(generated.contains(loop.get(2)));
	}

	@Test
	public void testGeneratorInstanceThrowsExceptionOnNullGenerator() {
		try {
			new Loop<Regex>((Definition<Regex>) null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
		}
	}

	@Test
	public void testInvalidContentForRegexThrowsParsingException() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z\n]"));

		try {
			loop.setContent("123");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
			assertEquals(loop.toString(), "Unable to parse REGEX[[a-zA-Z\\n]] for " + loop + " from (1,1): \"123\"",
					e.getMessage());
		}

		try {
			loop.setContent("abc123");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
			assertEquals(loop.toString(), "Unable to parse REGEX[[a-zA-Z\\n]] for " + loop + " from (1,4): \"123\"",
					e.getMessage());
		}

		try {
			loop.setContent("abc\nabc123");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
			assertEquals(loop.toString(), "Unable to parse REGEX[[a-zA-Z\\n]] for " + loop + " from (2,4): \"123\"",
					e.getMessage());
		}

		try {
			loop.setContent("abc123abc");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
			assertEquals(loop.toString(), "Unable to parse REGEX[[a-zA-Z\\n]] for " + loop + " from (1,4): \"123abc\"",
					e.getMessage());
		}
	}

	@Test
	public void testNoContentReturnsNull() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z\n]"));
		assertEquals(null, loop.getContent());
	}

	@Test
	public void testInvalidContentForMinThrowsParsingException() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[0-9\n]"), 5, 10);

		try {
			loop.setContent("123");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
			assertEquals(loop.toString(), "Unable to parse REGEX[[0-9\\n]] for " + loop + " from (1,4): \"\"",
					e.getMessage());
		}

		try {
			loop.setContent("1\n3");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
			assertEquals(loop.toString(), "Unable to parse REGEX[[0-9\\n]] for " + loop + " from (2,2): \"\"",
					e.getMessage());
		}

		try {
			loop.setContent("");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
			assertEquals(loop.toString(), "Unable to parse REGEX[[0-9\\n]] for " + loop + " from (1,1): \"\"",
					e.getMessage());
		}
	}

	@Test
	public void testInvalidContentForMaxThrowsParsingException() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[0-9\n]"), 0, 5);

		try {
			loop.setContent("123456789");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
			assertEquals("Nothing expected for " + loop + " from (1,6): \"6789\"", e.getMessage());
		}

		try {
			loop.setContent("12\n4567\n9");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
			assertEquals("Nothing expected for " + loop + " from (2,3): \"67\\n9\"", e.getMessage());
		}
	}

	@Test
	public void testSizeCorrespondsToNumberOfElementsInLoop() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));

		loop.setContent("Test");
		assertEquals(4, loop.size());
		assertNotNull(loop.get(0));
		assertNotNull(loop.get(1));
		assertNotNull(loop.get(2));
		assertNotNull(loop.get(3));
		try {
			loop.get(4);
		} catch (IndexOutOfBoundsException e) {
		}
	}

	@Test
	public void testIsEmptyOnlyWhenActuallyEmpty() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));

		loop.setContent("Tes");
		assertFalse(loop.isEmpty());

		loop.setContent("");
		assertTrue(loop.isEmpty());

		loop.setContent("a");
		assertFalse(loop.isEmpty());
	}

	@Test
	public void testOccurrenceContentCorrespondsToLoopContent() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]---"));
		loop.setContent("a---b---c---");

		assertEquals("a---", loop.get(0).getContent());
		assertEquals("b---", loop.get(1).getContent());
		assertEquals("c---", loop.get(2).getContent());
	}

	@Test
	public void testUpdateOnOccurrenceContentProperlyUpdateLoopContent() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");

		loop.get(0).setContent("C");
		assertEquals("Cest", loop.getContent());
		loop.get(1).setContent("a");
		assertEquals("Cast", loop.getContent());
		loop.get(3).setContent("e");
		assertEquals("Case", loop.getContent());
	}

	@Test
	public void testUpdateOnOccurrenceContentNotifiesLoopListeners() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");
		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		loop.get(0).setContent("C");
		assertEquals(loop.getContent(), value[0]);
		loop.get(1).setContent("a");
		assertEquals(loop.getContent(), value[0]);
		loop.get(2).setContent("s");
		assertEquals(loop.getContent(), value[0]);
		loop.get(3).setContent("e");
		assertEquals(loop.getContent(), value[0]);
	}

	@Test
	public void testAddContentProperlyAddsElement() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("");

		loop.add(loop.size(), "i");
		assertEquals("i", loop.getContent());
		loop.add(loop.size(), "n");
		assertEquals("in", loop.getContent());
		loop.add(loop.size(), "g");
		assertEquals("ing", loop.getContent());

		loop.add(0, "T");
		assertEquals("Ting", loop.getContent());
		loop.add(1, "e");
		assertEquals("Teing", loop.getContent());
		loop.add(2, "s");
		assertEquals("Tesing", loop.getContent());
		loop.add(3, "t");
		assertEquals("Testing", loop.getContent());
	}

	@Test
	public void testAddContentReturnsCorrectOccurrence() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");

		assertEquals("a", loop.add(3, "a").getContent());
		assertEquals("b", loop.add(0, "b").getContent());
		assertEquals("c", loop.add(loop.size(), "c").getContent());
	}

	@Test
	public void testAddContentProperlyNotifiesListeners() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");
		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		loop.add(loop.size(), "i");
		assertEquals("Testi", value[0]);
		loop.add(loop.size(), "n");
		assertEquals("Testin", value[0]);
		loop.add(loop.size(), "g");
		assertEquals("Testing", value[0]);

		loop.add(0, "V");
		assertEquals("VTesting", value[0]);
		loop.add(1, "i");
		assertEquals("ViTesting", value[0]);
		loop.add(2, "v");
		assertEquals("VivTesting", value[0]);
		loop.add(3, "a");
		assertEquals("VivaTesting", value[0]);
	}

	@Test
	public void testUpdateOnAddedContentProperlyNotifiesListeners() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");
		Regex added = loop.add(2, "a");

		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		added.setContent("C");
		assertEquals(loop.getContent(), value[0]);
	}

	@Test
	public void testAddContentThrowsExceptionOnInvalidContent() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");

		try {
			loop.add(3, "!");
			fail("Exception not thrown with " + loop);
		} catch (ParsingException e) {
			assertEquals(loop.toString(), "Incompatible regex \"[a-zA-Z]\" for content \"!\"", e.getMessage());
		}

		try {
			loop.add(3, "abc");
			fail("Exception not thrown with " + loop);
		} catch (ParsingException e) {
			assertEquals(loop.toString(), "Incompatible regex \"[a-zA-Z]\" for content \"abc\"", e.getMessage());
		}
	}

	@Test
	public void testAddContentThrowsExceptionOnInvalidIndex() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");

		try {
			loop.add(20, "a");
			fail("Exception not thrown with " + loop);
		} catch (IndexOutOfBoundsException e) {
		}

		try {
			loop.add(-5, "a");
			fail("Exception not thrown with " + loop);
		} catch (IndexOutOfBoundsException e) {
		}
	}

	@Test
	public void testAddContentThrowsExceptionIfMaxNotRespected() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"), 0, 5);
		loop.setContent("abcde");

		try {
			loop.add(2, "a");
			fail("Exception not thrown with " + loop);
		} catch (BoundException e) {
		}
	}

	@Test
	public void testAddElementProperlyAddsElement() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");

		loop.add(loop.size(), new Regex("[a-zA-Z]", "i"));
		assertEquals("Testi", loop.getContent());
		loop.add(loop.size(), new Regex("[a-zA-Z]", "n"));
		assertEquals("Testin", loop.getContent());
		loop.add(loop.size(), new Regex("[a-zA-Z]", "g"));
		assertEquals("Testing", loop.getContent());

		loop.add(0, new Regex("[a-zA-Z]", "V"));
		assertEquals("VTesting", loop.getContent());
		loop.add(1, new Regex("[a-zA-Z]", "i"));
		assertEquals("ViTesting", loop.getContent());
		loop.add(2, new Regex("[a-zA-Z]", "v"));
		assertEquals("VivTesting", loop.getContent());
		loop.add(3, new Regex("[a-zA-Z]", "a"));
		assertEquals("VivaTesting", loop.getContent());
	}

	@Test
	public void testAddElementProperlyNotifiesListeners() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");
		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		loop.add(loop.size(), new Regex("[a-zA-Z]", "i"));
		assertEquals("Testi", value[0]);
		loop.add(loop.size(), new Regex("[a-zA-Z]", "n"));
		assertEquals("Testin", value[0]);
		loop.add(loop.size(), new Regex("[a-zA-Z]", "g"));
		assertEquals("Testing", value[0]);

		loop.add(0, new Regex("[a-zA-Z]", "V"));
		assertEquals("VTesting", value[0]);
		loop.add(1, new Regex("[a-zA-Z]", "i"));
		assertEquals("ViTesting", value[0]);
		loop.add(2, new Regex("[a-zA-Z]", "v"));
		assertEquals("VivTesting", value[0]);
		loop.add(3, new Regex("[a-zA-Z]", "a"));
		assertEquals("VivaTesting", value[0]);
	}

	@Test
	public void testUpdateOnAddedElementProperlyNotifiesListeners() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");
		Regex added = new Regex("[a-zA-Z]", "a");
		loop.add(2, added);

		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		added.setContent("C");
		assertEquals(loop.getContent(), value[0]);
	}

	@Test
	public void testAddElementThrowsExceptionOnInvalidRegex() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");

		try {
			loop.add(3, new Regex("[a-zA-Z!]", "a"));
			fail("Exception not thrown with " + loop);
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testAddElementThrowsExceptionOnInvalidIndex() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");

		try {
			loop.add(20, new Regex("[a-zA-Z]", "a"));
			fail("Exception not thrown with " + loop);
		} catch (IndexOutOfBoundsException e) {
		}

		try {
			loop.add(-5, new Regex("[a-zA-Z]", "a"));
			fail("Exception not thrown with " + loop);
		} catch (IndexOutOfBoundsException e) {
		}
	}

	@Test
	public void testAddElementThrowsExceptionIfMaxNotRespected() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"), 0, 5);
		loop.setContent("abcde");

		try {
			loop.add(2, new Regex("[a-zA-Z]", "a"));
			fail("Exception not thrown with " + loop);
		} catch (BoundException e) {
		}
	}

	@Test
	public void testRemoveProperlyRemovesElements() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");

		loop.remove(0);
		assertEquals("est", loop.getContent());
		loop.remove(1);
		assertEquals("et", loop.getContent());
		loop.remove(1);
		assertEquals("e", loop.getContent());
		loop.remove(0);
		assertEquals("", loop.getContent());
	}

	@Test
	public void testRemoveReturnsCorrectOccurrence() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");

		assertEquals("T", loop.remove(0).getContent());
		assertEquals("s", loop.remove(1).getContent());
		assertEquals("t", loop.remove(1).getContent());
		assertEquals("e", loop.remove(0).getContent());
	}

	@Test
	public void testRemoveProperlyNotifiesListeners() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");
		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		loop.remove(3);
		assertEquals("Tes", value[0]);
		loop.remove(0);
		assertEquals("es", value[0]);
		loop.remove(1);
		assertEquals("e", value[0]);
		loop.remove(0);
		assertEquals("", value[0]);
	}

	@Test
	public void testUpdateOnRemovedOccurrenceDoesNotNotifyListeners() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");
		Regex removed = loop.remove(2);

		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		removed.setContent("C");
		assertEquals(null, value[0]);
	}

	@Test
	public void testRemoveThrowsExceptionOnInvalidIndex() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");

		try {
			loop.remove(20);
			fail("Exception not thrown with " + loop);
		} catch (IndexOutOfBoundsException e) {
		}

		try {
			loop.remove(-5);
			fail("Exception not thrown with " + loop);
		} catch (IndexOutOfBoundsException e) {
		}
	}

	@Test
	public void testIndexRemoveThrowsExceptionIfMinNotRespected() {
		Loop<Constant> loop = new Loop<Constant>(factory.defineConstant("a"), 5, 10);
		loop.setContent("aaaaa");

		for (int index = 0; index < loop.size(); index++) {
			try {
				loop.remove(index);
				fail("No exception thrown for index " + index);
			} catch (BoundException e) {
			}
		}
	}

	@Test
	public void testIteratorGivesCorrectSequence() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");

		Iterator<Regex> iterator = loop.iterator();
		for (int index = 0; index < loop.size(); index++) {
			assertTrue("For index " + index, iterator.hasNext());
			assertEquals("For index " + index, loop.get(index), iterator.next());
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testIteratorRemoveProperlyRemoves() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");

		Iterator<Regex> iterator = loop.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals("Tst", loop.getContent());
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals("Ts", loop.getContent());
	}

	@Test
	public void testIteratorRemoveProperlyNotifiesListener() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");
		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		Iterator<Regex> iterator = loop.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals("Tst", value[0]);
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals("Ts", value[0]);
	}

	@Test
	public void testUpdateOnIteratorRemovedOccurrenceDoesNotNotifyListeners() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("Test");

		Regex removed = loop.get(2);
		Iterator<Regex> iterator = loop.iterator();
		iterator.next();
		iterator.next();
		iterator.next();
		iterator.remove();

		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		removed.setContent("C");
		assertEquals(null, value[0]);
	}

	@Test
	public void testIteratorRemoveThrowsExceptionIfMinNotRespected() {
		Loop<Constant> loop = new Loop<Constant>(factory.defineConstant("a"), 5, 10);
		loop.setContent("aaaaa");
		Iterator<Constant> iterator = loop.iterator();

		int index = 0;
		while (iterator.hasNext()) {
			iterator.next();
			try {
				iterator.remove();
				fail("No exception thrown for index " + index);
			} catch (BoundException e) {
			}
			index++;
		}
	}

	@Test
	public void testClearRemovesAll() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("abcde");

		loop.clear();
		assertEquals(0, loop.size());
		assertEquals("", loop.getContent());
	}

	@Test
	public void testClearNotifiesListeners() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("abcde");
		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		loop.clear();
		assertEquals(loop.getContent(), value[0]);
	}

	@Test
	public void testUpdateOnClearedDoesNotNotifyListeners() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		loop.setContent("abcde");
		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		Collection<Layer> sublayers = new LinkedList<Layer>();
		for (Layer sublayer : loop) {
			sublayers.add(sublayer);
		}

		loop.clear();
		value[0] = null;

		for (Layer sublayer : sublayers) {
			sublayer.setContent("z");
			assertEquals(null, value[0]);
		}
	}

	@Test
	public void testClearThrowsExceptionIfMin() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"), 5, 10);
		loop.setContent("abcde");

		try {
			loop.clear();
			fail("No exception thrown");
		} catch (BoundException e) {
		}
	}

	@Test
	public void testListenersNotifiedOncePerAtomicUpdate() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("[a-zA-Z]"));
		final LinkedList<String> values = new LinkedList<String>();
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				values.addFirst(newContent);
			}
		});

		int operationCounter = 0;

		loop.setContent("abcde");
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.add(0, "x");
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.add(2, "x");
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.add(loop.size(), "x");
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.add(0, new Regex("[a-zA-Z]", "x"));
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.add(2, new Regex("[a-zA-Z]", "x"));
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.add(loop.size(), new Regex("[a-zA-Z]", "x"));
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.addAllContents(0, Arrays.asList("a", "b", "c"));
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.addAllContents(2, Arrays.asList("a", "b", "c"));
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.addAllContents(loop.size(), Arrays.asList("a", "b", "c"));
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.addAll(0,
				Arrays.asList(new Regex("[a-zA-Z]", "a"), new Regex("[a-zA-Z]", "b"), new Regex("[a-zA-Z]", "c")));
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.addAll(2,
				Arrays.asList(new Regex("[a-zA-Z]", "a"), new Regex("[a-zA-Z]", "b"), new Regex("[a-zA-Z]", "c")));
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.addAll(loop.size(),
				Arrays.asList(new Regex("[a-zA-Z]", "a"), new Regex("[a-zA-Z]", "b"), new Regex("[a-zA-Z]", "c")));
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.remove(0);
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.remove(2);
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.remove(loop.size() - 1);
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		Iterator<Regex> iterator = loop.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.clear();
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());

		loop.setContent("abc");
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());
	}

	@Test
	public void testNoProblemWithLazyComponents() {
		{
			String content = "abc";
			Loop<Regex> loop = new Loop<Regex>(factory.defineRegex(".+?"));

			loop.setContent(content);
			assertEquals(content, loop.getContent());
			assertEquals("a", loop.get(0).getContent());
			assertEquals("b", loop.get(1).getContent());
			assertEquals("c", loop.get(2).getContent());
		}
		{
			String content = "?abc?def";
			Loop<Regex> loop = new Loop<Regex>(factory.defineRegex("\\?.+?"));

			loop.setContent(content);
			assertEquals(content, loop.getContent());
			assertEquals("?abc", loop.get(0).getContent());
			assertEquals("?def", loop.get(1).getContent());
		}
		{
			String content = "abc?def?";
			Loop<Regex> loop = new Loop<Regex>(factory.defineRegex(".+?\\?"));

			loop.setContent(content);
			assertEquals(content, loop.getContent());
			assertEquals("abc?", loop.get(0).getContent());
			assertEquals("def?", loop.get(1).getContent());
		}
	}

	@Test
	public void testBigLoop() {
		Loop<Regex> loop = new Loop<Regex>(factory.defineRegex(">[0-9]++"), 0, Integer.MAX_VALUE,
				Quantifier.POSSESSIVE);
		Random rand = new Random();
		StringBuilder builder = new StringBuilder();
		for (long i = 0; i < 1e6; i++) {
			builder.append(">" + rand.nextInt(100000));
		}
		String content = builder.toString();

		loop.setContent(content);
		assertEquals(content, loop.getContent());
	}

	@Test
	public void testRecursiveLoopReturnsCorrectParsingException() {
		Loop<UnsafeRecursiveLayer> loop = instantiateRecursiveLayer();
		try {
			loop.setContent("---a---");
			fail("Exception not thrown");
		} catch (ParsingException e) {
			assertEquals("Unable to parse UNSAFE:LOOP[UNSAFE*] for LOOP[UNSAFE*] from (1,4): \"a---\"", e.getMessage());
		}
	}
}
