package fr.vergne.parsing.impl;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import fr.vergne.parsing.exception.IncompatibilityException;

public class RepetitionsTest {

	@Test
	public void testRegex() {
		Atom letter = new Atom("[a-zA-Z]");
		{
			String regex = new Repetitions<Atom>(letter).getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("*$"));
		}
		{
			String regex = new Repetitions<Atom>(letter, 0, 1).getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("?$"));
		}
		{
			String regex = new Repetitions<Atom>(letter, 1, Integer.MAX_VALUE)
					.getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("+$"));
		}
		{
			String regex = new Repetitions<Atom>(letter, 3).getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("{3}$"));
		}
		{
			String regex = new Repetitions<Atom>(letter, 3, Integer.MAX_VALUE)
					.getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("{3,}$"));
		}
		{
			String regex = new Repetitions<Atom>(letter, 3, 5).getRegex();
			assertTrue(regex, regex.contains(letter.getRegex()));
			assertTrue(regex, regex.endsWith("{3,5}$"));
		}
	}

	@Test
	public void testGetContent() {
		Atom letter = new Atom("[a-zA-Z]");
		Repetitions<Atom> repetitions = new Repetitions<Atom>(letter);

		String content = "Test";
		repetitions.setContent(content);

		assertEquals(content, repetitions.getContent());
	}

	@Test
	public void testIterator() {
		Atom letter = new Atom("[a-zA-Z]");
		Repetitions<Atom> repetitions = new Repetitions<Atom>(letter);

		String content = "Test";
		repetitions.setContent(content);

		Iterator<Atom> iterator = repetitions.iterator();
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
		Atom letter = new Atom("[a-zA-Z]");
		Repetitions<Atom> repetitions = new Repetitions<Atom>(letter);

		String content = "Test";
		repetitions.setContent(content);

		Iterator<Atom> iterator = repetitions.iterator();
		assertTrue(iterator.hasNext());
		iterator.next().setContent("C");
		assertTrue(iterator.hasNext());
		iterator.next().setContent("a");
		assertTrue(iterator.hasNext());
		iterator.next().setContent("s");
		assertTrue(iterator.hasNext());
		iterator.next().setContent("e");

		assertEquals("Case", repetitions.getContent());

		iterator = repetitions.iterator();
		iterator.next();
		iterator.next();
		iterator.remove();
		iterator.next();
		iterator.next();
		iterator.remove();
		assertEquals("Cs", repetitions.getContent());
	}

	@Test
	public void testCount() {
		Atom letter = new Atom("[a-zA-Z]");
		Repetitions<Atom> repetitions = new Repetitions<Atom>(letter);

		repetitions.setContent("Test");
		assertEquals(4, repetitions.size());

		repetitions.setContent("a");
		assertEquals(1, repetitions.size());

		repetitions.setContent("abcdefghIJKLmnoPQRSTUvWXyZ");
		assertEquals(26, repetitions.size());
	}

	@Test
	public void testGetIndex() {
		Atom letter = new Atom("[a-zA-Z]");
		Repetitions<Atom> repetitions = new Repetitions<Atom>(letter);

		repetitions.setContent("Test");
		assertEquals("T", repetitions.get(0).getContent());
		assertEquals("e", repetitions.get(1).getContent());
		assertEquals("s", repetitions.get(2).getContent());
		assertEquals("t", repetitions.get(3).getContent());

		repetitions.get(0).setContent("C");
		repetitions.get(1).setContent("a");
		repetitions.get(2).setContent("s");
		repetitions.get(3).setContent("e");
		assertEquals("C", repetitions.get(0).getContent());
		assertEquals("a", repetitions.get(1).getContent());
		assertEquals("s", repetitions.get(2).getContent());
		assertEquals("e", repetitions.get(3).getContent());
	}

	@Test
	public void testRemoveIndex() {
		Atom letter = new Atom("[a-zA-Z]");
		Repetitions<Atom> repetitions = new Repetitions<Atom>(letter);

		repetitions.setContent("Test");
		assertEquals("Test", repetitions.getContent());
		repetitions.remove(0);
		assertEquals("est", repetitions.getContent());
		repetitions.remove(2);
		assertEquals("es", repetitions.getContent());
		repetitions.remove(0);
		assertEquals("s", repetitions.getContent());
		repetitions.remove(0);
		assertEquals("", repetitions.getContent());
	}

	@Test
	public void testDuplicate() {
		Atom letter = new Atom("[a-zA-Z]");
		Repetitions<Atom> repetitions = new Repetitions<Atom>(letter);

		repetitions.setContent("Test");
		assertEquals("Test", repetitions.getContent());
		repetitions.duplicate(repetitions.size()).setContent("i");
		assertEquals("Testi", repetitions.getContent());
		repetitions.duplicate(repetitions.size()).setContent("n");
		assertEquals("Testin", repetitions.getContent());
		repetitions.duplicate(repetitions.size()).setContent("g");
		assertEquals("Testing", repetitions.getContent());
		repetitions.duplicate(0).setContent("V");
		assertEquals("VTesting", repetitions.getContent());
		repetitions.duplicate(1).setContent("i");
		assertEquals("ViTesting", repetitions.getContent());
		repetitions.duplicate(2).setContent("v");
		assertEquals("VivTesting", repetitions.getContent());
		repetitions.duplicate(3).setContent("a");
		assertEquals("VivaTesting", repetitions.getContent());
		repetitions.duplicate(3);
		assertEquals("VivaaTesting", repetitions.getContent());
		repetitions.duplicate(3);
		assertEquals("VivaaaTesting", repetitions.getContent());
	}

	@Test
	public void testAddString() {
		Atom letter = new Atom("[a-zA-Z]");
		Repetitions<Atom> repetitions = new Repetitions<Atom>(letter);

		repetitions.setContent("Test");
		assertEquals("Test", repetitions.getContent());
		repetitions.add(repetitions.size(), "i");
		assertEquals("Testi", repetitions.getContent());
		repetitions.add(repetitions.size(), "n");
		assertEquals("Testin", repetitions.getContent());
		repetitions.add(repetitions.size(), "g");
		assertEquals("Testing", repetitions.getContent());
		repetitions.add(0, "V");
		assertEquals("VTesting", repetitions.getContent());
		repetitions.add(1, "i");
		assertEquals("ViTesting", repetitions.getContent());
		repetitions.add(2, "v");
		assertEquals("VivTesting", repetitions.getContent());
		repetitions.add(3, "a");
		assertEquals("VivaTesting", repetitions.getContent());
	}

	@Test
	public void testSetContent() {
		Atom letter = new Atom("[a-zA-Z]");
		Repetitions<Atom> repetitions = new Repetitions<Atom>(letter);

		String content = "Test";
		repetitions.setContent(content);
		assertEquals(content, repetitions.getContent());

		content = "Noitamina";
		repetitions.setContent(content);
		assertEquals(content, repetitions.getContent());

		try {
			repetitions.setContent("BOOM!");
			fail("Exception not thrown.");
		} catch (IncompatibilityException e) {
		}
	}

	@Test
	public void testIncompatibilityException() {
		Atom letter = new Atom("[a-zA-Z]");
		Repetitions<Atom> repetitions = new Repetitions<Atom>(letter);

		try {
			repetitions.setContent("Test_");
			fail("Exception not thrown");
		} catch (IncompatibilityException e) {
			assertEquals(
					"Incompatible format \"[a-zA-Z]\" at position 4: \"_\"",
					e.getMessage());
		}

		try {
			repetitions.setContent("Test_test");
			fail("Exception not thrown");
		} catch (IncompatibilityException e) {
			assertEquals(
					"Incompatible format \"[a-zA-Z]\" at position 4: \"_\"",
					e.getMessage());
		}

		try {
			repetitions.setContent("_Test");
			fail("Exception not thrown");
		} catch (IncompatibilityException e) {
			assertEquals(
					"Incompatible format \"[a-zA-Z]\" at position 0: \"_\"",
					e.getMessage());
		}

		try {
			repetitions.setContent("Test_test_test");
			fail("Exception not thrown");
		} catch (IncompatibilityException e) {
			assertEquals(
					"Incompatible format \"[a-zA-Z]\" at position 4: \"_\"",
					e.getMessage());
		}
	}

	@Test
	public void testMinMax() {
		Atom letter = new Atom("[a-zA-Z]");
		Repetitions<Atom> repetitions = new Repetitions<Atom>(letter, 3, 5);
		repetitions.setContent("Tes");

		try {
			repetitions.setContent("Testing");
			fail("Exception not thrown");
		} catch (IncompatibilityException e) {
			assertEquals("Incompatible format (empty) at position 5: \"ng\"",
					e.getMessage());
		}

		try {
			repetitions.setContent("Te");
			fail("Exception not thrown");
		} catch (IncompatibilityException e) {
			assertEquals(
					"Incompatible format \"[a-zA-Z]\" at position 2: \"\"",
					e.getMessage());
		}
	}

}
