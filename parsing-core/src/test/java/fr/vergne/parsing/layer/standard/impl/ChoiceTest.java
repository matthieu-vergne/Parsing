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
import fr.vergne.parsing.definition.impl.SimpleDefinition;
import fr.vergne.parsing.definition.impl.StandardDefinitionFactory;
import fr.vergne.parsing.definition.impl.StandardDefinitionFactory.DefinitionProxy;
import fr.vergne.parsing.layer.ComposedLayerTest;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.standard.impl.Choice.InvalidChoiceException;

@RunWith(JUnitPlatform.class)
public class ChoiceTest implements ComposedLayerTest<Choice> {

	private final StandardDefinitionFactory factory = new StandardDefinitionFactory();

	@Override
	public Map<String, Choice> instantiateLayers(Collection<String> specialCharacters) {
		Collection<Definition<?>> alternatives = new LinkedList<>();
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		for (String content : specialCharacters) {
			alternatives.add(factory.defineConstant(content));
		}
		Choice choice = new Choice(alternatives);

		Map<String, Choice> map = new HashMap<String, Choice>();
		for (String content : specialCharacters) {
			map.put(content, choice);
		}
		return map;
	}

	@Override
	public Collection<Layer> getUsedSubLayers(Choice choice) {
		return Arrays.asList(choice.get());
	}
	
	@Override
	public Layer instantiateRecursiveLayer() {
		DefinitionProxy<Choice> choiceProxy = factory.prepareDefinition();
		Definition<Choice> choice = choiceProxy.getDefinition();
		choiceProxy.defineAs(factory.defineChoice(UnsafeRecursiveLayer.defineOn(choice), factory.defineConstant("")));
		return choice.create();
	}
	
	@Override
	public String getValidRecursiveContent(Layer layer) {
		return "-----";
	}

	@Test
	public void testSetGetContent() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> contiguous = factory.defineRegex("[a-z]+");
		Definition<Regex> newline = factory.defineRegex("[A-Z\n]+");
		Definition<Constant> empty = factory.defineConstant("");
		{
			String content = "test";
			Choice choice = new Choice(contiguous, newline, empty);
			choice.setContent(content);
			assertEquals(content, choice.getContent());
		}
		{
			String content = "TEST\nTEST";
			Choice choice = new Choice(contiguous, newline, empty);
			choice.setContent(content);
			assertEquals(content, choice.getContent());
		}
		{
			String content = "";
			Choice choice = new Choice(contiguous, newline, empty);
			choice.setContent(content);
			assertEquals(content, choice.getContent());
		}
	}

	@Test
	public void testDifferent() {
		{
			Definition<Regex> contiguous = factory.defineRegex("[a-z]+");
			Definition<Regex> newline = factory.defineRegex("[A-Z\n]+");
			Definition<Regex> empty = factory.defineRegex("");
			Choice choice = new Choice(contiguous, newline, empty);
			try {
				choice.setContent("123");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Incompatible regex \"(([a-z]+)|([A-Z\\n]+)|())\" for content \"123\"", e.getMessage());
			}
		}
	}

	@Test
	public void testIsProperLayer() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> lower = factory.defineRegex("[a-z]+");
		Definition<Regex> upper = factory.defineRegex("[A-Z]+");
		Definition<Regex> number = factory.defineRegex("[0-9]+");
		Choice choice = new Choice(lower, upper, number);

		choice.setContent("abc");
		assertTrue(choice.is(lower));

		choice.setContent("ABC");
		assertTrue(choice.is(upper));

		choice.setContent("123");
		assertTrue(choice.is(number));
	}

	@Test
	public void testGetLayerWithCorrectContent() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> lower = factory.defineRegex("[a-z]+");
		Definition<Regex> upper = factory.defineRegex("[A-Z]+");
		Definition<Regex> number = factory.defineRegex("[0-9]+");
		Choice choice = new Choice(lower, upper, number);

		choice.setContent("abc");
		assertEquals("abc", choice.get().getContent());

		choice.setContent("ABC");
		assertEquals("ABC", choice.get().getContent());

		choice.setContent("321");
		assertEquals("321", choice.get().getContent());
	}

	@Test
	public void testGetCorrectLayer() {
		Collection<Regex> lowerInstances = new LinkedList<>();
		Definition<Regex> lower = new SimpleDefinition<>(() -> {
			Regex instance = new Regex("[a-z]+");
			lowerInstances.add(instance);
			return instance;
		}, Regex::getRegex);

		Collection<Regex> upperInstances = new LinkedList<>();
		Definition<Regex> upper = new SimpleDefinition<>(() -> {
			Regex instance = new Regex("[A-Z]+");
			upperInstances.add(instance);
			return instance;
		}, Regex::getRegex);

		Choice choice = new Choice(lower, upper);

		choice.setContent("abc");
		assertTrue(lowerInstances.contains(choice.get()));
		assertFalse(upperInstances.contains(choice.get()));

		choice.setContent("ABC");
		assertFalse(lowerInstances.contains(choice.get()));
		assertTrue(upperInstances.contains(choice.get()));
	}

	@Test
	public void testGetAsCorrectLayer() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> lower = factory.defineRegex("[a-z]+");
		Definition<Regex> upper = factory.defineRegex("[A-Z]+");
		Definition<Regex> number = factory.defineRegex("[0-9]+");
		Choice choice = new Choice(lower, upper, number);

		choice.setContent("abc");
		assertSame(choice.get(), choice.getAs(lower));

		choice.setContent("ABC");
		assertSame(choice.get(), choice.getAs(upper));

		choice.setContent("123");
		assertSame(choice.get(), choice.getAs(number));
	}

	@Test
	public void testGetAsRejectsInvalidLayer() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> lower = factory.defineRegex("[a-z]+");
		Definition<Regex> upper = factory.defineRegex("[A-Z]+");
		Definition<Regex> number = factory.defineRegex("[0-9]+");
		Choice choice = new Choice(lower, upper, number);

		choice.setContent("ABC");
		try {
			choice.getAs(lower);
			fail("No exception thrown");
		} catch (InvalidChoiceException cause) {
			// OK
		}

		try {
			choice.getAs(number);
			fail("No exception thrown");
		} catch (InvalidChoiceException cause) {
			// OK
		}
	}

	@Test
	public void testGetCurrentDefinitionCorrespondsToContent() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> lower = factory.defineRegex("[a-z]+");
		Definition<Regex> upper = factory.defineRegex("[A-Z]+");
		Definition<Regex> number = factory.defineRegex("[0-9]+");
		Choice choice = new Choice(lower, upper, number);

		String content = "abc";
		choice.setContent(content);
		assertSame(lower, choice.getCurrentDefinition());

		content = "ABC";
		choice.setContent(content);
		assertSame(upper, choice.getCurrentDefinition());

		content = "321";
		choice.setContent(content);
		assertSame(number, choice.getCurrentDefinition());
	}

	@Test
	public void testGetDefinitionReturnsCorrectDefinition() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> lower = factory.defineRegex("[a-z]+");
		Definition<Regex> upper = factory.defineRegex("[A-Z]+");
		Definition<Regex> number = factory.defineRegex("[0-9]+");
		Definition<Constant> test = factory.defineConstant("T35T");
		Choice choice = new Choice(lower, upper, number, test);

		assertSame(lower, choice.getDefinition(0));
		assertSame(upper, choice.getDefinition(1));
		assertSame(number, choice.getDefinition(2));
		assertSame(test, choice.getDefinition(3));
	}

	@Test
	public void testGetDefinitionsReturnsProvidedDefinitions() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> lower = factory.defineRegex("[a-z]+");
		Definition<Regex> upper = factory.defineRegex("[A-Z]+");
		Definition<Regex> number = factory.defineRegex("[0-9]+");
		Choice choice = new Choice(lower, upper, number);

		String content = choice.getDefinitions().toString();
		assertEquals(content, 3, choice.getDefinitions().size());
		assertTrue(content, choice.getDefinitions().contains(lower));
		assertTrue(content, choice.getDefinitions().contains(upper));
		assertTrue(content, choice.getDefinitions().contains(number));
	}

	@Test
	public void testSizeReturnsNumberOfChoices() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> lower = factory.defineRegex("[a-z]+");
		Definition<Regex> upper = factory.defineRegex("[A-Z]+");
		Definition<Regex> number = factory.defineRegex("[0-9]+");
		Definition<Constant> test = factory.defineConstant("T35T");

		assertEquals(0, new Choice().size());
		assertEquals(1, new Choice(lower).size());
		assertEquals(2, new Choice(lower, upper).size());
		assertEquals(3, new Choice(lower, upper, number).size());
		assertEquals(4, new Choice(lower, upper, number, test).size());
	}

	@Test
	public void testReferenceDefinition() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> partial = factory.defineRegex("[a-z]");
		Definition<Regex> lessPartial = factory.defineRegex("[A-Z][a-z]");
		Definition<Regex> complete = factory.defineRegex("[A-Z][a-z][a-z]");
		Choice choice = new Choice(partial, lessPartial, complete);

		String content = "123";
		for (Definition<Regex> definition1 : Arrays.asList(partial, lessPartial, complete)) {
			try {
				definition1.create().setContent(content);
				fail("No exception thrown, change the content to throw one.");
			} catch (ParsingException e1) {
				for (Definition<Regex> definition2 : Arrays.asList(partial, lessPartial, complete)) {
					try {
						definition2.create().setContent(content);
						fail("No exception thrown, change the content to throw one.");
					} catch (ParsingException e2) {
						assertEquals(definition1.equals(definition2), e1.getMessage().equals(e2.getMessage()));
					}
				}
			}
		}

		try {
			choice.setContent(content);
			fail("No exception thrown.");
		} catch (ParsingException e1) {
			for (Definition<Regex> definition : Arrays.asList(partial, lessPartial, complete)) {
				try {
					definition.create().setContent(content);
					fail("No exception thrown while its parent throws one.");
				} catch (ParsingException e2) {
					assertNull(e1.getCause());
				}
			}
		}

		choice.setReferenceDefinition(partial);
		assertSame(partial, choice.getReferenceDefinition());
		try {
			choice.setContent(content);
			fail("No exception thrown.");
		} catch (ParsingException e1) {
			for (Definition<Regex> definition : Arrays.asList(partial, lessPartial, complete)) {
				try {
					definition.create().setContent(content);
					fail("No exception thrown while its parent throws one.");
				} catch (ParsingException e2) {
					assertEquals(partial.equals(definition), e1.getCause().getMessage().equals(e2.getMessage()));
				}
			}
		}

		choice.setReferenceDefinition(lessPartial);
		assertSame(lessPartial, choice.getReferenceDefinition());
		try {
			choice.setContent(content);
			fail("No exception thrown.");
		} catch (ParsingException e1) {
			for (Definition<Regex> definition : Arrays.asList(partial, lessPartial, complete)) {
				try {
					definition.create().setContent(content);
					fail("No exception thrown while its parent throws one.");
				} catch (ParsingException e2) {
					assertEquals(lessPartial.equals(definition), e1.getCause().getMessage().equals(e2.getMessage()));
				}
			}
		}

		choice.setReferenceDefinition(complete);
		assertSame(complete, choice.getReferenceDefinition());
		try {
			choice.setContent(content);
			fail("No exception thrown.");
		} catch (ParsingException e1) {
			for (Definition<Regex> definition : Arrays.asList(partial, lessPartial, complete)) {
				try {
					definition.create().setContent(content);
					fail("No exception thrown while its parent throws one.");
				} catch (ParsingException e2) {
					assertEquals(complete.equals(definition), e1.getCause().getMessage().equals(e2.getMessage()));
				}
			}
		}
	}

}
