package fr.vergne.parsing.layer;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

public interface ModifiableComposedLayerTest<T extends Layer> extends ComposedLayerTest<T> {

	static interface SublayerReplacement {
		Layer getInitial();

		Layer getReplacement();

		void execute();

		void revert();
	}

	public Collection<SublayerReplacement> getSublayersReplacements(T parent);

	@Test
	public default void testSublayerReplacementNotifiesParentListener() {
		Map<String, T> map = instantiateLayers(getSpecialCharactersToManage());
		int[] testCounter = { 0 };
		for (Entry<String, T> entry : map.entrySet()) {
			String content = entry.getKey();
			T layer = entry.getValue();

			if (layer == null) {
				// irrelevant test
			} else {
				layer.setContent(content);
				Collection<SublayerReplacement> replacements = getSublayersReplacements(layer);
				String[] value = { null };
				layer.addContentListener((newContent) -> value[0] = newContent);

				for (SublayerReplacement replacement : replacements) {
					BiConsumer<Layer, Boolean> test = (sub, mustNotify) -> {
						if (sub != null) {
							value[0] = null;
							// TODO Use different content
							// TODO should fail if same content
							sub.setContent(sub.getContent());
							if (mustNotify) {
								assertEquals(layer.getContent(), value[0]);
							} else {
								assertNull(value[0]);
							}
							testCounter[0]++;
						} else {
							// Nothing to test
						}
					};

					test.accept(replacement.getInitial(), true);
					test.accept(replacement.getReplacement(), false);

					replacement.execute();

					test.accept(replacement.getInitial(), false);
					test.accept(replacement.getReplacement(), true);

					replacement.revert();

					test.accept(replacement.getInitial(), true);
					test.accept(replacement.getReplacement(), false);
				}
			}
		}
		assertTrue("No replacement has been tested.", testCounter[0] > 0);
	}

}
