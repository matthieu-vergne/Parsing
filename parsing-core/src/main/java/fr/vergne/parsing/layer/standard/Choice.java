package fr.vergne.parsing.layer.standard;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;

/**
 * A {@link Choice} is a {@link Layer} representing a piece of text which can
 * have different types of content, like an ID which can be a number or a name.
 * In the case of a pure syntaxic variability (e.g. different representations of
 * a same number), it is better to use a {@link Formula}.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class Choice extends AbstractLayer {

	private final List<? extends Layer> alternatives;
	private Integer currentAlternative = null;
	private Integer referenceAlternativeIndex = null;

	public Choice(Collection<? extends Layer> alternatives) {
		this.alternatives = Collections.unmodifiableList(new LinkedList<Layer>(
				alternatives));
	}

	public Choice(Layer... choices) {
		this(Arrays.asList(choices));
	}

	@Override
	protected String buildRegex() {
		String regex = "";
		for (Layer layer : alternatives) {
			regex += "|(?:" + layer.getRegex() + ")";
		}
		return "(?:" + regex.substring(1) + ")";
	}

	@Override
	public String getContent() {
		return getCurrent().getContent();
	}

	@Override
	protected void setInternalContent(String content) {
		Map<Layer, ParsingException> exceptions = new HashMap<Layer, ParsingException>();
		for (Layer alternative : alternatives) {
			try {
				alternative.setContent(content);
				currentAlternative = alternatives.indexOf(alternative);
				return;
			} catch (ParsingException e) {
				exceptions.put(alternative, e);
			}
		}
		if (referenceAlternativeIndex != null) {
			Layer reference = alternatives.get(referenceAlternativeIndex);
			ParsingException cause = exceptions.get(reference);
			throw new ParsingException(this, reference, content, cause.getStart(),
					content.length(), cause);
		} else {
			throw new ParsingException(getRegex(), content);
		}
	}

	/**
	 * 
	 * @return the number of alternatives managed by this {@link Choice}
	 */
	public int size() {
		return alternatives.size();
	}

	/**
	 * 
	 * @param index
	 *            the alternative index
	 * @return the alternative
	 */
	public Layer getAlternative(int index) {
		return alternatives.get(index);
	}

	/**
	 * 
	 * @return the alternative corresponding to the current content
	 */
	public Layer getCurrent() {
		return alternatives.get(currentAlternative);
	}

	/**
	 * This method aims at providing a convenient way to improve parsing
	 * exception. A {@link Choice}, by essence, is prone to have many
	 * alternatives which does not fit (throwing a {@link ParsingException}) and
	 * a few alternatives which fits (applying the content successfully). Thus,
	 * throwing exceptions is expected, but in the case where no alternative
	 * fits, it makes a lot of exceptions with each its different message. In
	 * such a case, a generic exception is thrown, loosing all the information
	 * provided by the specific ones. To avoid that, one can set a reference
	 * alternative through this method in order to get the exception of this
	 * specific alternative (only in the case where no alternative fits).
	 * 
	 * @param alternative
	 *            the alternative to consider as reference
	 */
	public void setReferenceAlternative(Layer alternative) {
		int index = alternatives.indexOf(alternative);
		if (index >= 0) {
			referenceAlternativeIndex = index;
		} else {
			throw new NoSuchElementException("No alternative corresponds to "
					+ alternative);
		}
	}

	/**
	 * 
	 * @return the alternative set as reference, <code>null</code> by default
	 * @see #setReferenceAlternative(Layer)
	 */
	public Layer getReferenceAlternative() {
		if (referenceAlternativeIndex == null) {
			throw new NoSuchElementException("No reference has been defined.");
		} else {
			return alternatives.get(referenceAlternativeIndex);
		}
	}

	@Override
	public String toString() {
		List<String> choices = new LinkedList<String>();
		for (Layer layer : alternatives) {
			choices.add(layer.getClass().getSimpleName());
		}
		return "CHOICE" + choices;
	}
}
