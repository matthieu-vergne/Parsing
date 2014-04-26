package fr.vergne.parsing.layer.impl;

import fr.vergne.parsing.layer.Layer;

/**
 * A {@link GreedyMode} is a decorator which is added to some specific formats
 * of {@link Layer}s. For instance, for a {@link Layer} equivalent to a regex
 * wildcard like X*, it is possible to optimize the regex computation by making
 * it greedy (default), lazy (add '?') or possessive (add '+'). You can find
 * plenty of information about these different optimizations on the web, so no
 * detail is given here. However, this management was introduced on the fly and
 * should be revised in order to have a clean use of such features.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
// TODO revise the use of such feature
public enum GreedyMode {
	GREEDY(""), LAZY("?"), POSSESSIVE("+");

	private final String decorator;

	private GreedyMode(String decorator) {
		this.decorator = decorator;
	}

	public String getDecorator() {
		return decorator;
	}
}
