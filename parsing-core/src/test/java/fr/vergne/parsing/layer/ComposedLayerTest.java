package fr.vergne.parsing.layer;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

public interface ComposedLayerTest<T extends Layer> extends LayerTest<T> {

	public Collection<Layer> getUsedSubLayers(T layer);

	public Layer instantiateRecursiveLayer();

	public String getValidRecursiveContent(Layer layer);

	@Test
	public default void testSubLayersUpdateNotifiesParentListener() {
		Map<String, T> map = instantiateLayers(getSpecialCharactersToManage());
		int testCounter = 0;
		for (Entry<String, T> entry : map.entrySet()) {
			String content = entry.getKey();
			T layer = entry.getValue();

			if (layer == null) {
				// irrelevant test
			} else {
				layer.setContent(content);
				String[] value = { null };
				layer.addContentListener((newContent) -> value[0] = newContent);

				Collection<Layer> sublayers = getUsedSubLayers(layer);
				assertNotNull("If no used sublayer is available, return an empty collection.", sublayers);
				for (Layer sublayer : sublayers) {
					assertNotNull("Null sublayer provided", sublayer);
					value[0] = null;
					sublayer.setContent(sublayer.getContent());
					assertEquals(content, value[0]);
					testCounter++;
				}
			}
		}
		assertTrue("No sublayer tested.", testCounter > 0);
	}

	@Test
	public default void testCompatibleWithRecursiveSublayers() {
		Layer layer = instantiateRecursiveLayer();
		if (layer == null) {
			// irrelevant test
		} else {
			layer.setContent(getValidRecursiveContent(layer));
		}
	}
}
