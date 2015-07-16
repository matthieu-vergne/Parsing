package fr.vergne.parsing.layer.standard;

import fr.vergne.parsing.layer.Layer;

/**
 * A {@link Quantifier} is a decorator which is used in regex to improve
 * performances. For instance, for a {@link Layer} equivalent to the regex X*,
 * it is possible to optimize the regex computation by choosing the most
 * suitable case between:
 * <ul>
 * <li>lazy: X*?</li>
 * <li>greedy: X* (default)</li>
 * <li>possessive: X*+</li>
 * </ul>
 * These are regex-specific tools, so no detail is given here. But one can found
 * plenty of information about these quantifiers on the web, like:<br/>
 * <a href="http://www.regular-expressions.info/possessive.html">http://www.
 * regular-expressions.info/possessive.html</a>
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public enum Quantifier {
	GREEDY(""), LAZY("?"), POSSESSIVE("+");

	private final String decorator;

	private Quantifier(String decorator) {
		this.decorator = decorator;
	}

	public String getDecorator() {
		return decorator;
	}
}
