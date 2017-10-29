package fr.vergne.parsing.layer.standard.impl;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.standard.Loop;
import fr.vergne.parsing.layer.standard.LoopTest;
import fr.vergne.parsing.layer.standard.Quantifier;

@RunWith(JUnitPlatform.class)
public class BasicLoopTest implements LoopTest {

	@Override
	public <T extends Layer> Loop<T> instantiateLoop(Definition<T> definition, int min, int max,
			Quantifier quantifier) {
		return new BasicLoop<>(definition, min, max, quantifier);
	}

	@Override
	public <T extends Layer> Loop<T> instantiateLoop(Definition<T> definition, int min, int max) {
		return new BasicLoop<>(definition, min, max);
	}

	@Override
	public <T extends Layer> Loop<T> instantiateLoop(Definition<T> definition) {
		return new BasicLoop<>(definition);
	}

}
