package fr.vergne.parsing.layer.standard.impl;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.standard.Choice;
import fr.vergne.parsing.layer.standard.ChoiceTest;
import fr.vergne.parsing.layer.standard.Constant;
import fr.vergne.parsing.layer.standard.ConstantTest;
import fr.vergne.parsing.layer.standard.Loop;
import fr.vergne.parsing.layer.standard.LoopTest;
import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.OptionTest;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.layer.standard.Regex;
import fr.vergne.parsing.layer.standard.RegexTest;
import fr.vergne.parsing.layer.standard.SeparatedLoop;
import fr.vergne.parsing.layer.standard.SeparatedLoopTest;
import fr.vergne.parsing.layer.standard.Sequence;
import fr.vergne.parsing.layer.standard.SequenceTest;

@RunWith(JUnitPlatform.class)
public class StandardDefinitionFactoryTestSuite {

	@Test
	public void testDefineAs() {
		// TODO Test definition wrapping (defineAs)
		fail("defineAs not tested yet");
	}

	@Test
	public void testPrepareDefinition() {
		// TODO Test definition preparation (prepareDefinition)
		fail("prepareDefinition not tested yet");
	}

	@Nested
	public class ConstantDef implements ConstantTest {

		@Override
		public Constant instantiateConstant(String content) {
			StandardDefinitionFactory factory = new StandardDefinitionFactory();
			return factory.defineConstant(content).create();
		}
	}

	@Nested
	public class RegexDef implements RegexTest {

		@Override
		public Regex instantiateRegex(String regex) {
			StandardDefinitionFactory factory = new StandardDefinitionFactory();
			return factory.defineRegex(regex).create();
		}

	}

	@Nested
	public class SequenceDef implements SequenceTest {

		@Override
		public Sequence instantiateSequence(List<Definition<?>> definitions) {
			StandardDefinitionFactory factory = new StandardDefinitionFactory();
			return factory.defineSequence(definitions).create();
		}

	}

	@Nested
	public class ChoiceDef implements ChoiceTest {

		@Override
		public Choice instantiateChoice(Collection<Definition<?>> alternatives) {
			StandardDefinitionFactory factory = new StandardDefinitionFactory();
			return factory.defineChoice(alternatives).create();
		}
	}

	@Nested
	public class OptionDef implements OptionTest {

		@Override
		public <T extends Layer> Option<T> instantiateOption(Definition<T> definition) {
			StandardDefinitionFactory factory = new StandardDefinitionFactory();
			return factory.defineOptional(definition).create();
		}

	}

	@Nested
	public class LoopDef implements LoopTest {

		@Override
		public <T extends Layer> Loop<T> instantiateLoop(Definition<T> itemDefinition, int min, int max,
				Quantifier quantifier) {
			StandardDefinitionFactory factory = new StandardDefinitionFactory();
			return factory.defineLoop(itemDefinition, min, max, quantifier).create();
		}

		@Override
		public <T extends Layer> Loop<T> instantiateLoop(Definition<T> itemDefinition, int min, int max) {
			StandardDefinitionFactory factory = new StandardDefinitionFactory();
			return factory.defineLoop(itemDefinition, min, max).create();
		}

		@Override
		public <T extends Layer> Loop<T> instantiateLoop(Definition<T> itemDefinition) {
			StandardDefinitionFactory factory = new StandardDefinitionFactory();
			return factory.defineLoop(itemDefinition).create();
		}

	}

	@Nested
	public class SeparatedLoopDef implements SeparatedLoopTest {

		@Override
		public <Item extends Layer, Separator extends Layer> SeparatedLoop<Item, Separator> instantiateSeparatedLoop(
				Definition<Item> item, Definition<Separator> separator) {
			StandardDefinitionFactory factory = new StandardDefinitionFactory();
			return factory.defineSeparatedLoop(item, separator).create();
		}

		@Override
		public <Item extends Layer, Separator extends Layer> SeparatedLoop<Item, Separator> instantiateSeparatedLoop(
				Definition<Item> item, Definition<Separator> separator, int min, int max) {
			StandardDefinitionFactory factory = new StandardDefinitionFactory();
			return factory.defineSeparatedLoop(item, separator, min, max).create();
		}

	}

}
