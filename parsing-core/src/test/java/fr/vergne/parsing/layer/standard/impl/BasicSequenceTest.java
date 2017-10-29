package fr.vergne.parsing.layer.standard.impl;

import java.util.List;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.standard.Sequence;
import fr.vergne.parsing.layer.standard.SequenceTest;

@RunWith(JUnitPlatform.class)
public class BasicSequenceTest implements SequenceTest {

	@Override
	public Sequence instantiateSequence(List<Definition<?>> definitions) {
		return new BasicSequence(definitions);
	}

}
