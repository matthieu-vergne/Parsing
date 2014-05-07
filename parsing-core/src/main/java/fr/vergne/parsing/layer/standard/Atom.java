package fr.vergne.parsing.layer.standard;

import java.util.regex.Pattern;

import fr.vergne.parsing.layer.Layer;

/**
 * An {@link Atom} is a {@link Layer} representing a static piece of text. This
 * is particularly suited to represent keywords and other pieces of text we are
 * expected to find exactly as-is.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class Atom extends Formula {

	private final String content;

	public Atom(String content) {
		super(Pattern.quote(content));
		this.content = content;
	}

	@Override
	public String toString() {
		return "ATOM[" + content + "]";
	}
}
