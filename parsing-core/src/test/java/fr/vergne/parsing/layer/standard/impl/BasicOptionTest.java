package fr.vergne.parsing.layer.standard.impl;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.OptionTest;

@RunWith(JUnitPlatform.class)
public class BasicOptionTest implements OptionTest {

	@Override
	public <T extends Layer> Option<T> instantiateOption(Definition<T> definition) {
		return new BasicOption<>(definition);
	}

}
