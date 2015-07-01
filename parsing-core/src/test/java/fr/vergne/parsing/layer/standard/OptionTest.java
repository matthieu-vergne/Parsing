package fr.vergne.parsing.layer.standard;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.Layer.ContentListener;
import fr.vergne.parsing.layer.LayerTest;
import fr.vergne.parsing.layer.exception.ParsingException;

public class OptionTest extends LayerTest {

	@Override
	protected Map<String, Layer> instantiateLayers(
			Collection<String> specialCharacters) {
		StringBuilder builder = new StringBuilder();
		for (String character : specialCharacters) {
			builder.append(character);
		}
		Option<Formula> option = new Option<Formula>(new Formula("(?s:.+)"));

		Map<String, Layer> map = new HashMap<String, Layer>();
		map.put(builder.toString(), option);
		return map;
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

	@Test
	public void testSetContentNotifiesListeners() {
		Option<Formula> option = new Option<Formula>(new Formula("[abc]+"));
		final String[] value = new String[] { null };
		option.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		option.setContent("abba");
		assertEquals(option.getContent(), value[0]);
		option.setContent("");
		assertEquals(option.getContent(), value[0]);
	}

	@Test
	public void testPresentOptionUpdateNotifiesListeners() {
		Option<Formula> option = new Option<Formula>(new Formula("[abc]+"));
		option.setContent("abba");
		final String[] value = new String[] { null };
		option.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		option.getOption().setContent("baab");
		assertEquals(option.getOption().getContent(), value[0]);
	}

	@Test
	public void testAbsentOptionUpdateDoesNotNotifyListeners() {
		Option<Formula> option = new Option<Formula>(new Formula("[abc]+"));
		option.setContent("");
		final String[] value = new String[] { null };
		option.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		option.getOption().setContent("baab");
		assertEquals(null, value[0]);
	}

}
