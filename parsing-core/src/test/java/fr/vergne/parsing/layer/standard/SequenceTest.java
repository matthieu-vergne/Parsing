package fr.vergne.parsing.layer.standard;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.Layer.ContentListener;
import fr.vergne.parsing.layer.Layer.NoContentException;
import fr.vergne.parsing.layer.ModifiableComposedLayerTest;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.standard.impl.JavaPatternRegex;
import fr.vergne.parsing.layer.standard.impl.StandardDefinitionFactory;
import fr.vergne.parsing.layer.standard.impl.UnsafeRecursiveLayer;
import fr.vergne.parsing.layer.standard.impl.StandardDefinitionFactory.DefinitionProxy;

@RunWith(JUnitPlatform.class)
public interface SequenceTest extends ModifiableComposedLayerTest<Sequence> {

	Sequence instantiateSequence(List<Definition<?>> definitions);

	default Sequence instantiateSequence(Definition<?>... definitions) {
		return instantiateSequence(Arrays.asList(definitions));
	}

	@Override
	default Map<String, Sequence> instantiateLayers(Collection<String> specialCharacters) {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		StringBuilder builder = new StringBuilder();
		List<Definition<?>> definitions = new LinkedList<>();
		for (String character : specialCharacters) {
			builder.append(character);
			definitions.add(factory.defineConstant(character));
		}
		Sequence sequence = instantiateSequence(definitions);

		Map<String, Sequence> map = new HashMap<>();
		map.put(builder.toString(), sequence);
		return map;
	}

	@Override
	default Collection<Layer> getUsedSubLayers(Sequence sequence) {
		Collection<Layer> sublayers = new LinkedList<>();
		for (int i = 0; i < sequence.size(); i++) {
			sublayers.add(sequence.get(i));
		}
		return sublayers;
	}
	
	@Override
	default Collection<SublayerUpdate> getSublayersUpdates(Sequence parent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	default Collection<SublayerReplacement> getSublayersReplacements(Sequence sequence) {
		Collection<SublayerReplacement> updates = new LinkedList<>();

		// Set through index
		updates.add(new SublayerReplacement() {

			int index = 2;
			Layer initial = sequence.get(index);
			Layer replacement = sequence.getDefinition(index).create();

			@Override
			public Layer getInitial() {
				return initial;
			}

			@Override
			public Layer getReplacement() {
				replacement.setContent(initial.getContent());
				return replacement;
			}

			@Override
			public void execute() {
				sequence.set(index, replacement);
			}

			@Override
			public void revert() {
				sequence.set(index, initial);
			}
		});

		// Set through definition
		updates.add(new SublayerReplacement() {

			@SuppressWarnings("unchecked")
			Definition<Layer> definition = (Definition<Layer>) sequence.getDefinition(2);
			Layer initial = sequence.get(definition);
			Layer replacement = definition.create();

			@Override
			public Layer getInitial() {
				return initial;
			}

			@Override
			public Layer getReplacement() {
				replacement.setContent(initial.getContent());
				return replacement;
			}

			@Override
			public void execute() {
				sequence.set(definition, replacement);
			}

			@Override
			public void revert() {
				sequence.set(definition, initial);
			}
		});

		return updates;
	}

	@Override
	default Sequence instantiateRecursiveLayer() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		DefinitionProxy<Sequence> sequenceProxy = factory.prepareDefinition();
		Definition<Sequence> sequence = sequenceProxy.getDefinition();
		sequenceProxy.defineAs(factory.defineSequence(UnsafeRecursiveLayer.defineOn(sequence)));

		return sequence.create();
	}

	@Override
	default String getValidRecursiveContent(Layer layer) {
		return "-----";
	}

	@Test
	default void testGetContentEqualsSetContent1() {
		String content = "test";
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> letter1 = factory.defineRegex("[a-zA-Z]");
		Definition<Constant> letter2 = factory.defineConstant("e");
		Definition<Constant> letter3 = factory.defineConstant("s");
		Definition<Regex> letter4 = factory.defineRegex("[a-zA-Z]");
		Sequence sequence = instantiateSequence(letter1, letter2, letter3, letter4);
		sequence.setContent(content);
		assertEquals(content, sequence.getContent());
	}

	@Test
	default void testGetContentEqualsSetContent2() {
		String content = "test\ntest";
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word1 = factory.defineRegex("[a-z]{4}");
		Definition<Regex> space = factory.defineRegex("\\s+");
		Definition<Regex> word2 = factory.defineRegex("[a-z]{4}");
		Sequence sequence = instantiateSequence(word1, space, word2);
		sequence.setContent(content);
		assertEquals(content, sequence.getContent());
	}

	@Test
	default void testGetContentEqualsSetContentWithEmptySublayers() {
		String content = "test";
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word1 = factory.defineRegex("");
		Definition<Constant> word2 = factory.defineConstant("test");
		Definition<Regex> word3 = factory.defineRegex("");
		Sequence sequence = instantiateSequence(word1, word2, word3);
		sequence.setContent(content);
		assertEquals(content, sequence.getContent());
	}

	@Test
	default void testSizeEqualsNumberOfSubLayers() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-z]+");
		assertEquals(1, instantiateSequence(word).size());
		assertEquals(2, instantiateSequence(word, word).size());
		assertEquals(3, instantiateSequence(word, word, word).size());
	}

	@Test
	default void testDifferent() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		{
			Definition<Regex> letter1 = factory.defineRegex("[a-zA-Z]");
			Definition<Constant> letter2 = factory.defineConstant("e");
			Definition<Constant> letter3 = factory.defineConstant("s");
			Definition<Regex> letter4 = factory.defineRegex("[a-zA-Z]");
			Sequence sequence = instantiateSequence(letter1, letter2, letter3, letter4);
			try {
				sequence.setContent("1esr");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter1.create() + " for " + sequence + " from (1,1): \"1esr\"",
						e.getMessage());
			}
			try {
				sequence.setContent("taxi");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter2.create() + " for " + sequence + " from (1,2): \"axi\"",
						e.getMessage());
			}
			try {
				sequence.setContent("text");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter3.create() + " for " + sequence + " from (1,3): \"xt\"",
						e.getMessage());
			}
			try {
				sequence.setContent("tes1");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter4.create() + " for " + sequence + " from (1,4): \"1\"",
						e.getMessage());
			}
		}
		{
			Definition<Regex> word1 = factory.defineRegex("[a-z]{4}");
			Definition<Regex> space = factory.defineRegex("\\s+");
			Definition<Regex> word2 = factory.defineRegex("[a-z]{4}");
			Sequence sequence = instantiateSequence(word1, space, word2);
			try {
				sequence.setContent("abc\ndef");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word1.create() + " for " + sequence + " from (1,1): \"abc\\ndef\"",
						e.getMessage());
			}
			try {
				sequence.setContent("test-def");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + space.create() + " for " + sequence + " from (1,5): \"-def\"",
						e.getMessage());
			}
			try {
				sequence.setContent("test\ntex");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2.create() + " for " + sequence + " from (2,1): \"tex\"",
						e.getMessage());
			}
		}
		{
			Definition<Regex> word1 = factory.defineRegex("");
			Definition<Constant> word2 = factory.defineConstant("test");
			Definition<Regex> word3 = factory.defineRegex("");
			Sequence sequence = instantiateSequence(word1, word2, word3);
			try {
				sequence.setContent("abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2.create() + " for " + sequence + " from (1,1): \"abc\"",
						e.getMessage());
			}
		}
	}

	@Test
	default void testTooLongOnRight() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		{
			String content = "test";
			Definition<Regex> letter1 = factory.defineRegex("[a-zA-Z]");
			Definition<Constant> letter2 = factory.defineConstant("e");
			Definition<Constant> letter3 = factory.defineConstant("s");
			Definition<Regex> letter4 = factory.defineRegex("[a-zA-Z]");
			Sequence sequence = instantiateSequence(letter1, letter2, letter3, letter4);
			try {
				sequence.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter4.create() + " for " + sequence + " from (1,4): \"tabc\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Definition<Regex> word1 = factory.defineRegex("[a-z]{4}");
			Definition<Regex> space = factory.defineRegex("\\s+");
			Definition<Regex> word2 = factory.defineRegex("[a-z]{4}");
			Sequence sequence = instantiateSequence(word1, space, word2);
			try {
				sequence.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2.create() + " for " + sequence + " from (2,1): \"testabc\"",
						e.getMessage());
			}
		}
		{
			String content = "test";
			Definition<Regex> word1 = factory.defineRegex("");
			Definition<Constant> word2 = factory.defineConstant("test");
			Definition<Regex> word3 = factory.defineRegex("");
			Sequence sequence = instantiateSequence(word1, word2, word3);
			try {
				sequence.setContent(content + "abc");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word3.create() + " for " + sequence + " from (1,5): \"abc\"",
						e.getMessage());
			}
		}
	}

	@Test
	default void testTooLongOnLeft() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		{
			String content = "test";
			Definition<Regex> letter1 = factory.defineRegex("[a-zA-Z]");
			Definition<Constant> letter2 = factory.defineConstant("e");
			Definition<Constant> letter3 = factory.defineConstant("s");
			Definition<Regex> letter4 = factory.defineRegex("[a-zA-Z]");
			Sequence sequence = instantiateSequence(letter1, letter2, letter3, letter4);
			try {
				sequence.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter2.create() + " for " + sequence + " from (1,2): \"bctest\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Definition<Regex> word1 = factory.defineRegex("[a-z]{4}");
			Definition<Regex> space = factory.defineRegex("\\s+");
			Definition<Regex> word2 = factory.defineRegex("[a-z]{4}");
			Sequence sequence = instantiateSequence(word1, space, word2);
			try {
				sequence.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + space.create() + " for " + sequence + " from (1,5): \"est\\ntest\"",
						e.getMessage());
			}
		}
		{
			String content = "test";
			Definition<Regex> word1 = factory.defineRegex("");
			Definition<Constant> word2 = factory.defineConstant("test");
			Definition<Regex> word3 = factory.defineRegex("");
			Sequence sequence = instantiateSequence(word1, word2, word3);
			try {
				sequence.setContent("abc" + content);
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2.create() + " for " + sequence + " from (1,1): \"abctest\"",
						e.getMessage());
			}
		}
	}

	@Test
	default void testTooLongOnMiddle() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		{
			Definition<Regex> letter1 = factory.defineRegex("[a-zA-Z]");
			Definition<Constant> letter2 = factory.defineConstant("e");
			Definition<Constant> letter3 = factory.defineConstant("s");
			Definition<Regex> letter4 = factory.defineRegex("[a-zA-Z]");
			Sequence sequence = instantiateSequence(letter1, letter2, letter3, letter4);
			try {
				sequence.setContent("teabcst");
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter3.create() + " for " + sequence + " from (1,3): \"abcst\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Definition<Regex> word1 = factory.defineRegex("[a-z]{4}");
			Definition<Regex> space = factory.defineRegex("\\s+");
			Definition<Regex> word2 = factory.defineRegex("[a-z]{4}");
			Sequence sequence = instantiateSequence(word1, space, word2);
			try {
				sequence.setContent(content.substring(0, 2) + "abc" + content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + space.create() + " for " + sequence + " from (1,5): \"cst\\ntest\"",
						e.getMessage());
			}
			try {
				sequence.setContent(content.substring(0, 6) + "abc" + content.substring(6));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2.create() + " for " + sequence + " from (2,1): \"tabcest\"",
						e.getMessage());
			}
		}
	}

	@Test
	default void testTooShortOnRight() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		{
			String content = "test";
			Definition<Regex> letter1 = factory.defineRegex("[a-zA-Z]");
			Definition<Constant> letter2 = factory.defineConstant("e");
			Definition<Constant> letter3 = factory.defineConstant("s");
			Definition<Regex> letter4 = factory.defineRegex("[a-zA-Z]");
			Sequence sequence = instantiateSequence(letter1, letter2, letter3, letter4);
			try {
				sequence.setContent(content.substring(0, 2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter3.create() + " for " + sequence + " from (1,3): \"\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Definition<Regex> word1 = factory.defineRegex("[a-z]{4}");
			Definition<Regex> space = factory.defineRegex("\\s+");
			Definition<Regex> word2 = factory.defineRegex("[a-z]{4}");
			Sequence sequence = instantiateSequence(word1, space, word2);
			try {
				sequence.setContent(content.substring(0, 2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word1.create() + " for " + sequence + " from (1,1): \"te\"",
						e.getMessage());
			}
			try {
				sequence.setContent(content.substring(0, 4));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + space.create() + " for " + sequence + " from (1,5): \"\"",
						e.getMessage());
			}
			try {
				sequence.setContent(content.substring(0, 5));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2.create() + " for " + sequence + " from (2,1): \"\"",
						e.getMessage());
			}
			try {
				sequence.setContent(content.substring(0, 7));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2.create() + " for " + sequence + " from (2,1): \"te\"",
						e.getMessage());
			}
		}
	}

	@Test
	default void testTooShortOnLeft() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		{
			String content = "test";
			Definition<Regex> letter1 = factory.defineRegex("[a-zA-Z]");
			Definition<Constant> letter2 = factory.defineConstant("e");
			Definition<Constant> letter3 = factory.defineConstant("s");
			Definition<Regex> letter4 = factory.defineRegex("[a-zA-Z]");
			Sequence sequence = instantiateSequence(letter1, letter2, letter3, letter4);
			try {
				sequence.setContent(content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter2.create() + " for " + sequence + " from (1,2): \"t\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Definition<Regex> word1 = factory.defineRegex("[a-z]{4}");
			Definition<Regex> space = factory.defineRegex("\\s+");
			Definition<Regex> word2 = factory.defineRegex("[a-z]{4}");
			Sequence sequence = instantiateSequence(word1, space, word2);
			try {
				sequence.setContent(content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word1.create() + " for " + sequence + " from (1,1): \"st\\ntest\"",
						e.getMessage());
			}
			try {
				sequence.setContent(content.substring(4));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word1.create() + " for " + sequence + " from (1,1): \"\\ntest\"",
						e.getMessage());
			}
		}
	}

	@Test
	default void testTooShortOnMiddle() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		{
			String content = "test";
			Definition<Regex> letter1 = factory.defineRegex("[a-zA-Z]");
			Definition<Constant> letter2 = factory.defineConstant("e");
			Definition<Constant> letter3 = factory.defineConstant("s");
			Definition<Regex> letter4 = factory.defineRegex("[a-zA-Z]");
			Sequence sequence = instantiateSequence(letter1, letter2, letter3, letter4);
			try {
				sequence.setContent(content.substring(0, 1) + content.substring(2));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + letter2.create() + " for " + sequence + " from (1,2): \"st\"",
						e.getMessage());
			}
		}
		{
			String content = "test\ntest";
			Definition<Regex> word1 = factory.defineRegex("[a-z]{4}");
			Definition<Regex> space = factory.defineRegex("\\s+");
			Definition<Regex> word2 = factory.defineRegex("[a-z]{4}");
			Sequence sequence = instantiateSequence(word1, space, word2);
			try {
				sequence.setContent(content.substring(0, 2) + content.substring(3));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word1.create() + " for " + sequence + " from (1,1): \"tet\\ntest\"",
						e.getMessage());
			}
			try {
				sequence.setContent(content.substring(0, 2) + content.substring(6));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + space.create() + " for " + sequence + " from (1,5): \"t\"",
						e.getMessage());
			}
			try {
				sequence.setContent(content.substring(0, 6) + content.substring(7));
				fail("Exception not thrown.");
			} catch (ParsingException e) {
				assertEquals("Unable to parse " + word2.create() + " for " + sequence + " from (2,1): \"tst\"",
						e.getMessage());
			}
		}
	}

	@Test
	default void testInnerContentSynchronization() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		String content = "A testing case.";
		Definition<Regex> word1 = factory.defineRegex("[a-zA-Z]+");
		Definition<Regex> word2 = factory.defineRegex("[a-zA-Z]+");
		Definition<Regex> word3 = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space1 = factory.defineConstant(" ");
		Definition<Constant> space2 = factory.defineConstant(" ");
		Definition<Constant> dot = factory.defineConstant(".");
		Sequence sequence = instantiateSequence(Arrays.asList(word1, space1, word2, space2, word3, dot));
		sequence.setContent(content);

		assertEquals("A", sequence.get(word1).getContent());
		assertEquals("testing", sequence.get(word2).getContent());
		assertEquals("case", sequence.get(word3).getContent());

		sequence.get(word1).setContent("Another");
		sequence.get(word2).setContent("running");
		assertEquals("Another", sequence.get(word1).getContent());
		assertEquals("running", sequence.get(word2).getContent());
		assertEquals("case", sequence.get(word3).getContent());
		assertEquals("Another running case.", sequence.getContent());
	}

	@Test
	default void testGetIndexReturnsProperContent() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Constant> dot = factory.defineConstant(".");
		Sequence sequence = instantiateSequence(word, space, word, space, word, dot);
		sequence.setContent("This is testing.");

		assertEquals("This", sequence.get(0).getContent());
		assertEquals(" ", sequence.get(1).getContent());
		assertEquals("is", sequence.get(2).getContent());
		assertEquals(" ", sequence.get(3).getContent());
		assertEquals("testing", sequence.get(4).getContent());
		assertEquals(".", sequence.get(5).getContent());
	}

	@Test
	default void testGetFromDefinitionReturnsProperContent() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		assertEquals("Answer", sequence.get(word).getContent());
		assertEquals(" ", sequence.get(space).getContent());
		assertEquals("42", sequence.get(number).getContent());
	}

	@Test
	default void testGetDefinitionProvidesCorrectDefinition() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		assertEquals(word, sequence.getDefinition(0));
		assertEquals(space, sequence.getDefinition(1));
		assertEquals(number, sequence.getDefinition(2));
	}

	@Test
	default void testSetIndexContentChangesIndexContentCorrespondingly() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		sequence.set(0, "Test");

		assertEquals("Test", sequence.get(0).getContent());
	}

	@Test
	default void testSetIndexContentReturnsOldContent() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		assertEquals("Answer", sequence.set(0, "Test"));
	}

	@Test
	default void testSetIndexContentChangesOverallContentCorrespondingly() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		sequence.set(0, "Test");

		assertEquals("Test 42", sequence.getContent());
	}

	@Test
	default void testSetIndexContentNotifiesListeners() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		String[] value = { null };
		sequence.addContentListener((newValue) -> value[0] = newValue);

		sequence.set(0, "Test");
		assertEquals("Test 42", value[0]);
	}

	@Test
	default void testSetIndexContentRejectsNullContent() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		try {
			sequence.set(0, (String) null);
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	default void testSetIndexContentRejectsInvalidContent() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		try {
			sequence.set(0, "");
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	default void testSetIndexLayerChangesIndexContentCorrespondingly() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		Regex layer = word.create();
		layer.setContent("Test");
		sequence.set(0, layer);

		assertEquals("Test", sequence.get(0).getContent());
	}

	@Test
	default void testSetIndexLayerChangesIndexLayerCorrespondingly() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		Regex layer = word.create();
		layer.setContent("Test");
		sequence.set(0, layer);

		assertSame(layer, sequence.get(0));
	}

	@Test
	default void testSetIndexLayerReturnsOldLayer() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		Layer oldLayer = sequence.get(0);

		Regex layer = word.create();
		layer.setContent("Test");
		assertSame(oldLayer, sequence.set(0, layer));
	}

	@Test
	default void testSetIndexLayerChangesOverallContentCorrespondingly() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		Regex layer = word.create();
		layer.setContent("Test");
		sequence.set(0, layer);

		assertEquals("Test 42", sequence.getContent());
	}

	@Test
	default void testSetIndexLayerNotifiesListeners() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		String[] value = { null };
		sequence.addContentListener((newValue) -> value[0] = newValue);

		Regex layer = word.create();
		layer.setContent("Test");
		sequence.set(0, layer);
		assertEquals("Test 42", value[0]);
	}

	@Test
	default void testSetIndexLayerRejectsNullLayer() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		try {
			sequence.set(0, (Layer) null);
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	default void testSetIndexLayerRejectsInvalidLayer() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		Regex element = new JavaPatternRegex("[a-zA-Z0-9]+");
		element.setContent("Test");
		try {
			sequence.set(0, element);
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	default void testSetDefinitionContentChangesDefinitionContentCorrespondingly() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		sequence.set(word, "Test");

		assertEquals("Test", sequence.get(word).getContent());
	}

	@Test
	default void testSetDefinitionContentReturnsOldContent() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		assertEquals("Answer", sequence.set(word, "Test"));
	}

	@Test
	default void testSetDefinitionContentChangesOverallContentCorrespondingly() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		sequence.set(word, "Test");

		assertEquals("Test 42", sequence.getContent());
	}

	@Test
	default void testSetDefinitionContentNotifiesListeners() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		String[] value = { null };
		sequence.addContentListener((newValue) -> value[0] = newValue);

		sequence.set(word, "Test");
		assertEquals("Test 42", value[0]);
	}

	@Test
	default void testSetDefinitionContentRejectsNullContent() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		try {
			sequence.set(word, (String) null);
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	default void testSetDefinitionContentRejectsInvalidContent() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		try {
			sequence.set(word, "");
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	default void testSetDefinitionContentRejectsNullDefinition() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		try {
			sequence.set(null, "Test");
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	default void testSetDefinitionContentRejectsAmbiguousDefinition() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Sequence sequence = instantiateSequence(word, space, word);
		sequence.setContent("Answer test");

		try {
			sequence.set(word, "try");
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	default void testSetDefinitionContentRejectsUnknownDefinition() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		try {
			sequence.set(factory.defineRegex("[a-zA-Z]+"), "Test");
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	default void testSetDefinitionLayerChangesDefinitionContentCorrespondingly() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		Regex layer = word.create();
		layer.setContent("Test");
		sequence.set(word, layer);

		assertEquals("Test", sequence.get(word).getContent());
	}

	@Test
	default void testSetDefinitionLayerChangesDefinitionLayerCorrespondingly() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		Regex layer = word.create();
		layer.setContent("Test");
		sequence.set(word, layer);

		assertSame(layer, sequence.get(word));
	}

	@Test
	default void testSetDefinitionLayerReturnsOldLayer() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		Layer oldLayer = sequence.get(word);

		Regex layer = word.create();
		layer.setContent("Test");
		assertSame(oldLayer, sequence.set(word, layer));
	}

	@Test
	default void testSetDefinitionLayerChangesOverallContentCorrespondingly() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		Regex layer = word.create();
		layer.setContent("Test");
		sequence.set(word, layer);

		assertEquals("Test 42", sequence.getContent());
	}

	@Test
	default void testSetDefinitionLayerNotifiesListeners() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		String[] value = { null };
		sequence.addContentListener((newValue) -> value[0] = newValue);

		Regex layer = word.create();
		layer.setContent("Test");
		sequence.set(word, layer);
		assertEquals("Test 42", value[0]);
	}

	@Test
	default void testDirectUpdateOnSubLayerNotifiesListeners() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		String[] value = { null };
		sequence.addContentListener((newValue) -> value[0] = newValue);

		sequence.get(word).setContent("Test");
		assertEquals("Test 42", value[0]);

		sequence.get(number).setContent("123");
		assertEquals("Test 123", value[0]);
	}

	@Test
	default void testDirectUpdateOnSetSubLayerNotifiesListeners() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		String[] value = { null };
		sequence.addContentListener((newValue) -> value[0] = newValue);

		Regex wordLayer = word.create();
		wordLayer.setContent("Answer");
		sequence.set(word, wordLayer);

		wordLayer.setContent("Test");
		assertEquals("Test 42", value[0]);

		Regex numberLayer = number.create();
		numberLayer.setContent("42");
		sequence.set(number, numberLayer);

		numberLayer.setContent("123");
		assertEquals("Test 123", value[0]);
	}

	@Test
	default void testSetDefinitionLayerRejectsNullLayer() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		try {
			sequence.set(word, (Regex) null);
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	default void testSetDefinitionLayerRejectsInvalidLayer() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		Regex element = new JavaPatternRegex("[a-zA-Z0-9]+");
		element.setContent("Test");
		try {
			sequence.set(word, element);
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	default void testSetDefinitionLayerRejectsNullDefinition() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		Regex item = word.create();
		item.setContent("Test");
		try {
			sequence.set(null, item);
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	default void testSetDefinitionLayerRejectsUnknownDefinition() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Definition<Regex> number = factory.defineRegex("\\d+");
		Sequence sequence = instantiateSequence(word, space, number);
		sequence.setContent("Answer 42");

		Regex element = new JavaPatternRegex("[a-zA-Z0-9]+");
		element.setContent("Test");
		try {
			sequence.set(factory.defineRegex("[a-zA-Z]+"), element);
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	default void testSetDefinitionLayerRejectsAmbiguousDefinition() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space = factory.defineConstant(" ");
		Sequence sequence = instantiateSequence(word, space, word);
		sequence.setContent("Answer test");

		Regex element = new JavaPatternRegex("[a-zA-Z0-9]+");
		element.setContent("try");
		try {
			sequence.set(word, element);
			fail("No exception thrown");
		} catch (IllegalArgumentException cause) {
			// OK
		}
	}

	@Test
	default void testInnerContentUpdateOfFilledSequenceNotifiesListeners() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word1 = factory.defineRegex("[a-zA-Z]+");
		Definition<Regex> word2 = factory.defineRegex("[a-zA-Z]+");
		Definition<Regex> word3 = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space1 = factory.defineConstant(" ");
		Definition<Constant> space2 = factory.defineConstant(" ");
		Definition<Constant> dot = factory.defineConstant(".");
		Sequence sequence = instantiateSequence(word1, space1, word2, space2, word3, dot);
		sequence.setContent("A testing case.");

		final String[] value = new String[] { null };
		sequence.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		});

		sequence.get(word1).setContent("Another");
		assertEquals(sequence.getContent(), value[0]);
		sequence.get(word2).setContent("running");
		assertEquals(sequence.getContent(), value[0]);
	}

	@Test
	default void testUpdateOfUnfilledSequenceThrowsException() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word = factory.defineRegex("[a-zA-Z]+");
		Sequence sequence = instantiateSequence(word);

		try {
			sequence.get(word);
			fail("No exception thrown");
		} catch (NoContentException cause) {
			// OK
		}
	}

	@Test
	default void testListenersNotifiedOncePerAtomicUpdate() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		Definition<Regex> word1 = factory.defineRegex("[a-zA-Z]+");
		Definition<Regex> word2 = factory.defineRegex("[a-zA-Z]+");
		Definition<Regex> word3 = factory.defineRegex("[a-zA-Z]+");
		Definition<Constant> space1 = factory.defineConstant(" ");
		Definition<Constant> space2 = factory.defineConstant(" ");
		Definition<Constant> dot = factory.defineConstant(".");
		Sequence sequence = instantiateSequence(word1, space1, word2, space2, word3, dot);
		final LinkedList<String> values = new LinkedList<String>();
		sequence.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				values.addFirst(newContent);
			}
		});

		int operationCounter = 0;

		sequence.setContent("A running test.");
		assertEquals(++operationCounter, values.size());
		assertEquals(sequence.getContent(), values.getFirst());

		sequence.setContent("Another testing case.");
		assertEquals(++operationCounter, values.size());
		assertEquals(sequence.getContent(), values.getFirst());
	}

	@Test
	default void testNoProblemWithLazyComponents() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		String content = "abc?def";
		Sequence sequence = instantiateSequence(factory.defineRegex(".+?"), factory.defineConstant("?"),
				factory.defineRegex(".+?"));

		sequence.setContent(content);
		assertEquals(content, sequence.getContent());
		assertEquals("abc", sequence.get(0).getContent());
		assertEquals("?", sequence.get(1).getContent());
		assertEquals("def", sequence.get(2).getContent());
	}
}
