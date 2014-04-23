package fr.vergne.parsing.layer.impl;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import fr.vergne.parsing.layer.exception.ParsingException;

public class ChoiceTest {

	@Test
	public void testGetContent() {
		Formula lower = new Formula("[a-z]+");
		Formula upper = new Formula("[A-Z]+");
		Formula number = new Formula("[0-9]+");
		Atom test = new Atom("T35T");
		Choice choice = new Choice(Arrays.asList(lower, upper, number, test));

		String content = "abc";
		choice.setContent(content);
		assertEquals(content, choice.getContent());

		content = "ABC";
		choice.setContent(content);
		assertEquals(content, choice.getContent());

		content = "123";
		choice.setContent(content);
		assertEquals(content, choice.getContent());
	}

	@Test
	public void testInnerContentSynchronization() {
		Formula lower = new Formula("[a-z]+");
		Formula upper = new Formula("[A-Z]+");
		Formula number = new Formula("[0-9]+");
		Atom test = new Atom("T35T");
		Choice choice = new Choice(Arrays.asList(lower, upper, number, test));

		String content = "abc";
		choice.setContent(content);
		assertEquals(content, lower.getContent());

		content = "ABC";
		choice.setContent(content);
		assertEquals(content, upper.getContent());

		content = "321";
		choice.setContent(content);
		assertEquals(content, number.getContent());

		content = "T35T";
		choice.setContent(content);
		assertEquals(content, test.getContent());
	}

	@Test
	public void testSetContent() {
		Formula lower = new Formula("[a-z]+");
		Formula upper = new Formula("[A-Z]+");
		Formula number = new Formula("[0-9]+");
		Atom test = new Atom("T35T");
		Choice choice = new Choice(Arrays.asList(lower, upper, number, test));

		String content = "abc";
		choice.setContent(content);

		content = "654";
		choice.setContent(content);
		assertEquals(content, choice.getContent());

		try {
			choice.setContent("123abc");
			fail("Exception not thrown.");
		} catch (ParsingException e) {
		}
	}

	@Test
	public void testParsingException() {
		Formula lower = new Formula("[a-z]+");
		Formula upper = new Formula("[A-Z]+");
		Formula number = new Formula("[0-9]+");
		Atom test = new Atom("T35T");
		Choice choice = new Choice(Arrays.asList(lower, upper, number, test));

		try {
			choice.setContent("abc12");
			fail("Exception not thrown");
		} catch (ParsingException e) {
			assertEquals(
					"Incompatible format \"(?:(?:[a-z]+)|(?:[A-Z]+)|(?:[0-9]+)|(?:\\QT35T\\E))\" at position 0: \"abc12\"",
					e.getMessage());
		}
	}

}
