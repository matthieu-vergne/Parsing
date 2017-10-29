package fr.vergne.parsing.layer;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

public interface ComposedLayerTest<T extends Layer> extends LayerTest<T> {

	public Collection<Layer> getUsedSubLayers(T layer);

	static interface SublayerUpdate {
		Layer getLayer();

		String getInitial();

		String getReplacement();
	}

	// TODO See if I can merge with other methods
	// TODO Implement in child tests
	public Collection<SublayerUpdate> getSublayersUpdates(T parent);

	public Layer instantiateRecursiveLayer();

	public String getValidRecursiveContent(Layer layer);

	@Test
	public default void testSubLayerUpdateNotifiesParentListener() {
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
					// TODO Use different content
					// TODO Fail if same content
					sublayer.setContent(sublayer.getContent());
					assertEquals(content, value[0]);
					testCounter++;
				}
			}
		}
		assertTrue("No sublayer tested.", testCounter > 0);
	}

	@Test
	public default void testSublayerUpdateChangesParentContent() {
		Map<String, T> map = instantiateLayers(getSpecialCharactersToManage());
		int testCounter = 0;
		for (Entry<String, T> entry : map.entrySet()) {
			String content = entry.getKey();
			T layer = entry.getValue();

			if (layer == null) {
				// irrelevant test
			} else {
				layer.setContent(content);
				Collection<SublayerUpdate> updates = getSublayersUpdates(layer);
				for (SublayerUpdate update : updates) {
					Layer sublayer = update.getLayer();
					String initial = update.getInitial();
					String replacement = update.getReplacement();

					assertNotNull(sublayer);
					assertNotEquals(initial, replacement);

					/*
					 * We don't test notification on this one, because the content may be already
					 * set and notifications may be avoided with same content. We only ensure that
					 * the content is indeed the initial one.
					 */
					sublayer.setContent(initial);

					sublayer.setContent(replacement);
					assertTrue(layer.getContent().contains(sublayer.getContent()));
					testCounter++;

					// We revert to the initial content to make each iteration independent.
					sublayer.setContent(initial);
				}
			}
		}
		assertTrue("No update has been tested.", testCounter > 0);
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
