package fr.vergne.parsing.layer.standard;

import java.io.InputStream;
import java.util.regex.Pattern;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.AbstractLayer;
import fr.vergne.parsing.util.ContentInputStream;
import fr.vergne.parsing.util.Named;

/**
 * An {@link Constant} is a {@link Layer} representing a static piece of text.
 * This is particularly suited to represent keywords, punctuation, and other
 * pieces of text we expect to find exactly as-is.
 */
public class Constant extends AbstractLayer implements Named {

	private final String constant;

	public Constant(String content) {
		this.constant = content;
	}

	@Override
	public InputStream getInputStream() throws NoContentException {
		return new ContentInputStream(constant);
	}

	@Override
	protected void setInternalContent(String content) {
		if (content.equals(this.constant)) {
			// OK
		} else {
			throw new ParsingException(define(constant).getRegex(), content);
		}
	}

	/**
	 * This method provides the reference content of this {@link Constant}. As
	 * opposed to {@link #getContent()}, which may be set or not, this one is always
	 * set and always return the same value. If the content is not set, it is for
	 * example always possible to set a valid content by calling
	 * <code>setContent(getConstant())</code>.
	 * 
	 * @return the expected content of this {@link Constant}
	 */
	public String getConstant() {
		return constant;
	}

	@Override
	public String getName() {
		return "CONST";
	}

	@Override
	public String toString() {
		return getName() + "[" + constant + "]";
	}

	public static Definition<Constant> define(String content) {
		return new Definition<Constant>() {

			@Override
			public String getRegex() {
				return Pattern.quote(content);
			}

			@Override
			public Constant create() {
				return new Constant(content);
			}
		};
	}
}
