package fr.vergne.parsing.layer.standard;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import fr.vergne.parsing.layer.exception.ParsingException;

public class ChoiceTest {

	@Test
	public void testSetGetContent() {
		{
			String content = "test";
			Formula contiguous = new Formula("[a-z]+");
			Formula newline = new Formula("[A-Z\n]+");
			Formula empty = new Formula("");
			Choice choice = new Choice(contiguous, newline, empty);
			choice.setContent(content);
			assertEquals(content, choice.getContent());
		}
		{
			String content = "TEST\nTEST";
			Formula contiguous = new Formula("[a-z]+");
			Formula newline = new Formula("[A-Z\n]+");
			Formula empty = new Formula("");
			Choice choice = new Choice(contiguous, newline, empty);
			choice.setContent(content);
			assertEquals(content, choice.getContent());
		}
		{
			String content = "";
			Formula contiguous = new Formula("[a-z]+");
			Formula newline = new Formula("[A-Z\n]+");
			Formula empty = new Formula("");
			Choice choice = new Choice(contiguous, newline, empty);
			choice.setContent(content);
			assertEquals(content, choice.getContent());
		}
	}

	@Test
	public void testDifferent() {
		{
			Formula contiguous = new Formula("[a-z]+");
			Formula newline = new Formula("[A-Z\n]+");
			Formula empty = new Formula("");
			Choice choice = new Choice(contiguous, newline, empty);
			try {
				choice.setContent("123");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals(
						"Incompatible regex \"(([a-z]+)|([A-Z\\n]+)|())\" for content \"123\"",
						e.getMessage());
			}
		}
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
	public void testCurrent() {
		Formula lower = new Formula("[a-z]+");
		Formula upper = new Formula("[A-Z]+");
		Formula number = new Formula("[0-9]+");
		Atom test = new Atom("T35T");
		Choice choice = new Choice(Arrays.asList(lower, upper, number, test));

		String content = "abc";
		choice.setContent(content);
		assertSame(lower, choice.getCurrent());

		content = "ABC";
		choice.setContent(content);
		assertSame(upper, choice.getCurrent());

		content = "321";
		choice.setContent(content);
		assertSame(number, choice.getCurrent());

		content = "T35T";
		choice.setContent(content);
		assertSame(test, choice.getCurrent());
	}

	@Test
	public void testGetAlternative() {
		Formula lower = new Formula("[a-z]+");
		Formula upper = new Formula("[A-Z]+");
		Formula number = new Formula("[0-9]+");
		Atom test = new Atom("T35T");
		Choice choice = new Choice(Arrays.asList(lower, upper, number, test));

		assertSame(lower, choice.getAlternative(0));
		assertSame(upper, choice.getAlternative(1));
		assertSame(number, choice.getAlternative(2));
		assertSame(test, choice.getAlternative(3));
	}

	@Test
	public void testSize() {
		Formula lower = new Formula("[a-z]+");
		Formula upper = new Formula("[A-Z]+");
		Formula number = new Formula("[0-9]+");
		Atom test = new Atom("T35T");

		assertEquals(0, new Choice().size());
		assertEquals(1, new Choice(Arrays.asList(lower)).size());
		assertEquals(2, new Choice(Arrays.asList(lower, upper)).size());
		assertEquals(3, new Choice(Arrays.asList(lower, upper, number)).size());
		assertEquals(4,
				new Choice(Arrays.asList(lower, upper, number, test)).size());
	}

}
