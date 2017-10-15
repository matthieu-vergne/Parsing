package fr.vergne.parsing.layer.standard.impl;

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
import fr.vergne.parsing.definition.impl.StandardDefinitionFactory;
import fr.vergne.parsing.definition.impl.StandardDefinitionFactory.DefinitionProxy;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.Layer.ContentListener;
import fr.vergne.parsing.layer.ModifiableComposedLayerTest;
import fr.vergne.parsing.layer.exception.ParsingException;

@RunWith(JUnitPlatform.class)
public class OptionTest implements ModifiableComposedLayerTest<Option<Regex>> {

	private final StandardDefinitionFactory factory = new StandardDefinitionFactory();

	@Override
	public Map<String, Option<Regex>> instantiateLayers(Collection<String> specialCharacters) {
		StringBuilder builder = new StringBuilder();
		for (String character : specialCharacters) {
			builder.append(character);
		}
		Option<Regex> option = new Option<Regex>(factory.defineRegex("(?s:.+)"));

		Map<String, Option<Regex>> map = new HashMap<>();
		map.put(builder.toString(), option);
		return map;
	}

	@Override
	public Collection<Layer> getUsedSubLayers(Option<Regex> option) {
		return Arrays.asList(option.getOption());
	}

	@Override
	public Collection<Update> getSublayersUpdatesFunctions(Option<Regex> option) {
		Collection<Update> updates = new LinkedList<>();

		if (option.isPresent()) {
			updates.add(new Update() {

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
		DefinitionProxy<Option<UnsafeRecursiveLayer>> optionProxy = factory.prepareDefinition();
		Definition<Option<UnsafeRecursiveLayer>> option = optionProxy.getDefinition();
		optionProxy.defineAs(factory.defineOptional(UnsafeRecursiveLayer.defineOn(option)));

		return option.create();
	}

	@Override
	public String getValidRecursiveContent(Layer layer) {
		return "-----";
	}

	@Test
	public void testSetGetContent() {
		Option<Regex> option = new Option<Regex>(factory.defineRegex("[abc]+"));
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
		Option<Regex> option = new Option<Regex>(factory.defineRegex("[abc]+"));
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
		Definition<Regex> regex = factory.defineRegex("[abc]+");
		Option<Regex> option = new Option<Regex>(regex);
		try {
			option.setContent("abd");
		} catch (ParsingException e) {
			assertEquals("Unable to parse REGEX[[abc]+] for OPT[REGEX] from (1,1): \"abd\"", e.getMessage());
		}
	}

	@Test
	public void testSetContentNotifiesListeners() {
		Option<Regex> option = new Option<Regex>(factory.defineRegex("[abc]+"));
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
		Definition<Regex> regex = factory.defineRegex("[abc]+");
		Option<Regex> option = new Option<Regex>(regex);
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
		Option<Regex> option = new Option<Regex>(factory.defineRegex("[abc]+"));
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
		Definition<Regex> definition = factory.defineRegex("[abc]+");
		Option<Regex> option = new Option<Regex>(definition);

		assertEquals(definition, option.getOptionalDefinition());
	}
}
