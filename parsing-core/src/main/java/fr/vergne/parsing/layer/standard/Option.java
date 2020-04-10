package fr.vergne.parsing.layer.standard;

import java.io.InputStream;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.AbstractLayer;
import fr.vergne.parsing.util.ContentInputStream;
import fr.vergne.parsing.util.Named;
import fr.vergne.parsing.util.RegexRecursivityLimiter;

/**
 * An {@link Option} make a {@link Layer} optional. Thus, a compatible content
 * is one which fits the optional {@link Layer} or an empty one.
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

	/**
	 * 
	 * @return the {@link Definition} of the {@link Layer} used in this
	 *         {@link Option}
	 */
	public Definition<CLayer> getOptionalDefinition() {
		return optionDefinition;
	}

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

	@Override
	public String getName() {
		return "OPT";
	}

	@Override
	public String toString() {
		return getName() + "[" + Named.name(optionDefinition.create()) + "]";
	}

	public static <T extends Layer> Definition<Option<T>> define(Definition<T> definition) {
		return define(definition, Quantifier.GREEDY);
	}

	public static <T extends Layer> Definition<Option<T>> define(Definition<T> definition, Quantifier quantifier) {
		return new Definition<Option<T>>() {

			private final RegexRecursivityLimiter regexComputer = new RegexRecursivityLimiter(
					() -> "(?:" + definition.getRegex() + ")?" + quantifier.getDecorator());

			@Override
			public String getRegex() {
				return regexComputer.generate();
			}

			@Override
			public Option<T> create() {
				return new Option<T>(definition, quantifier);
			}
		};
	}
}
