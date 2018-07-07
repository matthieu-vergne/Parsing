package fr.vergne.parsing.layer.standard;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.definition.Definition.DefinitionProxy;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.Layer.ContentListener;
import fr.vergne.parsing.layer.ModifiableComposedLayerTest;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.standard.impl.UnsafeRecursiveLayer;

// TODO Add quantifier tests?
@RunWith(JUnitPlatform.class)
public class OptionTest implements ModifiableComposedLayerTest<Option<Regex>> {

	@Override
	public Map<String, Option<Regex>> instantiateLayers(Collection<String> specialCharacters) {
		StringBuilder builder = new StringBuilder();
		for (String character : specialCharacters) {
			builder.append(character);
		}
		Option<Regex> option = new Option<>(Regex.define("(?s:.+)"));

		Map<String, Option<Regex>> map = new HashMap<>();
		map.put(builder.toString(), option);
		return map;
	}

	@Override
	public Collection<Layer> getUsedSubLayers(Option<Regex> option) {
		return Arrays.asList(option.getOption());
	}

	@Override
	public Collection<SublayerUpdate> getSublayersUpdates(Option<Regex> parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<SublayerReplacement> getSublayersReplacements(Option<Regex> option) {
		Collection<SublayerReplacement> updates = new LinkedList<>();

		if (option.isPresent()) {
			updates.add(new SublayerReplacement() {

				Regex current = option.getOption();
				Regex replacement = option.getOptionalDefinition().create();

				@Override
				public Layer getInitial() {
					return current;
				}

				@Override
				public Layer getReplacement() {
					replacement.setContent(current.getContent());
					return replacement;
				}

				@Override
				public void execute() {
					option.setOption(replacement);
				}

				@Override
				public void revert() {
					option.setOption(current);
				}
			});
		} else {
			// Irrelevant updates
		}
		return updates;
	}

	@Override
	public Option<UnsafeRecursiveLayer> instantiateRecursiveLayer() {
		DefinitionProxy<Option<UnsafeRecursiveLayer>> option = Definition.prepare();
		option.setDelegate(Option.define(UnsafeRecursiveLayer.defineOn(option)));

		return option.create();
	}

	@Override
	public String getValidRecursiveContent(Layer layer) {
		return "-----";
	}

	@Test
	public void testSetGetContent() {
		Option<Regex> option = new Option<>(Regex.define("[abc]+"));
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
		Option<Regex> option = new Option<>(Regex.define("[abc]+"));
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
		Definition<Regex> regex = Regex.define("[abc]+");
		Option<Regex> option = new Option<>(regex);
		try {
			option.setContent("abd");
		} catch (ParsingException e) {
			assertEquals("Unable to parse REGEX[[abc]+] for OPT[REGEX] from (1,1): \"abd\"", e.getMessage());
		}
	}

	@Test
	public void testSetContentNotifiesListeners() {
		Option<Regex> option = new Option<>(Regex.define("[abc]+"));
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
	public void testSetOptionNotifiesListeners() {
		Definition<Regex> regex = Regex.define("[abc]+");
		Option<Regex> option = new Option<>(regex);
		final String[] value = new String[] { null };
		option.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		Regex element = regex.create();
		element.setContent("aaa");
		option.setOption(element);
		assertEquals("aaa", value[0]);

		element = regex.create();
		element.setContent("ccc");
		option.setOption(element);
		assertEquals("ccc", value[0]);
	}

	@Test
	public void testOptionDirectUpdateNotifiesListeners() {
		Option<Regex> option = new Option<>(Regex.define("[abc]+"));
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
	public void testGetCorrectOptionalDefinition() {
		Definition<Regex> definition = Regex.define("[abc]+");
		Option<Regex> option = new Option<>(definition);

		assertEquals(definition, option.getOptionalDefinition());
	}
}
