package fr.vergne.parsing.layer;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import fr.vergne.ioutils.StringUtils;
import fr.vergne.parsing.layer.Layer.ContentListener;

public abstract class LayerTest {

	/**
	 * This method aims at instantiating {@link Layer}s for applying generic
	 * tests. As such, it should return specific contents mapped to
	 * {@link Layer}s on which these contents are applicable. The special
	 * characters provided in arguments are {@link String}s that should be
	 * checked as much as possible, although it is not always possible. In such
	 * a case, there should still have the requested contents, but they can be
	 * mapped to <code>null</code> to say that no instance of the {@link Layer}
	 * can be applied to it. If contents are missing, it will fail the
	 * corresponding tests, while if a content is present but mapped to
	 * <code>null</code>, it will be considered as an irrelevant test and will
	 * not occur.
	 * 
	 * @param specialCharacters
	 *            specific characters to manage
	 * @return a map of contents mapped to specific instances of the
	 *         {@link Layer} to test
	 */
	protected abstract Map<String, Layer> instantiateLayers(
			Collection<String> specialCharacters);

	private Collection<String> getSpecialCharactersToManage() {
		return Arrays.asList("ê", "\n", "\r", "Σ", "δ");
	}

	@Test
	public void testInstantiatedLayersCoverAllSpecialCharacters() {
		Map<String, Layer> map = instantiateLayers(getSpecialCharactersToManage());

		Collection<String> notCoveredYet = new HashSet<String>(
				getSpecialCharactersToManage());
		for (String content : map.keySet()) {
			Iterator<String> iterator = notCoveredYet.iterator();
			while (iterator.hasNext()) {
				String specialCharacter = iterator.next();
				if (content.contains(specialCharacter)) {
					iterator.remove();
				} else {
					// not yet covered
				}
			}
		}
		assertTrue("Some special characters have not been covered: "
				+ notCoveredYet, notCoveredYet.isEmpty());
	}

	@Test
	public void testNullContentThrowsNullPointerException() {
		Map<String, Layer> map = instantiateLayers(getSpecialCharactersToManage());
		Collection<Layer> layers = new HashSet<Layer>(map.values());
		layers.remove(null);

		for (Layer layer : layers) {
			try {
				layer.setContent(null);
				fail("No exception thrown: " + layer);
			} catch (NullPointerException e) {
			}
		}
	}

	@Test
	public void testGetContentReturnsSetContent() {
		Map<String, Layer> map = instantiateLayers(getSpecialCharactersToManage());
		for (Entry<String, Layer> entry : map.entrySet()) {
			String content = entry.getKey();
			Layer layer = entry.getValue();

			if (layer == null) {
				// irrelevant test
			} else {
				layer.setContent(content);
				assertEquals(layer.toString(), content, layer.getContent());
			}
		}
	}

	@Test
	public void testSetContentProperlyNotifiesListeners() {
		final String[] value = new String[] { null };
		ContentListener listener = new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				value[0] = newContent;
			}
		};

		Map<String, Layer> map = instantiateLayers(getSpecialCharactersToManage());
		for (Entry<String, Layer> entry : map.entrySet()) {
			String content = entry.getKey();
			Layer layer = entry.getValue();

			if (layer == null) {
				// irrelevant test
			} else {
				layer.addContentListener(listener);

				value[0] = null;
				layer.setContent(content);
				assertEquals(layer.toString(), content, value[0]);

				layer.removeContentListener(listener);
			}
		}
	}

	@Test
	public void testInputStreamPreservesContent() throws IOException {
		Map<String, Layer> map = instantiateLayers(getSpecialCharactersToManage());

		for (Entry<String, Layer> entry : map.entrySet()) {
			String content = entry.getKey();
			Layer layer = entry.getValue();

			if (layer == null) {
				// irrelevant test
			} else {
				layer.setContent(content);
				String retrieved = StringUtils.readFromInputStream(layer
						.getInputStream());
				assertEquals(layer.toString(), content, retrieved);
			}
		}
	}

	@Test
	public void testCloneableInstanceGeneratesProperClone() throws IOException {
		Map<String, Layer> map = instantiateLayers(getSpecialCharactersToManage());
		Collection<Layer> layers = new HashSet<Layer>(map.values());
		layers.remove(null);

		for (Layer layer : layers) {
			try {
				Layer clone = (Layer) layer.getClass().getMethod("clone")
						.invoke(layer);
				assertNotNull(clone);
				assertNotSame(layer, clone);
				assertEquals(layer.getContent(), clone.getContent());
			} catch (Exception e) {
				// not cloneable
			}
		}
	}
}
