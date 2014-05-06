package fr.vergne.parsing.layer.standard;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
		for (Layer alternative : alternatives) {
			try {
				alternative.setContent(content);
				currentAlternative = alternatives.indexOf(alternative);
				return;
			} catch (ParsingException e) {
				// try another
			}
		}
		throw new ParsingException(getRegex(), content);
	}

	@Override
	public String toString() {
		List<String> choices = new LinkedList<String>();
		for (Layer layer : alternatives) {
			choices.add(layer.getClass().getSimpleName());
		}
		return "CHOOSE" + choices;
	}

	/**
	 * 
	 * @return the alternative corresponding to the current content
	 */
	public Layer getCurrent() {
		return alternatives.get(currentAlternative);
	}
}
