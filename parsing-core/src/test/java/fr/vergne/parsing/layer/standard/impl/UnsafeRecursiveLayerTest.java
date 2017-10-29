package fr.vergne.parsing.layer.standard.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import fr.vergne.parsing.layer.ComposedLayerTest;
import fr.vergne.parsing.layer.Layer;

@RunWith(JUnitPlatform.class)
public class UnsafeRecursiveLayerTest implements ComposedLayerTest<UnsafeRecursiveLayer> {

	StandardDefinitionFactory factory = new StandardDefinitionFactory();

	@Override
	public Map<String, UnsafeRecursiveLayer> instantiateLayers(Collection<String> specialCharacters) {
		Map<String, UnsafeRecursiveLayer> map = new HashMap<>();
		for (String character : specialCharacters) {
			map.put("-" + character, new UnsafeRecursiveLayer(factory.defineRegex(character)));
		}
		return map;
	}

	@Override
	public Collection<Layer> getUsedSubLayers(UnsafeRecursiveLayer layer) {
		return Arrays.asList(layer.getSublayer());
	}

	@Override
	public Layer instantiateRecursiveLayer() {
		// The class is unsafe for recursivity, so we ignore it
		return null;
	}

	@Override
	public String getValidRecursiveContent(Layer layer) {
		// The class is unsafe for recursivity, so we ignore it
		return null;
	}
}
