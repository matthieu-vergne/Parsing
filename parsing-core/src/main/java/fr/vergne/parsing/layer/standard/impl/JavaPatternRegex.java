package fr.vergne.parsing.layer.standard.impl;

import java.io.InputStream;
import java.util.regex.Pattern;

import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.AbstractLayer;
import fr.vergne.parsing.layer.standard.Regex;
import fr.vergne.parsing.util.ContentInputStream;

// TODO Doc
public class JavaPatternRegex extends AbstractLayer implements Regex {

	private final String regex;
	private String content;

	/**
	 * Create a {@link JavaPatternRegex} without any content yet.
	 * 
	 * @param regex
	 *            the regex of this {@link JavaPatternRegex}
	 */
	public JavaPatternRegex(String regex) {
		this.regex = regex;
	}

	/**
	 * Create a {@link JavaPatternRegex} with an initial content. The content should be
	 * compatible with the regex provided.
	 * 
	 * @param regex
	 *            the regex of this {@link JavaPatternRegex}
	 * @param content
	 *            the content of this {@link JavaPatternRegex}
	 */
	public JavaPatternRegex(String regex, String content) {
		this(regex);
		setContent(content);
	}

	@Override
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
	public String toString() {
		return getName() + "[" + regex + "]";
	}
}
