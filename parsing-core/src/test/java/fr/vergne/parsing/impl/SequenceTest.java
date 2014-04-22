package fr.vergne.parsing.impl;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import fr.vergne.parsing.exception.IncompatibilityException;

public class SequenceTest {

	@Test
	public void testGetContent() {
		String content = "A testing case.";
		Atom word1 = new Atom("[a-zA-Z]+");
		Atom word2 = new Atom("[a-zA-Z]+");
		Atom word3 = new Atom("[a-zA-Z]+");
		StaticAtom space1 = new StaticAtom(" ");
		StaticAtom space2 = new StaticAtom(" ");
		StaticAtom dot = new StaticAtom(".");
		Sequence structure = new Sequence(Arrays.asList(
				word1, space1, word2, space2, word3, dot));
		structure.setContent(content);

		assertEquals(content, structure.getContent());
	}

	@Test
	public void testSetContent() {
		String content = "A testing case.";
		Atom word1 = new Atom("[a-zA-Z]+");
		Atom word2 = new Atom("[a-zA-Z]+");
		Atom word3 = new Atom("[a-zA-Z]+");
		StaticAtom space1 = new StaticAtom(" ");
		StaticAtom space2 = new StaticAtom(" ");
		StaticAtom dot = new StaticAtom(".");
		Sequence structure = new Sequence(Arrays.asList(
				word1, space1, word2, space2, word3, dot));
		structure.setContent(content);

		String content2 = "A running case.";
		structure.setContent(content2);
		assertEquals(content2, structure.getContent());

		try {
			structure.setContent("A too long testing case.");
			fail("Exception not thrown.");
		} catch (IncompatibilityException e) {
		}
	}

	@Test
	public void testParsingException() {
		Atom word1 = new Atom("[a-zA-Z]+");
		Atom word2 = new Atom("[a-zA-Z]+");
		Atom word3 = new Atom("[a-zA-Z]+");
		StaticAtom space1 = new StaticAtom(" ");
		StaticAtom space2 = new StaticAtom(" ");
		StaticAtom dot = new StaticAtom(".");
		Sequence structure = new Sequence(Arrays.asList(
				word1, space1, word2, space2, word3, dot));

		try {
			structure.setContent("A testing case!");
			fail("Exception not thrown");
		} catch (IncompatibilityException e) {
			assertEquals("Incompatible format \"\\Q.\\E\" at position 14: !",
					e.getMessage());
		}

		try {
			structure.setContent("A 3 4 5.");
			fail("Exception not thrown");
		} catch (IncompatibilityException e) {
			assertEquals("Incompatible format \"[a-zA-Z]+\" at position 2: 3 4 5",
					e.getMessage());
		}

		try {
			structure.setContent("A testing c4se.");
			fail("Exception not thrown");
		} catch (IncompatibilityException e) {
			assertEquals("Incompatible format \"[a-zA-Z]+\" at position 10: c4se",
					e.getMessage());
		}

		try {
			structure.setContent("A very very long testing case.");
			fail("Exception not thrown");
		} catch (IncompatibilityException e) {
			assertEquals("Incompatible format \"[a-zA-Z]+\" at position 7: very long testing case",
					e.getMessage());
		}
	}

}
