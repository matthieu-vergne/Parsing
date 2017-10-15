package fr.vergne.parsing.layer;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.Test;

public interface ModifiableComposedLayerTest<T extends Layer> extends ComposedLayerTest<T> {

	static interface Update {
		Layer getInitial();

		Layer getReplacement();

		void execute();

		void revert();
	}

	public Collection<Update> getSublayersUpdatesFunctions(T parent);

	@Test
	public default void testUpdatedSublayersUpdateNotifiesParentListener() {
		Map<String, T> map = instantiateLayers(getSpecialCharactersToManage());
		int[] testCounter = { 0 };
		for (Entry<String, T> entry : map.entrySet()) {
			String content = entry.getKey();
			T layer = entry.getValue();

			if (layer == null) {
				// irrelevant test
			} else {
				layer.setContent(content);
				Collection<Update> updates = getSublayersUpdatesFunctions(layer);
				String[] value = { null };
				layer.addContentListener((newContent) -> value[0] = newContent);

				for (Update update : updates) {
					BiConsumer<Layer, Boolean> test = (sub, mustNotify) -> {
						if (sub != null) {
							value[0] = null;
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

					test.accept(update.getInitial(), true);
					test.accept(update.getReplacement(), false);

					update.execute();

					test.accept(update.getInitial(), false);
					test.accept(update.getReplacement(), true);

					update.revert();

					test.accept(update.getInitial(), true);
					test.accept(update.getReplacement(), false);
				}
			}
		}
		assertTrue("No update has been tested.", testCounter[0] > 0);
	}
}
