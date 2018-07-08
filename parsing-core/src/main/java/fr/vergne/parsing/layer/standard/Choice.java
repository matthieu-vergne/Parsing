package fr.vergne.parsing.layer.standard;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.AbstractLayer;
import fr.vergne.parsing.util.Named;
import fr.vergne.parsing.util.RegexRecursivityLimiter;

/**
 * A {@link Choice} is a {@link Layer} representing a piece of text which can
 * have different types of content, like an ID which can be a number or a name.
 * In the case of a pure syntaxic variability (e.g. different representations of
 * a same number), it is better to use a {@link Regex}.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class Choice extends AbstractLayer implements Named {

	private final List<Definition<? extends Layer>> definitions;
	private Layer currentLayer = null;
	private Integer currentDefinitionIndex = null;
	private Integer referenceDefinitionIndex = null;
	private final ContentListener deepListener = new ContentListener() {

		@Override
		public void contentSet(String newContent) {
			fireContentUpdate(newContent);
		}
	};

	public Choice(Collection<Definition<? extends Layer>> definitions) {
		this.definitions = Collections.unmodifiableList(new ArrayList<>(definitions));
	}

	@SafeVarargs
	public Choice(Definition<? extends Layer>... definitions) {
		this(Arrays.asList(definitions));
	}

	@Override
	public InputStream getInputStream() {
		return currentLayer.getInputStream();
	}

	@Override
	protected void setInternalContent(String content) {
		if (currentLayer != null) {
			currentLayer.removeContentListener(deepListener);
		} else {
			// No listener to remove
		}

		Map<Definition<?>, ParsingException> exceptions = new HashMap<>();
		for (Definition<? extends Layer> definition : definitions) {
			try {
				Layer layer = definition.create();
				layer.setContent(content);
				currentLayer = layer;
				currentLayer.addContentListener(deepListener);
				currentDefinitionIndex = definitions.indexOf(definition);
				return;
			} catch (ParsingException e) {
				exceptions.put(definition, e);
			}
		}
		if (referenceDefinitionIndex != null) {
			Definition<?> reference = definitions.get(referenceDefinitionIndex);
			ParsingException cause = exceptions.get(reference);
			throw new ParsingException(this, reference, content, cause.getStart(), content.length(), cause);
		} else {
			throw new ParsingException(define(definitions).getRegex(), content);
		}
	}

	/**
	 * 
	 * @return the number of {@link Definition}s managed by this {@link Choice}
	 */
	public int size() {
		return definitions.size();
	}

	/**
	 * 
	 * @param index
	 *            the {@link Definition} index
	 * @return the {@link Definition}
	 */
	public Definition<?> getDefinition(int index) {
		return definitions.get(index);
	}

	/**
	 * 
	 * @return the {@link Definition}s covered by this {@link Choice}
	 */
	public Collection<Definition<?>> getDefinitions() {
		return Collections.unmodifiableCollection(definitions);
	}

	/**
	 * 
	 * @return the {@link Definition} corresponding to the current content
	 */
	public Definition<?> getCurrentDefinition() {
		return definitions.get(currentDefinitionIndex);
	}

	/**
	 * 
	 * @param definition
	 *            the {@link Definition} to check
	 * @return <code>true</code> if the current content is considered as this
	 *         {@link Definition}, <code>false</code> otherwise
	 */
	public boolean is(Definition<?> definition) {
		return getCurrentDefinition() == definition;
	}

	/**
	 * {@link Choice} can receive the content of various {@link Layer}s. As such,
	 * {@link #get()} returns a generic {@link Layer}. This method allows to specify
	 * the specific {@link Definition} we expect it to be, thus obtaining the right
	 * type of {@link Layer}.
	 * 
	 * @param definition
	 *            the {@link Definition} corresponding to the content
	 * @return the {@link Layer} currently used with the current content
	 * @throws InvalidChoiceException
	 *             if the provided {@link Definition} is not one of the available
	 *             choices
	 */
	@SuppressWarnings("unchecked")
	public <Item extends Layer> Item getAs(Definition<Item> definition) {
		Definition<? extends Layer> currentDefinition = definitions.get(currentDefinitionIndex);
		if (currentDefinition != definition) {
			throw new InvalidChoiceException(currentDefinition, definition);
		} else {
			return (Item) currentLayer;
		}
	}

	/**
	 * 
	 * @return the {@link Layer} currently used with the current content
	 */
	public Layer get() {
		return currentLayer;
	}

	/**
	 * This method aims at providing a convenient way to improve parsing exception.
	 * A {@link Choice}, by essence, is prone to have many alternatives which does
	 * not fit (throwing a {@link ParsingException}) and a few alternatives which
	 * fits (applying the content successfully). Thus, throwing exceptions is
	 * expected, but in the case where no alternative fits, it makes a lot of
	 * exceptions with each its different message. In such a case, a generic
	 * exception is thrown, loosing all the information provided by the specific
	 * ones. To avoid that, one can set a reference alternative through this method
	 * in order to get the exception of this specific alternative (only in the case
	 * where no alternative fits).
	 * 
	 * @param alternative
	 *            the alternative to consider as reference
	 * @throws InvalidChoiceException
	 *             if the provided {@link Definition} is not one of the available
	 *             choices
	 */
	public void setReferenceDefinition(Definition<?> alternative) {
		int index = definitions.indexOf(alternative);
		if (index >= 0) {
			referenceDefinitionIndex = index;
		} else {
			throw new InvalidChoiceException(alternative);
		}
	}

	/**
	 * 
	 * @return the alternative set as reference, <code>null</code> by default
	 * @see #setReferenceDefinition(Layer)
	 */
	public Definition<?> getReferenceDefinition() {
		if (referenceDefinitionIndex == null) {
			throw new NoSuchElementException("No reference has been defined.");
		} else {
			return definitions.get(referenceDefinitionIndex);
		}
	}

	@Override
	public String getName() {
		return "CHOICE";
	}

	@Override
	public String toString() {
		List<String> choices = new LinkedList<String>();
		for (Definition<?> definition : definitions) {
			choices.add(Named.name(definition.create()));
		}
		return getName() + choices;
	}

	@SuppressWarnings("serial")
	public class InvalidChoiceException extends IllegalArgumentException {

		public InvalidChoiceException(Definition<?> wrongDefinition) {
			super("Invalid choice: " + wrongDefinition.create());
		}

		public InvalidChoiceException(Definition<?> correctDefinition, Definition<?> wrongDefinition) {
			super("Invalid choice: " + wrongDefinition.create() + " instead of " + correctDefinition.create());
		}

	}

	public static Definition<Choice> define(Collection<Definition<? extends Layer>> definitions) {
		return new Definition<Choice>() {

			private final RegexRecursivityLimiter regexComputer = new RegexRecursivityLimiter(() -> {
				StringBuilder regex = new StringBuilder();
				for (Definition<? extends Layer> definition : definitions) {
					regex.append("|(?:" + definition.getRegex() + ")");
				}
				return "(?:" + regex.substring(1) + ")";
			});

			@Override
			public String getRegex() {
				return regexComputer.generate();
			}

			@Override
			public Choice create() {
				return new Choice(definitions);
			}
		};
	}

	@SafeVarargs
	public static final Definition<Choice> define(Definition<? extends Layer>... definitions) {
		return define(Arrays.asList(definitions));
	}
}
