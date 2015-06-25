package fr.vergne.parsing.layer.standard;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.LayerTest;
import fr.vergne.parsing.layer.exception.ParsingException;

public class OptionTest extends LayerTest {

	@Override
	protected Layer instantiateFilledLayer() {
		Option<Formula> option = new Option<Formula>(new Formula("[abc]+"));
		option.setContent("aabbababcbcbc");
		return option;
	}

	@Override
	protected Layer instantiateFilledLayerwithSpecialCharacters(
			Collection<String> charactersToReuse) {
		StringBuilder builder = new StringBuilder();
		for (String character : charactersToReuse) {
			builder.append(character);
		}
		Option<Formula> option = new Option<Formula>(new Formula("(?s:.+)"));
		option.setContent(builder.toString());
		return option;
	}

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
