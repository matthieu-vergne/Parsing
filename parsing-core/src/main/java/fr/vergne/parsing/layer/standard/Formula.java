package fr.vergne.parsing.layer.standard;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;

/**
 * A {@link Formula} is a {@link Layer} which represents a piece of text which
 * respect syntaxic rules provided by a single formula, such as a number which
 * can have different representations (with/out fractional part, binary, octal,
 * scientific, etc.). If you want to consider variability in the semantics (e.g.
 * a number or a name), you should prefer other kinds of {@link Layer}s like
 * {@link Choice}s or {@link Loop}s.<br/>
 * <br/>
 * Note: Basically, a {@link Formula} is a regular expression. However, to avoid
 * confusing between an instance of this class and the {@link String} returned
 * by {@link #getRegex()}, the name was changed. Moreover, it is not rejected to
 * use different types of formulas for future implementations, leading to the
 * current name.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public class Formula extends AbstractLayer {

	private final String regex;
	private String content;

	/**
	 * Create a {@link Formula} without any content yet.
	 * 
	 * @param regex
	 *            the regex of this {@link Formula}
	 */
	public Formula(String regex) {
		this.regex = regex;
	}

	/**
	 * Create a {@link Formula} with an initial content. The content should be
	 * compatible with the regex provided.
	 * 
	 * @param regex
	 *            the regex of this {@link Formula}
	 * @param content
	 *            the content of this {@link Formula}
	 */
	public Formula(String regex, String content) {
		this(regex);
		setContent(content);
	}

	@Override
	protected String buildRegex() {
		return regex;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public InputStream getInputStream() {
		return IOUtils.toInputStream(content, Charset.forName("UTF-8"));
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
		return getClass().getSimpleName() + "[" + regex + "]";
	}

	@Override
	public Object clone() {
		Formula formula = new Formula(getRegex());
		String content = getContent();
		if (content != null) {
			formula.setContent(content);
		} else {
			// keep it not filled
		}
		return formula;
	}
}
