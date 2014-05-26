package fr.vergne.parsing.layer.standard;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import org.junit.Test;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.LayerTest;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.standard.Loop.Generator;

public class LoopTest extends LayerTest {

	@Override
	protected Layer instantiateFilledLayer() {
		Loop<Formula> loop = new Loop<Formula>(new Formula("[a-zA-Z]"), false);
		loop.setContent("test");
		return loop;
	}

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
	private Collection<Loop<Formula>> createLoops(final String regex, int min,
			int max) {
		Collection<Loop<Formula>> combinations = new LinkedList<Loop<Formula>>();
		combinations.add(new Loop<Formula>(new Generator<Formula>() {

			@Override
			public Formula generates() {
				return new Formula(regex);
			}

		}, min, max) {
			@Override
			public String toString() {
				return "Generator-based";
			}
		});
		combinations.add(new Loop<Formula>(new Formula(regex) {
			public Object clone() {
				return new Formula(regex);
			}
		}, min, max) {
			@Override
			public String toString() {
				return "Cloneable-based";
			}
		});
		combinations
				.add(new Loop<Formula>(new Formula(regex), min, max, false) {
					@Override
					public String toString() {
						return "Uncloneable-based";
					}
				});
		return combinations;
	}

	@Test
	public void testSetGetContent() {
		for (Loop<Formula> loop : createLoops("[a-zA-Z\n]", 0, 10)) {
			{
				String content = "test";
				loop.setContent(content);
				assertEquals(loop.toString(), content, loop.getContent());
			}
			{
				String content = "test\ntest";
				loop.setContent(content);
				assertEquals(loop.toString(), content, loop.getContent());
			}
			{
				String content = "";
				loop.setContent(content);
				assertEquals(loop.toString(), content, loop.getContent());
			}
		}
	}

	@Test
	public void testGetOccurrenceContent() {
		for (Loop<Formula> loop : createLoops("[a-zA-Z\n]", 0, 10)) {
			{
				String content = "test";
				loop.setContent(content);
				assertEquals("t", loop.getContent(0));
				assertEquals("e", loop.getContent(1));
				assertEquals("s", loop.getContent(2));
				assertEquals("t", loop.getContent(3));
			}
			{
				String content = "test\ntest";
				loop.setContent(content);
				assertEquals("t", loop.getContent(0));
				assertEquals("e", loop.getContent(1));
				assertEquals("s", loop.getContent(2));
				assertEquals("t", loop.getContent(3));
				assertEquals("\n", loop.getContent(4));
				assertEquals("t", loop.getContent(5));
				assertEquals("e", loop.getContent(6));
				assertEquals("s", loop.getContent(7));
				assertEquals("t", loop.getContent(8));
			}
		}
	}

	@Test
	public void testInexistentOccurrenceContent() {
		for (Loop<Formula> loop : createLoops("[a-zA-Z\n]", 0, 10)) {
			String content = "test";
			loop.setContent(content);
			try {
				loop.getContent(-1);
				fail("Exception not thrown.");
			} catch (IndexOutOfBoundsException e) {
			}
			try {
				loop.getContent(4);
				fail("Exception not thrown.");
			} catch (IndexOutOfBoundsException e) {
			}
		}
	}

	@Test
	public void testDifferent() {
		for (Loop<Formula> loop : createLoops("[a-zA-Z\n]", 0, 20)) {
			try {
				loop.setContent("123");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(loop.toString(),
						"Unable to parse Formula[[a-zA-Z\\n]] for " + loop
								+ " from (1,1): \"123\"", e.getMessage());
			}
			try {
				loop.setContent("abc123");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(loop.toString(),
						"Unable to parse Formula[[a-zA-Z\\n]] for " + loop
								+ " from (1,4): \"123\"", e.getMessage());
			}
			try {
				loop.setContent("abc\nabc123");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(loop.toString(),
						"Unable to parse Formula[[a-zA-Z\\n]] for " + loop
								+ " from (2,4): \"123\"", e.getMessage());
			}
			try {
				loop.setContent("abc123abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(loop.toString(),
						"Unable to parse Formula[[a-zA-Z\\n]] for " + loop
								+ " from (1,4): \"123abc\"", e.getMessage());
			}
		}
	}

	@Test
	public void testTooLong() {
		for (Loop<Formula> loop : createLoops("[0-9\n]", 0, 5)) {
			{
				try {
					loop.setContent("123456789");
					fail("Exception not thrown.");
				} catch (ParsingException e) {
					assertEquals(loop.toString(), "Nothing expected for "
							+ loop + " from (1,6): \"6789\"", e.getMessage());
				}
			}
			{
				try {
					loop.setContent("12\n4567\n9");
					fail("Exception not thrown.");
				} catch (ParsingException e) {
					assertEquals(loop.toString(), "Nothing expected for "
							+ loop + " from (2,3): \"67\\n9\"", e.getMessage());
				}
			}
		}
	}

	@Test
	public void testTooShort() {
		for (Loop<Formula> loop : createLoops("[0-9\n]", 5, 10)) {
			{
				try {
					loop.setContent("123");
					fail("Exception not thrown.");
				} catch (ParsingException e) {
					assertEquals(loop.toString(),
							"Unable to parse Formula[[0-9\\n]] for " + loop
									+ " from (1,4): \"\"", e.getMessage());
				}
			}
			{
				try {
					loop.setContent("1\n3");
					fail("Exception not thrown.");
				} catch (ParsingException e) {
					assertEquals(loop.toString(),
							"Unable to parse Formula[[0-9\\n]] for " + loop
									+ " from (2,2): \"\"", e.getMessage());
				}
				try {
					loop.setContent("");
					fail("Exception not thrown.");
				} catch (ParsingException e) {
					assertEquals(loop.toString(),
							"Unable to parse Formula[[0-9\\n]] for " + loop
									+ " from (1,1): \"\"", e.getMessage());
				}
			}
		}
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
		Loop<Formula> loop = new Loop<Formula>(template, false);
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
		Formula letter = new Formula("[a-zA-Z]") {
			@Override
			public Object clone() throws CloneNotSupportedException {
				return new Formula(getRegex());
			}
		};
		{
			String regex = new Loop<Formula>(letter).getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("*"));
		}
		{
			String regex = new Loop<Formula>(letter, 0, 1).getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("?"));
		}
		{
			String regex = new Loop<Formula>(letter, 1, Integer.MAX_VALUE)
					.getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("+"));
		}
		{
			String regex = new Loop<Formula>(letter, 3).getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("{3}"));
		}
		{
			String regex = new Loop<Formula>(letter, 3, Integer.MAX_VALUE)
					.getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("{3,}"));
		}
		{
			String regex = new Loop<Formula>(letter, 3, 5).getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("{3,5}"));
		}
	}

	@Test
	public void testIterator() {
		for (Loop<Formula> loop : createLoops("[a-zA-Z]", 0, 50)) {
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
		for (Loop<Formula> loop : createLoops("[a-zA-Z]", 0, 50)) {
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
		for (Loop<Formula> loop : createLoops("[a-zA-Z]", 0, 50)) {
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
		for (Loop<Formula> loop : createLoops("[a-zA-Z]", 0, 50)) {
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
		for (Loop<Formula> loop : createLoops("[a-zA-Z]", 0, 50)) {
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
		for (Loop<Formula> loop : createLoops("[a-zA-Z]", 0, 50)) {
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
		for (Loop<Formula> loop : createLoops("[a-zA-Z]", 0, 50)) {
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
		for (Loop<Formula> loop : createLoops("[a-zA-Z]", 0, 50)) {
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
				assertEquals(loop.toString(),
						"Incompatible regex \"[a-zA-Z]\" for content \"!\"",
						e.getMessage());
			}

			try {
				loop.add(5, "abc");
				fail("Exception not thrown with " + loop);
			} catch (ParsingException e) {
				assertEquals(loop.toString(),
						"Incompatible regex \"[a-zA-Z]\" for content \"abc\"",
						e.getMessage());
			}
		}
	}

	@Test
	public void testMove() {
		for (Loop<Formula> loop : createLoops("[a-zA-Z]", 0, 50)) {
			loop.setContent("Test");

			loop.move(0, 3);
			assertEquals(loop.toString(), "estT", loop.getContent());
			loop.move(2, 0);
			assertEquals(loop.toString(), "tesT", loop.getContent());
		}
	}

	public class A extends Loop<Suite> {

		public A() {
			super(new Generator<Suite>() {

				@Override
				public Suite generates() {
					return new Suite(new Atom("["), new A(), new Atom("]"));
				}
			});
		}

	}

	@Test
	public void testInfiniteLoop() {
		A loop = new A();
		loop.setContent("");
		loop.setContent("[]");
		loop.setContent("[][]");
		loop.setContent("[[]]");
		loop.setContent("[][[][[][][]]][]");
		try {
			loop.setContent("[][]a[][]");
			fail("Exception not thrown");
		} catch (ParsingException e) {
			assertEquals(
					"Unable to parse Suite[Atom, A, Atom] for A[Suite*] from (1,5): \"a[][]\"",
					e.getMessage());
		}
	}
}
