package fr.vergne.parsing.layer.standard.impl;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.standard.SeparatedLoop;
import fr.vergne.parsing.layer.standard.SeparatedLoopTest;

@RunWith(JUnitPlatform.class)
public class LoopBasedSeparatedLoopTest implements SeparatedLoopTest {

	@Override
	public <Item extends Layer, Separator extends Layer> SeparatedLoop<Item, Separator> instantiateSeparatedLoop(
			Definition<Item> item, Definition<Separator> separator) {
		return new LoopBasedSeparatedLoop<>(item, separator);
	}

	@Override
	public <Item extends Layer, Separator extends Layer> SeparatedLoop<Item, Separator> instantiateSeparatedLoop(
			Definition<Item> item, Definition<Separator> separator, int min, int max) {
		return new LoopBasedSeparatedLoop<>(item, separator, min, max);
	}

}
