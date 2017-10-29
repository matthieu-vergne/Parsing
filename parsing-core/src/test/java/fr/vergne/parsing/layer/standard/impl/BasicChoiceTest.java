package fr.vergne.parsing.layer.standard.impl;

import java.util.Collection;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.standard.Choice;
import fr.vergne.parsing.layer.standard.ChoiceTest;

@RunWith(JUnitPlatform.class)
public class BasicChoiceTest implements ChoiceTest {

	@Override
	public Choice instantiateChoice(Collection<Definition<?>> alternatives) {
		return new BasicChoice(alternatives);
	}

}
