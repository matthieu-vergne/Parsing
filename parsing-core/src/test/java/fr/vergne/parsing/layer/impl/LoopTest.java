package fr.vergne.parsing.layer.impl;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Test;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.Loop.Generator;

public class LoopTest {

	/**
	 * This methods generates several {@link Loop}s to cover the different
	 * initializations methods (i.e. {@link Generator}, cloneable {@link Layer},
	 * uncloneable {@link Layer}). It is a helper to use in tests which could be
	 * influenced by the initialization of the {@link Loop}. Their
	 * {@link #toString()} methods are overridden to help identifying which
	 * {@link Loop} makes fail the test.
	 * 
	 * @return potential combinations of {@link Loop}s
	 */
	private Collection<Loop<Formula>> createLoops() {
		Collection<Loop<Formula>> combinations = new LinkedList<Loop<Formula>>();
		combinations.add(new Loop<Formula>(new Generator<Formula>() {

			@Override
			public Formula generates() {
				return new Formula("[a-zA-Z]");
			}

		}) {
			@Override
			public String toString() {
				return "Generator-based";
			}
		});
		combinations.add(new Loop<Formula>(new Formula("[a-zA-Z]") {
			public Object clone() {
				return new Formula("[a-zA-Z]");
			}
		}) {
			@Override
			public String toString() {
				return "Cloneable-based";
			}
		});
		combinations.add(new Loop<Formula>(new Formula("[a-zA-Z]")) {
			@Override
			public String toString() {
				return "Uncloneable-based";
			}
		});
		return combinations;
	}

	@Test
	public void testGeneratorInstances() {
		Loop<Formula> loop = new Loop<Formula>(new Generator<Formula>() {

			@Override
			public Formula generates() {
				return new Formula("[a-zA-Z]");
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
	}

	@Test
	public void testCloneableClones() {
		Formula template = new Formula("[a-zA-Z]") {
			public Object clone() {
				return new Formula("[a-zA-Z]");
			}
		};
		Loop<Formula> loop = new Loop<Formula>(template);
		loop.setContent("abc");

		assertNotSame(template, loop.get(0));

		assertSame(loop.get(0), loop.get(0));
		assertNotSame(loop.get(0), loop.get(1));
		assertNotSame(loop.get(0), loop.get(2));

		assertNotSame(loop.get(1), loop.get(0));
		assertSame(loop.get(1), loop.get(1));
		assertNotSame(loop.get(1), loop.get(2));

		assertNotSame(loop.get(2), loop.get(0));
		assertNotSame(loop.get(2), loop.get(1));
		assertSame(loop.get(2), loop.get(2));
	}

	@Test
	public void testUncloneableReuse() {
		Formula template = new Formula("[a-zA-Z]");
		Loop<Formula> loop = new Loop<Formula>(template);
		loop.setContent("abc");

		assertSame(template, loop.get(0));

		assertSame(loop.get(0), loop.get(0));
		assertSame(loop.get(0), loop.get(1));
		assertSame(loop.get(0), loop.get(2));

		assertSame(loop.get(1), loop.get(0));
		assertSame(loop.get(1), loop.get(1));
		assertSame(loop.get(1), loop.get(2));

		assertSame(loop.get(2), loop.get(0));
		assertSame(loop.get(2), loop.get(1));
		assertSame(loop.get(2), loop.get(2));
	}

	@Test
	public void testRegex() {
		Formula letter = new Formula("[a-zA-Z]");
		{
			String regex = new Loop<Formula>(letter).getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("*$"));
		}
		{
			String regex = new Loop<Formula>(letter, 0, 1).getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("?$"));
		}
		{
			String regex = new Loop<Formula>(letter, 1, Integer.MAX_VALUE)
					.getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("+$"));
		}
		{
			String regex = new Loop<Formula>(letter, 3).getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("{3}$"));
		}
		{
			String regex = new Loop<Formula>(letter, 3, Integer.MAX_VALUE)
					.getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("{3,}$"));
		}
		{
			String regex = new Loop<Formula>(letter, 3, 5).getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("{3,5}$"));
		}
	}

	@Test
	public void testGetContent() {
		for (Loop<Formula> loop : createLoops()) {
			String content = "Test";
			loop.setContent(content);

			assertEquals(loop.toString(), content, loop.getContent());
		}
	}

	@Test
	public void testIterator() {
		for (Loop<Formula> loop : createLoops()) {
			String content = "Test";
			loop.setContent(content);

			Iterator<Formula> iterator = loop.iterator();
			assertTrue(loop.toString(), iterator.hasNext());
			assertEquals(loop.toString(), "T", iterator.next().getContent());
			assertTrue(loop.toString(), iterator.hasNext());
			assertEquals(loop.toString(), "e", iterator.next().getContent());
			assertTrue(loop.toString(), iterator.hasNext());
			assertEquals(loop.toString(), "s", iterator.next().getContent());
			assertTrue(loop.toString(), iterator.hasNext());
			assertEquals(loop.toString(), "t", iterator.next().getContent());
			assertFalse(loop.toString(), iterator.hasNext());
		}
	}

	@Test
	public void testInnerContentSynchronization() {
		for (Loop<Formula> loop : createLoops()) {
			String content = "Test";
			loop.setContent(content);

			Iterator<Formula> iterator = loop.iterator();
			assertTrue(loop.toString(), iterator.hasNext());
			iterator.next().setContent("C");
			assertTrue(loop.toString(), iterator.hasNext());
			iterator.next().setContent("a");
			assertTrue(loop.toString(), iterator.hasNext());
			iterator.next().setContent("s");
			assertTrue(loop.toString(), iterator.hasNext());
			iterator.next().setContent("e");

			assertEquals(loop.toString(), "Case", loop.getContent());

			iterator = loop.iterator();
			iterator.next();
			iterator.next();
			iterator.remove();
			iterator.next();
			iterator.next();
			iterator.remove();
			assertEquals(loop.toString(), "Cs", loop.getContent());
		}
	}

	@Test
	public void testSize() {
		for (Loop<Formula> loop : createLoops()) {
			loop.setContent("Test");
			assertEquals(loop.toString(), 4, loop.size());

			loop.setContent("a");
			assertEquals(loop.toString(), 1, loop.size());

			loop.setContent("abcdefghIJKLmnoPQRSTUvWXyZ");
			assertEquals(loop.toString(), 26, loop.size());
		}
	}

	@Test
	public void testIsEmpty() {
		for (Loop<Formula> loop : createLoops()) {
			assertTrue(loop.toString(), loop.isEmpty());

			loop.setContent("Tes");
			assertFalse(loop.toString(), loop.isEmpty());

			loop.setContent("");
			assertTrue(loop.toString(), loop.isEmpty());

			loop.setContent("a");
			assertFalse(loop.toString(), loop.isEmpty());
		}
	}

	@Test
	public void testGetIndex() {
		for (Loop<Formula> loop : createLoops()) {
			loop.setContent("Test");
			assertEquals(loop.toString(), "T", loop.get(0).getContent());
			assertEquals(loop.toString(), "e", loop.get(1).getContent());
			assertEquals(loop.toString(), "s", loop.get(2).getContent());
			assertEquals(loop.toString(), "t", loop.get(3).getContent());

			loop.get(0).setContent("C");
			loop.get(1).setContent("a");
			loop.get(2).setContent("s");
			loop.get(3).setContent("e");
			assertEquals(loop.toString(), "C", loop.get(0).getContent());
			assertEquals(loop.toString(), "a", loop.get(1).getContent());
			assertEquals(loop.toString(), "s", loop.get(2).getContent());
			assertEquals(loop.toString(), "e", loop.get(3).getContent());
		}
	}

	@Test
	public void testRemoveIndex() {
		for (Loop<Formula> loop : createLoops()) {
			loop.setContent("Test");
			assertEquals(loop.toString(), "Test", loop.getContent());
			loop.remove(0);
			assertEquals(loop.toString(), "est", loop.getContent());
			loop.remove(2);
			assertEquals(loop.toString(), "es", loop.getContent());
			loop.remove(0);
			assertEquals(loop.toString(), "s", loop.getContent());
			loop.remove(0);
			assertEquals(loop.toString(), "", loop.getContent());
		}
	}

	@Test
	public void testDuplicate() {
		for (Loop<Formula> loop : createLoops()) {
			loop.setContent("Test");
			assertEquals(loop.toString(), "Test", loop.getContent());
			loop.duplicate(loop.size()).setContent("i");
			assertEquals(loop.toString(), "Testi", loop.getContent());
			loop.duplicate(loop.size()).setContent("n");
			assertEquals(loop.toString(), "Testin", loop.getContent());
			loop.duplicate(loop.size()).setContent("g");
			assertEquals(loop.toString(), "Testing", loop.getContent());
			loop.duplicate(0).setContent("V");
			assertEquals(loop.toString(), "VTesting", loop.getContent());
			loop.duplicate(1).setContent("i");
			assertEquals(loop.toString(), "ViTesting", loop.getContent());
			loop.duplicate(2).setContent("v");
			assertEquals(loop.toString(), "VivTesting", loop.getContent());
			loop.duplicate(3).setContent("a");
			assertEquals(loop.toString(), "VivaTesting", loop.getContent());
			loop.duplicate(3);
			assertEquals(loop.toString(), "VivaaTesting", loop.getContent());
			loop.duplicate(3);
			assertEquals(loop.toString(), "VivaaaTesting", loop.getContent());
		}
	}

	@Test
	public void testAddString() {
		for (Loop<Formula> loop : createLoops()) {
			loop.setContent("Test");
			assertEquals(loop.toString(), "Test", loop.getContent());
			loop.add(loop.size(), "i");
			assertEquals(loop.toString(), "Testi", loop.getContent());
			loop.add(loop.size(), "n");
			assertEquals(loop.toString(), "Testin", loop.getContent());
			loop.add(loop.size(), "g");
			assertEquals(loop.toString(), "Testing", loop.getContent());
			loop.add(0, "V");
			assertEquals(loop.toString(), "VTesting", loop.getContent());
			loop.add(1, "i");
			assertEquals(loop.toString(), "ViTesting", loop.getContent());
			loop.add(2, "v");
			assertEquals(loop.toString(), "VivTesting", loop.getContent());
			loop.add(3, "a");
			assertEquals(loop.toString(), "VivaTesting", loop.getContent());

			try {
				loop.add(5, "!");
				fail("Exception not thrown with " + loop);
			} catch (ParsingException e) {
				assertEquals(
						loop.toString(),
						"Incompatible format \"[a-zA-Z]\" at position 0: \"!\"",
						e.getMessage());
			}

			try {
				loop.add(5, "abc");
				fail("Exception not thrown with " + loop);
			} catch (ParsingException e) {
				assertEquals(
						loop.toString(),
						"Incompatible format \"[a-zA-Z]\" at position 0: \"abc\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testMove() {
		for (Loop<Formula> loop : createLoops()) {
			loop.setContent("Test");

			loop.move(0, 3);
			assertEquals(loop.toString(), "estT", loop.getContent());
			loop.move(2, 0);
			assertEquals(loop.toString(), "tesT", loop.getContent());
		}
	}

	@Test
	public void testSetContent() {
		for (Loop<Formula> loop : createLoops()) {
			String content = "Test";
			loop.setContent(content);
			assertEquals(loop.toString(), content, loop.getContent());

			content = "Noitamina";
			loop.setContent(content);
			assertEquals(loop.toString(), content, loop.getContent());

			try {
				loop.setContent("BOOM!");
				fail("Exception not thrown with " + loop);
			} catch (ParsingException e) {
			}
		}
	}

	@Test
	public void testParsingException() {
		for (Loop<Formula> loop : createLoops()) {
			try {
				loop.setContent("Test_");
				fail("Exception not thrown with " + loop);
			} catch (ParsingException e) {
				assertEquals(
						loop.toString(),
						"Incompatible format \"[a-zA-Z]\" at position 4: \"_\"",
						e.getMessage());
			}

			try {
				loop.setContent("Test_test");
				fail("Exception not thrown with " + loop);
			} catch (ParsingException e) {
				assertEquals(
						loop.toString(),
						"Incompatible format \"[a-zA-Z]\" at position 4: \"_\"",
						e.getMessage());
			}

			try {
				loop.setContent("_Test");
				fail("Exception not thrown with " + loop);
			} catch (ParsingException e) {
				assertEquals(
						loop.toString(),
						"Incompatible format \"[a-zA-Z]\" at position 0: \"_\"",
						e.getMessage());
			}

			try {
				loop.setContent("Test_test_test");
				fail("Exception not thrown with " + loop);
			} catch (ParsingException e) {
				assertEquals(
						loop.toString(),
						"Incompatible format \"[a-zA-Z]\" at position 4: \"_\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testMinMax() {
		Formula letter = new Formula("[a-zA-Z]");
		Loop<Formula> loop = new Loop<Formula>(letter, 3, 5);
		loop.setContent("Tes");

		try {
			loop.setContent("Testing");
			fail("Exception not thrown");
		} catch (ParsingException e) {
			assertEquals("Incompatible format (empty) at position 5: \"ng\"",
					e.getMessage());
		}

		try {
			loop.setContent("Te");
			fail("Exception not thrown");
		} catch (ParsingException e) {
			assertEquals(
					"Incompatible format \"[a-zA-Z]\" at position 2: \"\"",
					e.getMessage());
		}
	}

}
