package fr.vergne.parsing.layer.impl;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import fr.vergne.parsing.layer.exception.ParsingException;

public class LoopTest {

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
		Formula letter = new Formula("[a-zA-Z]");
		Loop<Formula> loop = new Loop<Formula>(letter);

		String content = "Test";
		loop.setContent(content);

		assertEquals(content, loop.getContent());
	}

	@Test
	public void testIterator() {
		Formula letter = new Formula("[a-zA-Z]");
		Loop<Formula> loop = new Loop<Formula>(letter);

		String content = "Test";
		loop.setContent(content);

		Iterator<Formula> iterator = loop.iterator();
		assertTrue(iterator.hasNext());
		assertEquals("T", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("e", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("s", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("t", iterator.next().getContent());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testInnerContentSynchronization() {
		Formula letter = new Formula("[a-zA-Z]");
		Loop<Formula> loop = new Loop<Formula>(letter);

		String content = "Test";
		loop.setContent(content);

		Iterator<Formula> iterator = loop.iterator();
		assertTrue(iterator.hasNext());
		iterator.next().setContent("C");
		assertTrue(iterator.hasNext());
		iterator.next().setContent("a");
		assertTrue(iterator.hasNext());
		iterator.next().setContent("s");
		assertTrue(iterator.hasNext());
		iterator.next().setContent("e");

		assertEquals("Case", loop.getContent());

		iterator = loop.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals("Cs", loop.getContent());
	}

	@Test
	public void testCount() {
		Formula letter = new Formula("[a-zA-Z]");
		Loop<Formula> loop = new Loop<Formula>(letter);

		loop.setContent("Test");
		assertEquals(4, loop.size());

		loop.setContent("a");
		assertEquals(1, loop.size());

		loop.setContent("abcdefghIJKLmnoPQRSTUvWXyZ");
		assertEquals(26, loop.size());
	}

	@Test
	public void testIsEmpty() {
		Formula letter = new Formula("[a-zA-Z]");
		Loop<Formula> loop = new Loop<Formula>(letter);
		assertTrue(loop.isEmpty());

		loop.setContent("Tes");
		assertFalse(loop.isEmpty());

		loop.setContent("");
		assertTrue(loop.isEmpty());

		loop.setContent("a");
		assertFalse(loop.isEmpty());
	}

	@Test
	public void testGetIndex() {
		Formula letter = new Formula("[a-zA-Z]");
		Loop<Formula> loop = new Loop<Formula>(letter);

		loop.setContent("Test");
		assertEquals("T", loop.get(0).getContent());
		assertEquals("e", loop.get(1).getContent());
		assertEquals("s", loop.get(2).getContent());
		assertEquals("t", loop.get(3).getContent());

		loop.get(0).setContent("C");
		loop.get(1).setContent("a");
		loop.get(2).setContent("s");
		loop.get(3).setContent("e");
		assertEquals("C", loop.get(0).getContent());
		assertEquals("a", loop.get(1).getContent());
		assertEquals("s", loop.get(2).getContent());
		assertEquals("e", loop.get(3).getContent());
	}

	@Test
	public void testRemoveIndex() {
		Formula letter = new Formula("[a-zA-Z]");
		Loop<Formula> loop = new Loop<Formula>(letter);

		loop.setContent("Test");
		assertEquals("Test", loop.getContent());
		loop.remove(0);
		assertEquals("est", loop.getContent());
		loop.remove(2);
		assertEquals("es", loop.getContent());
		loop.remove(0);
		assertEquals("s", loop.getContent());
		loop.remove(0);
		assertEquals("", loop.getContent());
	}

	@Test
	public void testDuplicate() {
		Formula letter = new Formula("[a-zA-Z]");
		Loop<Formula> loop = new Loop<Formula>(letter);

		loop.setContent("Test");
		assertEquals("Test", loop.getContent());
		loop.duplicate(loop.size()).setContent("i");
		assertEquals("Testi", loop.getContent());
		loop.duplicate(loop.size()).setContent("n");
		assertEquals("Testin", loop.getContent());
		loop.duplicate(loop.size()).setContent("g");
		assertEquals("Testing", loop.getContent());
		loop.duplicate(0).setContent("V");
		assertEquals("VTesting", loop.getContent());
		loop.duplicate(1).setContent("i");
		assertEquals("ViTesting", loop.getContent());
		loop.duplicate(2).setContent("v");
		assertEquals("VivTesting", loop.getContent());
		loop.duplicate(3).setContent("a");
		assertEquals("VivaTesting", loop.getContent());
		loop.duplicate(3);
		assertEquals("VivaaTesting", loop.getContent());
		loop.duplicate(3);
		assertEquals("VivaaaTesting", loop.getContent());
	}

	@Test
	public void testAddString() {
		Formula letter = new Formula("[a-zA-Z]");
		Loop<Formula> loop = new Loop<Formula>(letter);

		loop.setContent("Test");
		assertEquals("Test", loop.getContent());
		loop.add(loop.size(), "i");
		assertEquals("Testi", loop.getContent());
		loop.add(loop.size(), "n");
		assertEquals("Testin", loop.getContent());
		loop.add(loop.size(), "g");
		assertEquals("Testing", loop.getContent());
		loop.add(0, "V");
		assertEquals("VTesting", loop.getContent());
		loop.add(1, "i");
		assertEquals("ViTesting", loop.getContent());
		loop.add(2, "v");
		assertEquals("VivTesting", loop.getContent());
		loop.add(3, "a");
		assertEquals("VivaTesting", loop.getContent());
	}

	@Test
	public void testSetContent() {
		Formula letter = new Formula("[a-zA-Z]");
		Loop<Formula> loop = new Loop<Formula>(letter);

		String content = "Test";
		loop.setContent(content);
		assertEquals(content, loop.getContent());

		content = "Noitamina";
		loop.setContent(content);
		assertEquals(content, loop.getContent());

		try {
			loop.setContent("BOOM!");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
		}
	}

	@Test
	public void testParsingException() {
		Formula letter = new Formula("[a-zA-Z]");
		Loop<Formula> loop = new Loop<Formula>(letter);

		try {
			loop.setContent("Test_");
			fail("Exception not thrown");
		} catch (ParsingException e) {
			assertEquals(
					"Incompatible format \"[a-zA-Z]\" at position 4: \"_\"",
					e.getMessage());
		}

		try {
			loop.setContent("Test_test");
			fail("Exception not thrown");
		} catch (ParsingException e) {
			assertEquals(
					"Incompatible format \"[a-zA-Z]\" at position 4: \"_\"",
					e.getMessage());
		}

		try {
			loop.setContent("_Test");
			fail("Exception not thrown");
		} catch (ParsingException e) {
			assertEquals(
					"Incompatible format \"[a-zA-Z]\" at position 0: \"_\"",
					e.getMessage());
		}

		try {
			loop.setContent("Test_test_test");
			fail("Exception not thrown");
		} catch (ParsingException e) {
			assertEquals(
					"Incompatible format \"[a-zA-Z]\" at position 4: \"_\"",
					e.getMessage());
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
