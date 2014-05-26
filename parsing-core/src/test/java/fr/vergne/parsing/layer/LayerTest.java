package fr.vergne.parsing.layer;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public abstract class LayerTest {

	protected abstract Layer instantiateFilledLayer();

	@Test
	public void testContentStreamAlignment() throws IOException {
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

}
