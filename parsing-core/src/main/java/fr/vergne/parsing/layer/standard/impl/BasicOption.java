package fr.vergne.parsing.layer.standard.impl;

import java.io.InputStream;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.AbstractLayer;
import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.util.ContentInputStream;
import fr.vergne.parsing.util.Named;

// TODO Replace Basic by more explicit term
// TODO Doc
public class BasicOption<CLayer extends Layer> extends AbstractLayer implements Option<CLayer> {

	private final Definition<CLayer> optionDefinition;
	private final Quantifier quantifier;
	private CLayer layer;
	private final ContentListener deepListener = new ContentListener() {

		@Override
		public void contentSet(String newContent) {
			fireContentUpdate(newContent);
		}
	};

	public BasicOption(Definition<CLayer> definition, Quantifier quantifier) {
		this.optionDefinition = definition;
		this.layer = null;
		this.quantifier = quantifier;
	}

	public BasicOption(Definition<CLayer> definition) {
		this(definition, Quantifier.GREEDY);
	}

	@Override
	public Definition<CLayer> getOptionalDefinition() {
		return optionDefinition;
	}

	@Override
	public Quantifier getQuantifier() {
		return quantifier;
	}

	@Override
	public InputStream getInputStream() {
		if (isPresent()) {
			return layer.getInputStream();
		} else {
			return new ContentInputStream("");
		}
	}

	@Override
	protected void setInternalContent(String content) {
		if (content.isEmpty()) {
			if (layer == null) {
				// already null
			} else {
				layer.removeContentListener(deepListener);
				layer = null;
			}
		} else {
			if (layer == null) {
				layer = optionDefinition.create();
				layer.addContentListener(deepListener);
			} else {
				// reuse
			}

			try {
				layer.setContent(content);
			} catch (ParsingException e) {
				throw new ParsingException(this, optionDefinition, content, 0, content.length(), e);
			}
		}
	}

	@Override
	public boolean isPresent() {
		return layer != null;
	}

	@Override
	public CLayer getOption() {
		if (isPresent()) {
			return layer;
		} else {
			throw new RuntimeException("The option is not present");
		}
	}

	@Override
	public void setOption(CLayer layer) {
		if (!optionDefinition.isCompatibleWith(layer)) {
			throw new IllegalArgumentException("Invalid layer: " + layer);
		} else {
			if (isPresent()) {
				this.layer.removeContentListener(deepListener);
			} else {
				// No listener to remove
			}
			this.layer = layer;
			this.layer.addContentListener(deepListener);
			fireContentUpdate();
		}
	}

	@Override
	public String toString() {
		return getName() + "[" + Named.name(optionDefinition.create()) + "]";
	}
}
