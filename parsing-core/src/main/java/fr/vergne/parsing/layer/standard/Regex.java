package fr.vergne.parsing.layer.standard;

import fr.vergne.parsing.layer.Layer;
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
public interface Regex extends Layer, Named {

	/**
	 * 
	 * @return the regular expression of this {@link Regex}
	 */
	public String getRegex();

	@Override
	default String getName() {
		return "REGEX";
	}

}
