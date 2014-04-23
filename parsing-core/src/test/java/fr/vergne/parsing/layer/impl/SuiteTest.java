package fr.vergne.parsing.layer.impl;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import fr.vergne.parsing.layer.exception.ParsingException;

public class SuiteTest {

	@Test
	public void testRegex() {
		Formula word1 = new Formula("[a-zA-Z]+");
		Formula word2 = new Formula("[0-9]+");
		Formula word3 = new Formula("[a-zA-Z0-9]+");
		Atom space1 = new Atom(" ");
		Atom space2 = new Atom(" ");
		Atom dot = new Atom(".");
		Suite suite = new Suite(Arrays.asList(word1, space1, word2, space2,
				word3, dot));
		assertEquals(word1.getRegex() + space1.getRegex() + word2.getRegex()
				+ space2.getRegex() + word3.getRegex() + dot.getRegex(),
				suite.getRegex());
	}

	@Test
	public void testGetContent() {
		String content = "A testing case.";
		Formula word1 = new Formula("[a-zA-Z]+");
		Formula word2 = new Formula("[a-zA-Z]+");
		Formula word3 = new Formula("[a-zA-Z]+");
		Atom space1 = new Atom(" ");
		Atom space2 = new Atom(" ");
		Atom dot = new Atom(".");
		Suite suite = new Suite(Arrays.asList(word1, space1, word2, space2,
				word3, dot));
		suite.setContent(content);

		assertEquals(content, suite.getContent());
	}

	@Test
	public void testInnerContentSynchronization() {
		String content = "A testing case.";
		Formula word1 = new Formula("[a-zA-Z]+");
		Formula word2 = new Formula("[a-zA-Z]+");
		Formula word3 = new Formula("[a-zA-Z]+");
		Atom space1 = new Atom(" ");
		Atom space2 = new Atom(" ");
		Atom dot = new Atom(".");
		Suite suite = new Suite(Arrays.asList(word1, space1, word2, space2,
				word3, dot));
		suite.setContent(content);

		assertEquals("A", word1.getContent());
		assertEquals("testing", word2.getContent());
		assertEquals("case", word3.getContent());

		word1.setContent("Another");
		word2.setContent("running");
		assertEquals("Another", word1.getContent());
		assertEquals("running", word2.getContent());
		assertEquals("case", word3.getContent());
		assertEquals("Another running case.", suite.getContent());
	}

	@Test
	public void testSetContent() {
		String content = "A testing case.";
		Formula word1 = new Formula("[a-zA-Z]+");
		Formula word2 = new Formula("[a-zA-Z]+");
		Formula word3 = new Formula("[a-zA-Z]+");
		Atom space1 = new Atom(" ");
		Atom space2 = new Atom(" ");
		Atom dot = new Atom(".");
		Suite suite = new Suite(Arrays.asList(word1, space1, word2, space2,
				word3, dot));
		suite.setContent(content);

		String content2 = "A running case.";
		suite.setContent(content2);
		assertEquals(content2, suite.getContent());

		try {
			suite.setContent("A too long testing case.");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
		}
	}

	@Test
	public void testParsingException() {
		Formula word1 = new Formula("[a-zA-Z]+");
		Formula word2 = new Formula("[a-zA-Z]+");
		Formula word3 = new Formula("[a-zA-Z]+");
		Atom space1 = new Atom(" ");
		Atom space2 = new Atom(" ");
		Atom dot = new Atom(".");
		Suite suite = new Suite(Arrays.asList(word1, space1, word2, space2,
				word3, dot));

		try {
			suite.setContent("A testing case!");
			fail("Exception not thrown");
		} catch (ParsingException e) {
			assertEquals(
					"Incompatible format \"\\Q.\\E\" at position 14: \"!\"",
					e.getMessage());
		}

		try {
			suite.setContent("A 3 4 5.");
			fail("Exception not thrown");
		} catch (ParsingException e) {
			assertEquals(
					"Incompatible format \"[a-zA-Z]+\" at position 2: \"3 4 5\"",
					e.getMessage());
		}

		try {
			suite.setContent("A testing c4se.");
			fail("Exception not thrown");
		} catch (ParsingException e) {
			assertEquals(
					"Incompatible format \"[a-zA-Z]+\" at position 10: \"c4se\"",
					e.getMessage());
		}

		try {
			suite.setContent("A very very long testing case.");
			fail("Exception not thrown");
		} catch (ParsingException e) {
			assertEquals(
					"Incompatible format \"[a-zA-Z]+\" at position 7: \"very long testing case\"",
					e.getMessage());
		}
	}

}
