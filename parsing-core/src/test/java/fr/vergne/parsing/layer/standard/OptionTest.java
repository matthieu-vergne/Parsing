package fr.vergne.parsing.layer.standard;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.vergne.parsing.layer.exception.ParsingException;

public class OptionTest {

	@Test
	public void testSetGetContent() {
		Option<Formula> option = new Option<Formula>(new Formula("[abc]+"));
		{
			String content = "";
			option.setContent(content);
			assertEquals(content, option.getContent());
		}
		{
			String content = "a";
			option.setContent(content);
			assertEquals(content, option.getContent());
		}
		{
			String content = "b";
			option.setContent(content);
			assertEquals(content, option.getContent());
		}
		{
			String content = "abcba";
			option.setContent(content);
			assertEquals(content, option.getContent());
		}
	}

	@Test
	public void testIsPresent() {
		Option<Formula> option = new Option<Formula>(new Formula("[abc]+"));
		{
			String content = "";
			option.setContent(content);
			assertFalse(option.isPresent());
		}
		{
			String content = "a";
			option.setContent(content);
			assertTrue(option.isPresent());
		}
		{
			String content = "b";
			option.setContent(content);
			assertTrue(option.isPresent());
		}
		{
			String content = "abcba";
			option.setContent(content);
			assertTrue(option.isPresent());
		}
	}

	@Test
	public void testDifferent() {
		Option<Formula> option = new Option<Formula>(new Formula("[abc]+"));
		try {
			option.setContent("abd");
		} catch (ParsingException e) {
			assertEquals(
					"Unable to parse Formula[[abc]+] for Formula[[abc]+](opt) from (1,1): \"abd\"",
					e.getMessage());
		}
	}

}
