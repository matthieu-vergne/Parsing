package fr.vergne.parsing.layer.standard.impl;

import java.io.InputStream;
import java.util.regex.Pattern;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.AbstractLayer;
import fr.vergne.parsing.util.ContentInputStream;
import fr.vergne.parsing.util.Named;

/**
 * A {@link Regex} is a {@link Layer} which represents a piece of text which
 * respect syntaxic rules provided by a single formula, such as a number which
 * can have different representations (with/out fractional part, binary, octal,
 * scientific, etc.). If you want to consider variability in the semantics (e.g.
 * a number or a name), you should prefer other kinds of {@link Layer}s like
 * {@link Choice}s or {@link Loop}s.<br/>
 * <br/>
 * Note: Basically, a {@link Regex} is a regular expression. However, to avoid
 * confusing between an instance of this class and the {@link String} returned
 * by {@link #getRegex()}, the name was changed. Moreover, it is not rejected to
 * use different types of formulas for future implementations, leading to the
 * current name.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class Regex extends AbstractLayer implements Named {

	private final String regex;
	private String content;

	/**
	 * Create a {@link Regex} without any content yet.
	 * 
	 * @param regex
	 *            the regex of this {@link Regex}
	 */
	public Regex(String regex) {
		this.regex = regex;
	}

	/**
	 * Create a {@link Regex} with an initial content. The content should be
	 * compatible with the regex provided.
	 * 
	 * @param regex
	 *            the regex of this {@link Regex}
	 * @param content
	 *            the content of this {@link Regex}
	 */
	public Regex(String regex, String content) {
		this(regex);
		setContent(content);
	}

	public String getRegex() {
		return regex;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public InputStream getInputStream() {
		if (content == null) {
			throw new NoContentException();
		} else {
			return new ContentInputStream(content);
		}
	}

	@Override
	protected void setInternalContent(String content) {
		if (Pattern.matches("^" + regex + "$", content)) {
			this.content = content;
		} else {
			throw new ParsingException(regex, content);
		}
	}

	@Override
	public String getName() {
		return "REGEX";
	}

	@Override
	public String toString() {
		return getName() + "[" + regex + "]";
	}
}
