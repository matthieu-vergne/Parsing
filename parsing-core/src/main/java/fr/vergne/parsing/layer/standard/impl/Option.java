package fr.vergne.parsing.layer.standard.impl;

import java.io.InputStream;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.AbstractLayer;
import fr.vergne.parsing.layer.impl.RecursivityLimiter;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.util.ContentInputStream;
import fr.vergne.parsing.util.Named;

/**
 * An {@link Option} make a {@link Layer} optional. Thus, a compatible content
 * is one which fits the optional {@link Layer} or an empty one.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <CLayer>
 */
public class Option<CLayer extends Layer> extends AbstractLayer implements Named {

	private final Definition<CLayer> optionDefinition;
	private final Quantifier quantifier;
	private CLayer layer;
	private final ContentListener deepListener = new ContentListener() {

		@Override
		public void contentSet(String newContent) {
			fireContentUpdate(newContent);
		}
	};

	public Option(Definition<CLayer> definition, Quantifier quantifier) {
		this.optionDefinition = definition;
		this.layer = null;
		this.quantifier = quantifier;
	}

	public Option(Definition<CLayer> definition) {
		this(definition, Quantifier.GREEDY);
	}

	public Quantifier getQuantifier() {
		return quantifier;
	}

	private static final RecursivityLimiter<Option<?>, String> regexProcessor = new RecursivityLimiter<>(
			(option) -> "(?:" + option.optionDefinition.getRegex() + ")?" + option.quantifier.getDecorator(),
			(option) -> "[\\s\\S]*");

	// TODO Deprecate & remove
	public String getRegex() {
		return regexProcessor.callOn(this);
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
			if (layer != null) {
				layer.removeContentListener(deepListener);
				layer = null;
			} else {
				// already null
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

	/**
	 * 
	 * @return <code>true</code> if the current content fits the option,
	 *         <code>false</code> otherwise (empty content)
	 */
	public boolean isPresent() {
		return layer != null;
	}

	/**
	 * 
	 * @return the {@link Definition} of the {@link Layer} used in this
	 *         {@link Option}
	 */
	public Definition<CLayer> getOptionalDefinition() {
		return optionDefinition;
	}

	/**
	 * 
	 * @return the {@link Layer} wrapped by this {@link Option}
	 * @throws RuntimeException
	 *             if the option is not present
	 */
	public CLayer getOption() {
		if (isPresent()) {
			return layer;
		} else {
			throw new RuntimeException("The option is not present");
		}
	}

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
	public String getName() {
		return "OPT";
	}

	@Override
	public String toString() {
		return getName() + "[" + Named.name(optionDefinition.create()) + "]";
	}
}
