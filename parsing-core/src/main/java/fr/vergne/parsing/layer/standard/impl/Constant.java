package fr.vergne.parsing.layer.standard.impl;

import java.util.regex.Pattern;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.util.Named;

/**
 * An {@link Constant} is a {@link Layer} representing a static piece of text.
 * This is particularly suited to represent keywords and other pieces of text we
 * are expected to find exactly as-is.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class Constant extends Regex implements Named {

	public Constant(String content) {
		super(Pattern.quote(content));
		setContent(content);
	}

	@Override
	public String getName() {
		return "CONST";
	}

	@Override
	public String toString() {
		return getName() + "[" + getContent() + "]";
	}
}
