package fr.vergne.parsing.layer.util;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.Layer.ContentListener;
import fr.vergne.parsing.layer.LayerTest;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Loop.BoundException;
import fr.vergne.parsing.layer.standard.Loop.Generator;

public class SeparatedLoopTest extends LayerTest {

	@Override
	protected Map<String, Layer> instantiateLayers(
			Collection<String> specialCharacters) {
		String sep = ",";
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[^,]+"), new Atom(sep));

		StringBuilder builder = new StringBuilder();
		for (String character : specialCharacters) {
			builder.append(sep);
			builder.append(character);
		}
		String content = builder.toString().substring(sep.length());

		Map<String, Layer> map = new HashMap<String, Layer>();
		map.put(content, loop);

		SeparatedLoop<Formula, Atom> loop2 = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		map.put("a,b,c,d", loop2);
		map.put("a", loop2);
		map.put("", loop2);

		return map;
	}

	@Test
	public void testGeneratorInstanceProperlyGeneratesInstancesWhenRequired() {
		final Collection<Formula> generated = new HashSet<Formula>();
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Generator<Formula>() {

					@Override
					public Formula generates() {
						Formula formula = new Formula("[a-zA-Z]");
						generated.add(formula);
						return formula;
					}

				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						Atom atom = new Atom(",");
						generated.add(atom);
						return atom;
					}

				});
		loop.setContent("a,b,c");

		assertSame(loop.get(0), loop.get(0));
		assertNotSame(loop.get(0), loop.get(1));
		assertNotSame(loop.get(0), loop.get(2));

		assertNotSame(loop.get(1), loop.get(0));
		assertSame(loop.get(1), loop.get(1));
		assertNotSame(loop.get(1), loop.get(2));

		assertNotSame(loop.get(2), loop.get(0));
		assertNotSame(loop.get(2), loop.get(1));
		assertSame(loop.get(2), loop.get(2));

		assertSame(loop.getSeparator(0), loop.getSeparator(0));
		assertNotSame(loop.getSeparator(0), loop.getSeparator(1));

		assertNotSame(loop.getSeparator(1), loop.getSeparator(0));
		assertSame(loop.getSeparator(1), loop.getSeparator(1));

		assertTrue(generated.contains(loop.get(0)));
		assertTrue(generated.contains(loop.get(1)));
		assertTrue(generated.contains(loop.get(2)));
		assertTrue(generated.contains(loop.getSeparator(0)));
		assertTrue(generated.contains(loop.getSeparator(1)));
	}

	@Test
	public void testGeneratorInstanceThrowsExceptionOnNullGenerator() {
		try {
			new SeparatedLoop<Formula, Atom>((Generator<Formula>) null,
					new Generator<Atom>() {

						@Override
						public Atom generates() {
							return new Atom(",");
						}
					});
			fail("No exception thrown");
		} catch (NullPointerException e) {
		}

		try {
			new SeparatedLoop<Formula, Atom>(new Generator<Formula>() {

				@Override
				public Formula generates() {
					return new Formula("[a-zA-Z]");
				}
			}, (Generator<Atom>) null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
		}
	}

	@Test
	public void testTemplateInstanceProperlyGeneratesClones() {
		final Collection<Object> clones = new HashSet<Object>();
		Formula template1 = new Formula("[a-zA-Z]") {
			@Override
			public Object clone() {
				Object clone = super.clone();
				clones.add(clone);
				return clone;
			}
		};
		Atom template2 = new Atom(",") {
			@Override
			public Object clone() {
				Object clone = super.clone();
				clones.add(clone);
				return clone;
			}
		};
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				template1, template2);
		loop.setContent("a,b,c");

		assertNotSame(template1, loop.get(0));
		assertNotSame(template1, loop.get(1));
		assertNotSame(template1, loop.get(2));

		assertNotSame(template2, loop.getSeparator(0));
		assertNotSame(template2, loop.getSeparator(1));

		assertSame(loop.get(0), loop.get(0));
		assertNotSame(loop.get(0), loop.get(1));
		assertNotSame(loop.get(0), loop.get(2));

		assertNotSame(loop.get(1), loop.get(0));
		assertSame(loop.get(1), loop.get(1));
		assertNotSame(loop.get(1), loop.get(2));

		assertNotSame(loop.get(2), loop.get(0));
		assertNotSame(loop.get(2), loop.get(1));
		assertSame(loop.get(2), loop.get(2));

		assertSame(loop.getSeparator(0), loop.getSeparator(0));
		assertNotSame(loop.getSeparator(0), loop.getSeparator(1));

		assertNotSame(loop.getSeparator(1), loop.getSeparator(0));
		assertSame(loop.getSeparator(1), loop.getSeparator(1));

		assertTrue(clones.contains(loop.get(0)));
		assertTrue(clones.contains(loop.get(1)));
		assertTrue(clones.contains(loop.get(2)));
		assertTrue(clones.contains(loop.getSeparator(0)));
		assertTrue(clones.contains(loop.getSeparator(1)));
	}

	@Test
	public void testTemplateInstanceThrowsExceptionOnNullTemplate() {
		try {
			new SeparatedLoop<Formula, Atom>((Formula) null, new Atom(","));
			fail("No exception thrown");
		} catch (NullPointerException e) {
		}

		try {
			new SeparatedLoop<Formula, Atom>(new Formula("[a-zA-Z]"),
					(Atom) null);
			fail("No exception thrown");
		} catch (NullPointerException e) {
		}
	}

	@Test
	public void testInvalidContentForRegexThrowsParsingException() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));

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
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[0-9]"), new Atom(","), 5, 10);

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
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[0-9]"), new Atom(","), 0, 5);

		try {
			loop.setContent("1,2,3,4,5,6,7,8,9");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
		}
	}

	@Test
	public void testSizeCorrespondsToNumberOfElementsInLoop() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c,d");

		assertEquals(4, loop.size());

		assertNotNull(loop.get(0));
		assertNotNull(loop.get(1));
		assertNotNull(loop.get(2));
		assertNotNull(loop.get(3));
		try {
			loop.get(4);
		} catch (IndexOutOfBoundsException e) {
		}

		assertNotNull(loop.getSeparator(0));
		assertNotNull(loop.getSeparator(1));
		assertNotNull(loop.getSeparator(2));
		try {
			loop.getSeparator(3);
		} catch (IndexOutOfBoundsException e) {
		}
	}

	@Test
	public void testIsEmptyOnlyWhenActuallyEmpty() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));

		loop.setContent("a,b,c");
		assertFalse(loop.isEmpty());

		loop.setContent("");
		assertTrue(loop.isEmpty());

		loop.setContent("a");
		assertFalse(loop.isEmpty());
	}

	@Test
	public void testOccurrenceContentCorrespondsToLoopContent() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c");

		assertEquals("a", loop.get(0).getContent());
		assertEquals("b", loop.get(1).getContent());
		assertEquals("c", loop.get(2).getContent());

	}

	@Test
	public void testSeparatorContentCorrespondsToLoopContent() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c");

		assertEquals(",", loop.getSeparator(0).getContent());
		assertEquals(",", loop.getSeparator(1).getContent());

	}

	@Test
	public void testUpdateOnOccurrenceContentProperlyUpdateLoopContent() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
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
		SeparatedLoop<Formula, Formula> loop = new SeparatedLoop<Formula, Formula>(
				new Formula("[a-zA-Z]"), new Formula("[,;]"));
		loop.setContent("a,b,c,d");

		loop.getSeparator(0).setContent(";");
		assertEquals("a;b,c,d", loop.getContent());
		loop.getSeparator(1).setContent(";");
		assertEquals("a;b;c,d", loop.getContent());
		loop.getSeparator(2).setContent(";");
		assertEquals("a;b;c;d", loop.getContent());
	}

	@Test
	public void testUpdateOnOccurrenceContentNotifiesLoopListeners() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
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
	public void testUpdateOnSeparatorContentNotifiesLoopListeners() {
		SeparatedLoop<Formula, Formula> loop = new SeparatedLoop<Formula, Formula>(
				new Formula("[a-zA-Z]"), new Formula("[,;]"));
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
		SeparatedLoop<Formula, Formula> loop = new SeparatedLoop<Formula, Formula>(
				new Formula("[a-zA-Z]"), new Formula("[,;]"));

		try {
			loop.setDefaultSeparator("+");
			fail("No exception thrown");
		} catch (ParsingException e) {
		}
	}

	@Test
	public void testDefaultSeparatorGuessedFromContent() {
		SeparatedLoop<Formula, Formula> loop = new SeparatedLoop<Formula, Formula>(
				new Formula("[a-zA-Z]"), new Formula("[,;]"));

		loop.setContent("a,b,c,d");
		assertEquals(",", loop.getDefaultSeparator());

		loop.setContent("a;b;c;d");
		assertEquals(";", loop.getDefaultSeparator());
	}

	@Test
	public void testGetAddSeparatorReturnsSetAddSeparator() {
		SeparatedLoop<Formula, Formula> loop = new SeparatedLoop<Formula, Formula>(
				new Formula("[a-zA-Z]"), new Formula("[,;]"));

		loop.setDefaultSeparator(",");
		assertEquals(",", loop.getDefaultSeparator());

		loop.setDefaultSeparator(";");
		assertEquals(";", loop.getDefaultSeparator());
	}

	@Test
	public void testAddContentProperlyAddsElement() {
		SeparatedLoop<Formula, Formula> loop = new SeparatedLoop<Formula, Formula>(
				new Formula("[a-zA-Z]"), new Formula("[,;]"));
		loop.setContent("a,b,c,d");
		loop.setDefaultSeparator(",");

		loop.add(loop.size(), "e");
		assertEquals("a,b,c,d,e", loop.getContent());
		loop.add(loop.size(), "f");
		assertEquals("a,b,c,d,e,f", loop.getContent());

		loop.add(1, "z");
		assertEquals("a,z,b,c,d,e,f", loop.getContent());
		loop.add(0, "y");
		assertEquals("y,a,z,b,c,d,e,f", loop.getContent());
		loop.add(3, "x");
		assertEquals("y,a,z,x,b,c,d,e,f", loop.getContent());
	}

	@Test
	public void testAddContentUsesDefaultSeparator() {
		SeparatedLoop<Formula, Formula> loop = new SeparatedLoop<Formula, Formula>(
				new Formula("[a-zA-Z]"), new Formula("[,;]"));
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
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c,d");

		assertEquals("a", loop.add(3, "a").getContent());
		assertEquals("b", loop.add(0, "b").getContent());
		assertEquals("c", loop.add(loop.size(), "c").getContent());
	}

	@Test
	public void testAddContentProperlyNotifiesListeners() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
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
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c,d");
		Formula added = loop.add(2, "x");

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
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c,d");

		try {
			loop.add(2, "!");
			fail("Exception not thrown with " + loop);
		} catch (ParsingException e) {
			assertEquals(loop.toString(),
					"Incompatible regex \"[a-zA-Z]\" for content \"!\"",
					e.getMessage());
		}

		try {
			loop.add(2, "abc");
			fail("Exception not thrown with " + loop);
		} catch (ParsingException e) {
			assertEquals(loop.toString(),
					"Incompatible regex \"[a-zA-Z]\" for content \"abc\"",
					e.getMessage());
		}
	}

	@Test
	public void testAddContentThrowsExceptionOnInvalidIndex() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
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
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","), 0, 5);
		loop.setContent("a,b,c,d,e");

		try {
			loop.add(2, "a");
			fail("Exception not thrown with " + loop);
		} catch (BoundException e) {
		}
	}

	@Test
	public void testAddElementProperlyAddsElement() {
		SeparatedLoop<Formula, Formula> loop = new SeparatedLoop<Formula, Formula>(
				new Formula("[a-zA-Z]"), new Formula("[,;]"));
		loop.setContent("a,b,c,d");
		loop.setDefaultSeparator(",");

		loop.add(loop.size(), new Formula("[a-zA-Z]", "e"));
		assertEquals("a,b,c,d,e", loop.getContent());
		loop.add(loop.size(), new Formula("[a-zA-Z]", "f"));
		assertEquals("a,b,c,d,e,f", loop.getContent());

		loop.add(1, new Formula("[a-zA-Z]", "z"));
		assertEquals("a,z,b,c,d,e,f", loop.getContent());
		loop.add(0, new Formula("[a-zA-Z]", "y"));
		assertEquals("y,a,z,b,c,d,e,f", loop.getContent());
		loop.add(3, new Formula("[a-zA-Z]", "x"));
		assertEquals("y,a,z,x,b,c,d,e,f", loop.getContent());
	}

	@Test
	public void testAddElementUsesCorrectSeparator() {
		SeparatedLoop<Formula, Formula> loop = new SeparatedLoop<Formula, Formula>(
				new Formula("[a-zA-Z]"), new Formula("[,;]"));
		loop.setContent("a,b,c,d");

		loop.setDefaultSeparator(",");
		loop.add(loop.size(), new Formula("[a-zA-Z]", "e"));
		assertEquals("a,b,c,d,e", loop.getContent());
		loop.add(loop.size(), new Formula("[a-zA-Z]", "f"));
		assertEquals("a,b,c,d,e,f", loop.getContent());

		loop.setDefaultSeparator(";");
		loop.add(1, new Formula("[a-zA-Z]", "z"));
		assertEquals("a;z,b,c,d,e,f", loop.getContent());
		loop.add(0, new Formula("[a-zA-Z]", "y"));
		assertEquals("y;a;z,b,c,d,e,f", loop.getContent());
		loop.add(3, new Formula("[a-zA-Z]", "x"));
		assertEquals("y;a;z;x,b,c,d,e,f", loop.getContent());
	}

	@Test
	public void testAddElementProperlyNotifiesListeners() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c,d");
		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		loop.add(0, new Formula("[a-zA-Z]", "i"));
		assertEquals(loop.getContent(), value[0]);
		loop.add(2, new Formula("[a-zA-Z]", "j"));
		assertEquals(loop.getContent(), value[0]);
		loop.add(loop.size(), new Formula("[a-zA-Z]", "k"));
		assertEquals(loop.getContent(), value[0]);
	}

	@Test
	public void testUpdateOnAddedElementProperlyNotifiesListeners() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c,d");
		Formula added = new Formula("[a-zA-Z]", "x");
		loop.add(2, added);

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
	public void testAddElementThrowsExceptionOnInvalidRegex() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c,d");

		try {
			loop.add(3, new Formula("[a-zA-Z!]", "a"));
			fail("Exception not thrown with " + loop);
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	public void testAddElementThrowsExceptionOnInvalidIndex() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c,d");

		try {
			loop.add(20, new Formula("[a-zA-Z]", "a"));
			fail("Exception not thrown with " + loop);
		} catch (IndexOutOfBoundsException e) {
		}

		try {
			loop.add(-5, new Formula("[a-zA-Z]", "a"));
			fail("Exception not thrown with " + loop);
		} catch (IndexOutOfBoundsException e) {
		}
	}

	@Test
	public void testAddElementThrowsExceptionIfMaxNotRespected() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","), 0, 5);
		loop.setContent("a,b,c,d,e");

		try {
			loop.add(2, new Formula("[a-zA-Z]", "a"));
			fail("Exception not thrown with " + loop);
		} catch (BoundException e) {
		}
	}

	@Test
	public void testRemoveProperlyRemovesElements() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
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
	public void testRemoveReturnsCorrectOccurrence() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c,d");

		assertEquals("a", loop.remove(0).getContent());
		assertEquals("c", loop.remove(1).getContent());
		assertEquals("d", loop.remove(1).getContent());
		assertEquals("b", loop.remove(0).getContent());
	}

	@Test
	public void testRemoveProperlyNotifiesListeners() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
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
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c,d");
		Formula removed = loop.remove(2);

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
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
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
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","), 5, 10);
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
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c,d");

		Iterator<Formula> iterator = loop.iterator();
		for (int index = 0; index < loop.size(); index++) {
			assertTrue("For index " + index, iterator.hasNext());
			assertEquals("For index " + index, loop.get(index), iterator.next());
		}
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testIteratorRemoveProperlyRemoves() {
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c,d");

		Iterator<Formula> iterator = loop.iterator();
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
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c,d");
		final String[] value = new String[] { null };
		loop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		Iterator<Formula> iterator = loop.iterator();
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
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","));
		loop.setContent("a,b,c,d");

		Formula removed = loop.get(2);
		Iterator<Formula> iterator = loop.iterator();
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
		SeparatedLoop<Formula, Atom> loop = new SeparatedLoop<Formula, Atom>(
				new Formula("[a-zA-Z]"), new Atom(","), 5, 10);
		loop.setContent("a,b,c,d,e");
		Iterator<Formula> iterator = loop.iterator();

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
	public void testSize() {
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});

		separatedLoop.setContent("");
		assertEquals(0, separatedLoop.size());

		separatedLoop.setContent("1");
		assertEquals(1, separatedLoop.size());

		separatedLoop.setContent("1-2");
		assertEquals(2, separatedLoop.size());

		separatedLoop.setContent("9-8-7");
		assertEquals(3, separatedLoop.size());
	}

	@Test
	public void testGetIndex() {
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});

		separatedLoop.setContent("1");
		assertEquals(1, separatedLoop.get(0).getValue());

		separatedLoop.setContent("1-2");
		assertEquals(1, separatedLoop.get(0).getValue());
		assertEquals(2, separatedLoop.get(1).getValue());

		separatedLoop.setContent("9-8-7");
		assertEquals(9, separatedLoop.get(0).getValue());
		assertEquals(8, separatedLoop.get(1).getValue());
		assertEquals(7, separatedLoop.get(2).getValue());
	}

	@Test
	public void testGetWrongIndex() {
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});

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
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});
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
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});
		separatedLoop.setContent("1-2-3-4-5");

		assertEquals("3", separatedLoop.remove(2).getContent());
		assertEquals("1", separatedLoop.remove(0).getContent());
		assertEquals("5", separatedLoop.remove(2).getContent());
		assertEquals("2", separatedLoop.remove(0).getContent());
		assertEquals("4", separatedLoop.remove(0).getContent());
	}

	@Test
	public void testIndexRemoveNotifies() {
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});
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
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});

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
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});

		separatedLoop.setContent("1-2-3-4-5");
		Iterator<IntNumber> iterator = separatedLoop.iterator();
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
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});

		separatedLoop.setContent("1-2-3-4-5");
		Iterator<IntNumber> iterator = separatedLoop.iterator();
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
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				});
		final String[] value = new String[] { null };
		separatedLoop.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		separatedLoop.setContent("1-2-3-4-5");
		Iterator<IntNumber> iterator = separatedLoop.iterator();
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
		SeparatedLoop<IntNumber, Atom> separatedLoop = new SeparatedLoop<IntNumber, Atom>(
				new Generator<IntNumber>() {

					@Override
					public IntNumber generates() {
						return new IntNumber();
					}
				}, new Generator<Atom>() {

					@Override
					public Atom generates() {
						return new Atom("-");
					}
				}, 2, 5);

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
}
