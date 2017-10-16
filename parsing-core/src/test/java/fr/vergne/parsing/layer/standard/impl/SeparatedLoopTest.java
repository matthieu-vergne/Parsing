package fr.vergne.parsing.layer.standard.impl;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

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
import fr.vergne.parsing.layer.standard.impl.Loop.BoundException;

@RunWith(JUnitPlatform.class)
public class SeparatedLoopTest implements ModifiableComposedLayerTest<SeparatedLoop<Regex, Constant>> {

	final StandardDefinitionFactory factory = new StandardDefinitionFactory();

	@Override
	public Map<String, SeparatedLoop<Regex, Constant>> instantiateLayers(Collection<String> specialCharacters) {
		String sep = ",";
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<>(factory.defineRegex("[^,]+"),
				factory.defineConstant(sep));

		StringBuilder builder = new StringBuilder();
		for (String character : specialCharacters) {
			builder.append(sep);
			builder.append(character);
		}
		String content = builder.toString().substring(sep.length());

		Map<String, SeparatedLoop<Regex, Constant>> map = new HashMap<>();
		map.put(content, loop);

		SeparatedLoop<Regex, Constant> loop2 = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		map.put("a,b,c,d", loop2);
		map.put("a", loop2);
		map.put("", loop2);

		return map;
	}

	@Override
	public Collection<Layer> getUsedSubLayers(SeparatedLoop<Regex, Constant> loop) {
		Collection<Layer> sublayers = new LinkedList<>();
		for (int i = 0; i < loop.size(); i++) {
			sublayers.add(loop.get(i));
		}
		for (int i = 0; i < loop.size() - 1; i++) {
			sublayers.add(loop.getSeparator(i));
		}
		return sublayers;
	}

	@Override
	public Collection<Update> getSublayersUpdatesFunctions(SeparatedLoop<Regex, Constant> loop) {
		Collection<Update> updates = new LinkedList<>();

		if (loop.size() > 0) {
			// Item addition+removal at the start
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

			// Item addition+removal at the end
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

		if (loop.size() > 1) {
			// Separator addition+removal
			updates.add(new Update() {

				Constant replacement;

				@Override
				public Layer getInitial() {
					return null;
				}

				@Override
				public Layer getReplacement() {
					return replacement;
				}

				@Override
				public void execute() {
					Regex reference = loop.get(loop.size() - 1);
					Regex addition = loop.getItemDefinition().create();
					addition.setContent(reference.getContent());
					loop.add(loop.size(), addition);
					replacement = loop.getSeparator(loop.size() - 2);
				}

				@Override
				public void revert() {
					loop.remove(loop.size() - 1);
				}

			});

			// Item addition+removal at the end
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
	public Layer instantiateRecursiveLayer() {
		DefinitionProxy<SeparatedLoop<UnsafeRecursiveLayer, Constant>> loopProxy = factory.prepareDefinition();
		Definition<SeparatedLoop<UnsafeRecursiveLayer, Constant>> loop = loopProxy.getDefinition();
		loopProxy.defineAs(factory.defineSeparatedLoop(UnsafeRecursiveLayer.defineOn(loop), factory.defineConstant(",")));
		return loop.create();
	}

	@Override
	public String getValidRecursiveContent(Layer layer) {
		return "-,-,-";
	}

	@Test
	public void testInvalidContentForRegexThrowsParsingException() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));

		try {
			loop.setContent("1,2,3");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
		}

		try {
			loop.setContent("a,b,c,1,2,3");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
		}

		try {
			loop.setContent("a,b,c,1,2,3,a,b,c");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
		}
	}

	@Test
	public void testInvalidContentForMinThrowsParsingException() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[0-9]"),
				factory.defineConstant(","), 5, 10);

		try {
			loop.setContent("1,2,3");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
		}

		try {
			loop.setContent("");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
		}
	}

	@Test
	public void testInvalidContentForMaxThrowsParsingException() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[0-9]"),
				factory.defineConstant(","), 0, 5);

		try {
			loop.setContent("1,2,3,4,5,6,7,8,9");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
		}
	}

	@Test
	public void testSizeCorrespondsToNumberOfItemsInLoop() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));

		loop.setContent("");
		assertEquals(0, loop.size());

		loop.setContent("a");
		assertEquals(1, loop.size());

		loop.setContent("a,b");
		assertEquals(2, loop.size());

		loop.setContent("a,b,c");
		assertEquals(3, loop.size());
	}

	@Test
	public void testSizeCorrespondsToNumberOfAddedElements() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));

		assertEquals(0, loop.size());

		loop.add(0, "a");
		assertEquals(1, loop.size());

		loop.add(0, "b");
		assertEquals(2, loop.size());

		loop.add(0, "c");
		assertEquals(3, loop.size());
	}

	@Test
	public void testIsEmptyOnlyWhenActuallyEmpty() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));

		loop.setContent("a,b,c");
		assertFalse(loop.isEmpty());

		loop.setContent("");
		assertTrue(loop.isEmpty());

		loop.setContent("a");
		assertFalse(loop.isEmpty());
	}

	@Test
	public void testOccurrenceContentCorrespondsToLoopContent() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c");

		assertEquals("a", loop.get(0).getContent());
		assertEquals("b", loop.get(1).getContent());
		assertEquals("c", loop.get(2).getContent());

	}

	@Test
	public void testSeparatorContentCorrespondsToLoopContent() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c");

		assertEquals(",", loop.getSeparator(0).getContent());
		assertEquals(",", loop.getSeparator(1).getContent());

	}

	@Test
	public void testUpdateOnOccurrenceContentProperlyUpdateLoopContent() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");

		loop.get(0).setContent("e");
		assertEquals("e,b,c,d", loop.getContent());
		loop.get(1).setContent("f");
		assertEquals("e,f,c,d", loop.getContent());
		loop.get(2).setContent("g");
		assertEquals("e,f,g,d", loop.getContent());
		loop.get(3).setContent("h");
		assertEquals("e,f,g,h", loop.getContent());
	}

	@Test
	public void testUpdateOnSeparatorContentProperlyUpdateLoopContent() {
		SeparatedLoop<Regex, Regex> loop = new SeparatedLoop<Regex, Regex>(factory.defineRegex("[a-zA-Z]"),
				factory.defineRegex("[,;]"));
		loop.setContent("a,b,c,d");

		loop.getSeparator(0).setContent(";");
		assertEquals("a;b,c,d", loop.getContent());
		loop.getSeparator(1).setContent(";");
		assertEquals("a;b;c,d", loop.getContent());
		loop.getSeparator(2).setContent(";");
		assertEquals("a;b;c;d", loop.getContent());
	}

	@Test
	public void testUpdateOnOccurrenceNotifiesLoopListeners() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");
		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		loop.get(0).setContent("e");
		assertEquals(loop.getContent(), value[0]);
		loop.get(1).setContent("f");
		assertEquals(loop.getContent(), value[0]);
		loop.get(2).setContent("g");
		assertEquals(loop.getContent(), value[0]);
		loop.get(3).setContent("h");
		assertEquals(loop.getContent(), value[0]);
	}

	@Test
	public void testUpdateOnSeparatorNotifiesLoopListeners() {
		SeparatedLoop<Regex, Regex> loop = new SeparatedLoop<Regex, Regex>(factory.defineRegex("[a-zA-Z]"),
				factory.defineRegex("[,;]"));
		loop.setContent("a,b,c,d");
		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		loop.getSeparator(0).setContent(";");
		assertEquals(loop.getContent(), value[0]);
		loop.getSeparator(1).setContent(";");
		assertEquals(loop.getContent(), value[0]);
		loop.getSeparator(2).setContent(";");
		assertEquals(loop.getContent(), value[0]);
	}

	@Test
	public void testSetAddSeparatorThrowsExecptionOnInvalidRegex() {
		SeparatedLoop<Regex, Regex> loop = new SeparatedLoop<Regex, Regex>(factory.defineRegex("[a-zA-Z]"),
				factory.defineRegex("[,;]"));

		try {
			loop.setDefaultSeparator("+");
			fail("No exception thrown");
		} catch (ParsingException e) {
		}
	}

	@Test
	public void testDefaultSeparatorGuessedFromContent() {
		SeparatedLoop<Regex, Regex> loop = new SeparatedLoop<Regex, Regex>(factory.defineRegex("[a-zA-Z]"),
				factory.defineRegex("[,;]"));

		loop.setContent("a,b,c,d");
		assertEquals(",", loop.getDefaultSeparator());

		loop.setContent("a;b;c;d");
		assertEquals(";", loop.getDefaultSeparator());
	}

	@Test
	public void testGetAddSeparatorReturnsSetAddSeparator() {
		SeparatedLoop<Regex, Regex> loop = new SeparatedLoop<Regex, Regex>(factory.defineRegex("[a-zA-Z]"),
				factory.defineRegex("[,;]"));

		loop.setDefaultSeparator(",");
		assertEquals(",", loop.getDefaultSeparator());

		loop.setDefaultSeparator(";");
		assertEquals(";", loop.getDefaultSeparator());
	}

	@Test
	public void testAddContentProperlyAddsItem() {
		SeparatedLoop<Regex, Regex> loop = new SeparatedLoop<Regex, Regex>(factory.defineRegex("[a-zA-Z]"),
				factory.defineRegex("[,;]"));
		loop.setDefaultSeparator(",");
		loop.setContent("");

		loop.add(loop.size(), "d");
		assertEquals("d", loop.getContent());
		loop.add(loop.size(), "e");
		assertEquals("d,e", loop.getContent());

		loop.add(0, "a");
		assertEquals("a,d,e", loop.getContent());
		loop.add(1, "b");
		assertEquals("a,b,d,e", loop.getContent());
		loop.add(2, "c");
		assertEquals("a,b,c,d,e", loop.getContent());
	}

	@Test
	public void testAddContentUsesDefaultSeparator() {
		SeparatedLoop<Regex, Regex> loop = new SeparatedLoop<Regex, Regex>(factory.defineRegex("[a-zA-Z]"),
				factory.defineRegex("[,;]"));
		loop.setContent("a,b,c,d");

		loop.setDefaultSeparator(",");
		loop.add(loop.size(), "e");
		assertEquals("a,b,c,d,e", loop.getContent());
		loop.add(loop.size(), "f");
		assertEquals("a,b,c,d,e,f", loop.getContent());

		loop.setDefaultSeparator(";");
		loop.add(1, "z");
		assertEquals("a;z,b,c,d,e,f", loop.getContent());
		loop.add(0, "y");
		assertEquals("y;a;z,b,c,d,e,f", loop.getContent());
		loop.add(3, "x");
		assertEquals("y;a;z;x,b,c,d,e,f", loop.getContent());
	}

	@Test
	public void testAddContentReturnsCorrectOccurrence() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");

		assertEquals("a", loop.add(3, "a").getContent());
		assertEquals("b", loop.add(0, "b").getContent());
		assertEquals("c", loop.add(loop.size(), "c").getContent());
	}

	@Test
	public void testAddContentProperlyNotifiesListeners() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");
		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		loop.add(0, "i");
		assertEquals(loop.getContent(), value[0]);
		loop.add(2, "j");
		assertEquals(loop.getContent(), value[0]);
		loop.add(loop.size(), "k");
		assertEquals(loop.getContent(), value[0]);
	}

	@Test
	public void testUpdateOnAddedContentProperlyNotifiesListeners() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");
		Regex added = loop.add(2, "x");

		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		added.setContent("z");
		assertEquals(loop.getContent(), value[0]);
	}

	@Test
	public void testAddContentThrowsExceptionOnInvalidContent() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");

		try {
			loop.add(2, "!");
			fail("Exception not thrown with " + loop);
		} catch (ParsingException e) {
			assertEquals(loop.toString(), "Incompatible regex \"[a-zA-Z]\" for content \"!\"", e.getMessage());
		}

		try {
			loop.add(2, "abc");
			fail("Exception not thrown with " + loop);
		} catch (ParsingException e) {
			assertEquals(loop.toString(), "Incompatible regex \"[a-zA-Z]\" for content \"abc\"", e.getMessage());
		}
	}

	@Test
	public void testAddContentThrowsExceptionOnInvalidIndex() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");

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
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","), 0, 5);
		loop.setContent("a,b,c,d,e");

		try {
			loop.add(2, "a");
			fail("Exception not thrown with " + loop);
		} catch (BoundException e) {
		}
	}

	@Test
	public void testAddItemProperlyAddsItem() {
		SeparatedLoop<Regex, Regex> loop = new SeparatedLoop<Regex, Regex>(factory.defineRegex("[a-zA-Z]"),
				factory.defineRegex("[,;]"));
		loop.setContent("a,b,c,d");
		loop.setDefaultSeparator(",");

		loop.add(loop.size(), new Regex("[a-zA-Z]", "e"));
		assertEquals("a,b,c,d,e", loop.getContent());
		loop.add(loop.size(), new Regex("[a-zA-Z]", "f"));
		assertEquals("a,b,c,d,e,f", loop.getContent());

		loop.add(1, new Regex("[a-zA-Z]", "z"));
		assertEquals("a,z,b,c,d,e,f", loop.getContent());
		loop.add(0, new Regex("[a-zA-Z]", "y"));
		assertEquals("y,a,z,b,c,d,e,f", loop.getContent());
		loop.add(3, new Regex("[a-zA-Z]", "x"));
		assertEquals("y,a,z,x,b,c,d,e,f", loop.getContent());
	}

	@Test
	public void testAddItemUsesCorrectSeparator() {
		SeparatedLoop<Regex, Regex> loop = new SeparatedLoop<Regex, Regex>(factory.defineRegex("[a-zA-Z]"),
				factory.defineRegex("[,;]"));
		loop.setContent("a,b,c,d");

		loop.setDefaultSeparator(",");
		loop.add(loop.size(), new Regex("[a-zA-Z]", "e"));
		assertEquals("a,b,c,d,e", loop.getContent());
		loop.add(loop.size(), new Regex("[a-zA-Z]", "f"));
		assertEquals("a,b,c,d,e,f", loop.getContent());

		loop.setDefaultSeparator(";");
		loop.add(1, new Regex("[a-zA-Z]", "z"));
		assertEquals("a;z,b,c,d,e,f", loop.getContent());
		loop.add(0, new Regex("[a-zA-Z]", "y"));
		assertEquals("y;a;z,b,c,d,e,f", loop.getContent());
		loop.add(3, new Regex("[a-zA-Z]", "x"));
		assertEquals("y;a;z;x,b,c,d,e,f", loop.getContent());
	}

	@Test
	public void testAddItemProperlyNotifiesListeners() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");
		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		loop.add(0, new Regex("[a-zA-Z]", "i"));
		assertEquals(loop.getContent(), value[0]);
		loop.add(2, new Regex("[a-zA-Z]", "j"));
		assertEquals(loop.getContent(), value[0]);
		loop.add(loop.size(), new Regex("[a-zA-Z]", "k"));
		assertEquals(loop.getContent(), value[0]);
	}

	@Test
	public void testUpdateOnAddedItemProperlyNotifiesListeners() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setDefaultSeparator(",");
		Regex added0 = new Regex("[a-zA-Z]", "a");
		Regex added1 = new Regex("[a-zA-Z]", "b");
		Regex added2 = new Regex("[a-zA-Z]", "c");
		loop.add(0, added0);
		loop.add(1, added1);
		loop.add(2, added2);

		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		added0.setContent("x");
		assertEquals(loop.getContent(), value[0]);
		added1.setContent("y");
		assertEquals(loop.getContent(), value[0]);
		added2.setContent("z");
		assertEquals(loop.getContent(), value[0]);
	}

	@Test
	public void testAddItemThrowsExceptionOnInvalidRegex() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");

		try {
			loop.add(0, new Regex("[a-zA-Z!]", "a"));
			fail("Exception not thrown with " + loop);
		} catch (IllegalArgumentException e) {
		}

		try {
			loop.add(2, new Regex("[a-zA-Z!]", "a"));
			fail("Exception not thrown with " + loop);
		} catch (IllegalArgumentException e) {
		}

		try {
			loop.add(4, new Regex("[a-zA-Z!]", "a"));
			fail("Exception not thrown with " + loop);
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testAddItemThrowsExceptionOnInvalidIndex() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");

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
	public void testAddItemThrowsExceptionIfMaxNotRespected() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","), 0, 5);
		loop.setContent("a,b,c,d,e");

		try {
			loop.add(2, new Regex("[a-zA-Z]", "a"));
			fail("Exception not thrown with " + loop);
		} catch (BoundException e) {
		}
	}

	@Test
	public void testRemoveProperlyRemovesItems() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");

		loop.remove(0);
		assertEquals("b,c,d", loop.getContent());
		loop.remove(1);
		assertEquals("b,d", loop.getContent());
		loop.remove(1);
		assertEquals("b", loop.getContent());
		loop.remove(0);
		assertEquals("", loop.getContent());
	}

	@Test
	public void testRemoveProperlyNotifiesListeners() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");
		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		loop.remove(3);
		assertEquals(loop.getContent(), value[0]);
		loop.remove(0);
		assertEquals(loop.getContent(), value[0]);
		loop.remove(1);
		assertEquals(loop.getContent(), value[0]);
		loop.remove(0);
		assertEquals(loop.getContent(), value[0]);
	}

	@Test
	public void testUpdateOnRemovedOccurrenceDoesNotNotifyListeners() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");
		Regex removed = loop.remove(2);

		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		removed.setContent("x");
		assertEquals(null, value[0]);
	}

	@Test
	public void testRemoveThrowsExceptionOnInvalidIndex() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");

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
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","), 5, 10);
		loop.setContent("a,b,c,d,e");

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
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");

		Iterator<Regex> iterator = loop.iterator();
		for (int index = 0; index < loop.size(); index++) {
			assertTrue("For index " + index, iterator.hasNext());
			assertEquals("For index " + index, loop.get(index), iterator.next());
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testIteratorRemoveProperlyRemoves() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");

		Iterator<Regex> iterator = loop.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals("a,c,d", loop.getContent());
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals("a,c", loop.getContent());
	}

	@Test
	public void testIteratorRemoveProperlyNotifiesListener() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");
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
		assertEquals(loop.getContent(), value[0]);
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals(loop.getContent(), value[0]);
	}

	@Test
	public void testUpdateOnIteratorRemovedOccurrenceDoesNotNotifyListeners() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d");

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

		removed.setContent("x");
		assertEquals(null, value[0]);
	}

	@Test
	public void testIteratorRemoveThrowsExceptionIfMinNotRespected() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","), 5, 10);
		loop.setContent("a,b,c,d,e");
		Iterator<Regex> iterator = loop.iterator();

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

	/**************************************************************/

	@Test
	public void testGetIndex() {
		SeparatedLoop<Regex, Constant> separatedLoop = new SeparatedLoop<>(factory.defineRegex("\\d+"),
				factory.defineConstant("-"));

		separatedLoop.setContent("1");
		assertEquals("1", separatedLoop.get(0).getContent());

		separatedLoop.setContent("1-2");
		assertEquals("1", separatedLoop.get(0).getContent());
		assertEquals("2", separatedLoop.get(1).getContent());

		separatedLoop.setContent("9-8-7");
		assertEquals("9", separatedLoop.get(0).getContent());
		assertEquals("8", separatedLoop.get(1).getContent());
		assertEquals("7", separatedLoop.get(2).getContent());
	}

	@Test
	public void testGetWrongIndex() {
		SeparatedLoop<Regex, Constant> separatedLoop = new SeparatedLoop<>(factory.defineRegex("\\d+"),
				factory.defineConstant("-"));

		separatedLoop.setContent("");
		try {
			separatedLoop.get(-1);
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			separatedLoop.get(0);
		} catch (IndexOutOfBoundsException e) {
		}

		separatedLoop.setContent("1");
		try {
			separatedLoop.get(-1);
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			separatedLoop.get(1);
		} catch (IndexOutOfBoundsException e) {
		}

		separatedLoop.setContent("1-2");
		try {
			separatedLoop.get(-1);
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			separatedLoop.get(2);
		} catch (IndexOutOfBoundsException e) {
		}

		separatedLoop.setContent("9-8-7");
		try {
			separatedLoop.get(-1);
		} catch (IndexOutOfBoundsException e) {
		}
		try {
			separatedLoop.get(3);
		} catch (IndexOutOfBoundsException e) {
		}
	}

	@Test
	public void testIndexRemoveActuallyRemove() {
		SeparatedLoop<Regex, Constant> separatedLoop = new SeparatedLoop<>(factory.defineRegex("\\d+"),
				factory.defineConstant("-"));
		separatedLoop.setContent("1-2-3-4-5");

		separatedLoop.remove(2);
		assertEquals("1-2-4-5", separatedLoop.getContent());
		separatedLoop.remove(0);
		assertEquals("2-4-5", separatedLoop.getContent());
		separatedLoop.remove(2);
		assertEquals("2-4", separatedLoop.getContent());
		separatedLoop.remove(0);
		assertEquals("4", separatedLoop.getContent());
		separatedLoop.remove(0);
		assertEquals("", separatedLoop.getContent());
	}

	@Test
	public void testIndexRemoveReturnsCorrectOccurrence() {
		SeparatedLoop<Regex, Constant> separatedLoop = new SeparatedLoop<>(factory.defineRegex("\\d+"),
				factory.defineConstant(","));
		separatedLoop.setContent("1,2,3,4,5");

		assertEquals("5", separatedLoop.remove(4).getContent());
		assertEquals("3", separatedLoop.remove(2).getContent());
		assertEquals("1", separatedLoop.remove(0).getContent());
		assertEquals("2", separatedLoop.remove(0).getContent());
		assertEquals("4", separatedLoop.remove(0).getContent());
	}

	@Test
	public void testIndexRemoveNotifies() {
		SeparatedLoop<Regex, Constant> separatedLoop = new SeparatedLoop<>(factory.defineRegex("\\d+"),
				factory.defineConstant("-"));
		separatedLoop.setContent("1-2-3-4-5");
		final String[] value = new String[] { null };
		separatedLoop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		separatedLoop.remove(2);
		assertEquals("1-2-4-5", value[0]);
		separatedLoop.remove(0);
		assertEquals("2-4-5", value[0]);
		separatedLoop.remove(2);
		assertEquals("2-4", value[0]);
		separatedLoop.remove(0);
		assertEquals("4", value[0]);
		separatedLoop.remove(0);
		assertEquals("", value[0]);
	}

	@Test
	public void testParsingException() {
		SeparatedLoop<Regex, Constant> separatedLoop = new SeparatedLoop<>(factory.defineRegex("\\d+"),
				factory.defineConstant("-"));

		try {
			separatedLoop.setContent("-");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			separatedLoop.setContent("-1");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			separatedLoop.setContent("1-");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			separatedLoop.setContent("-1-");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			separatedLoop.setContent("1x2");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}
	}

	@Test
	public void testIteratorProvidesCorrectSequence() {
		SeparatedLoop<Regex, Constant> separatedLoop = new SeparatedLoop<>(factory.defineRegex("\\d+"),
				factory.defineConstant("-"));

		separatedLoop.setContent("1-2-3-4-5");
		Iterator<Regex> iterator = separatedLoop.iterator();
		assertTrue(iterator.hasNext());
		assertEquals("1", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("2", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("3", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("4", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("5", iterator.next().getContent());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testIteratorRemoveActuallyRemoves() {
		SeparatedLoop<Regex, Constant> separatedLoop = new SeparatedLoop<>(factory.defineRegex("\\d+"),
				factory.defineConstant("-"));

		separatedLoop.setContent("1-2-3-4-5");
		Iterator<Regex> iterator = separatedLoop.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals("1-3-4-5", separatedLoop.getContent());
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals("1-3-5", separatedLoop.getContent());
		iterator = separatedLoop.iterator();
		iterator.next();
		iterator.remove();
		assertEquals("3-5", separatedLoop.getContent());
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals("3", separatedLoop.getContent());
	}

	@Test
	public void testIteratorRemoveNotifies() {
		SeparatedLoop<Regex, Constant> separatedLoop = new SeparatedLoop<>(factory.defineRegex("\\d+"),
				factory.defineConstant("-"));
		final String[] value = new String[] { null };
		separatedLoop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		separatedLoop.setContent("1-2-3-4-5");
		Iterator<Regex> iterator = separatedLoop.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals("1-3-4-5", value[0]);
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals("1-3-5", value[0]);
	}

	@Test
	public void testMinMax() {
		SeparatedLoop<Regex, Constant> separatedLoop = new SeparatedLoop<>(factory.defineRegex("\\d+"),
				factory.defineConstant("-"), 2, 5);

		try {
			separatedLoop.setContent("");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			separatedLoop.setContent("1");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		separatedLoop.setContent("1-2");
		separatedLoop.setContent("1-2-3");
		separatedLoop.setContent("1-2-3-4");
		separatedLoop.setContent("1-2-3-4-5");

		try {
			separatedLoop.setContent("1-2-3-4-5-6");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}

		try {
			separatedLoop.setContent("1-2-3-4-5-6-7");
			fail("No exception thrown.");
		} catch (ParsingException e) {
		}
	}

	@Test
	public void testClearRemovesAll() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d,e");

		loop.clear();
		assertEquals(0, loop.size());
		assertEquals("", loop.getContent());
	}

	@Test
	public void testClearNotifiesListeners() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d,e");
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
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		loop.setContent("a,b,c,d,e");
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
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","), 5, 10);
		loop.setContent("a,b,c,d,e");

		try {
			loop.clear();
			fail("No exception thrown");
		} catch (BoundException e) {
		}
	}

	@Test
	public void testListenersNotifiedOncePerAtomicUpdate() {
		SeparatedLoop<Regex, Constant> loop = new SeparatedLoop<Regex, Constant>(factory.defineRegex("[a-zA-Z]"),
				factory.defineConstant(","));
		final LinkedList<String> values = new LinkedList<String>();
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				values.addFirst(newContent);
			}
		});

		int operationCounter = 0;

		loop.setContent("a,b,c,d,e");
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

		loop.setContent("a,b,c");
		assertEquals(++operationCounter, values.size());
		assertEquals(loop.getContent(), values.getFirst());
	}
}
