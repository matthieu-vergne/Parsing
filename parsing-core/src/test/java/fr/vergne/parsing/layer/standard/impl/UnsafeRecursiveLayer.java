package fr.vergne.parsing.layer.standard.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.AbstractLayer;
import fr.vergne.parsing.util.ContentInputStream;
import fr.vergne.parsing.util.Named;

public class UnsafeRecursiveLayer extends AbstractLayer implements Named {

	public static final Definition<UnsafeRecursiveLayer> defineOn(Definition<?> definition) {
		return new Definition<UnsafeRecursiveLayer>() {

			@Override
			public String getRegex() {
				return "-(?:" + definition.getRegex() + ")?";
			}

			@Override
			public UnsafeRecursiveLayer create() {
				return new UnsafeRecursiveLayer(definition);
			}
		};
	}

	private final Definition<?> definition;
	private final Layer layer;
	private boolean goDeeper;

	public UnsafeRecursiveLayer(Definition<?> definition) {
		this.definition = definition;
		this.layer = definition.create();
		this.layer.addContentListener((newContent) -> fireContentUpdate());
	}

	@Override
	protected void setInternalContent(String content) {
		String regex = defineOn(definition).getRegex();
		if (!Pattern.matches(regex, content)) {
			throw new ParsingException(regex, content);
		} else {
			String remaining = content.substring(1);
			if (remaining.isEmpty()) {
				goDeeper = false;
			} else {
				goDeeper = true;
				layer.setContent(remaining);
			}
		}
	}

	@Override
	public InputStream getInputStream() throws NoContentException {
		return new InputStream() {

			InputStream stream1 = new ContentInputStream("-");
			InputStream stream2 = goDeeper ? layer.getInputStream() : new ContentInputStream("");

			@Override
			public int read() throws IOException {
				int read = stream1.read();
				if (read == -1) {
					return stream2.read();
				} else {
					return read;
				}
			}
		};
	}

	@Override
	public String getName() {
		return "UNSAFE";
	}

	@Override
	public String toString() {
		return getName() + ":" + definition.create().toString();
	}

	public Layer getSublayer() {
		return layer;
	}
}
