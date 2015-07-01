package fr.vergne.parsing.layer;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public abstract class LayerTest {

	protected abstract Layer instantiateFilledLayer();

	@Test
	public void testNullContentThrowsNullPointerException() {
		Layer layer = instantiateFilledLayer();
		try {
			layer.setContent(null);
			fail("No exception thrown.");
		} catch (NullPointerException e) {
		}
	}

	@Test
	public void testInputStreamPreservesContent() throws IOException {
		Layer layer = instantiateFilledLayer();
		String content = layer.getContent();
		InputStream stream = layer.getInputStream();
		StringBuilder builder = new StringBuilder();
		int codePoint = 0;
		while ((codePoint = stream.read()) != -1) {
			builder.appendCodePoint(codePoint);
		}
		assertEquals(content, builder.toString());
	}

	protected abstract Layer instantiateFilledLayerwithSpecialCharacters(
			Collection<String> charactersToReuse);

	@Test
	public void testInputStreamPreservesSpecialCharacters() throws IOException {
		Collection<String> charactersToReuse = Arrays.asList("ê", "\n", "\r",
				"Σ", "δ");

		Layer layer = instantiateFilledLayerwithSpecialCharacters(charactersToReuse);
		String original = layer.getContent();
		for (String character : charactersToReuse) {
			assertTrue("'" + character + "' not used in \"" + original + "\"",
					original.contains(character));
		}

		String copy = IOUtils.toString(layer.getInputStream());
		assertEquals(original, copy);
	}
}
