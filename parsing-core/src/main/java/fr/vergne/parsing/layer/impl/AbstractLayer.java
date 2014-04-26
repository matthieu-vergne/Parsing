package fr.vergne.parsing.layer.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import fr.vergne.parsing.layer.Layer;

public abstract class AbstractLayer implements Layer {

	/**
	 * {@link Layer}s are used in other layers to build tree structures.
	 * However, it is frequent that going deep in the content of a {@link Layer}
	 * , we find that this same {@link Layer} is used recursively, creating a
	 * graph with loops. This variable provides the limit before to stop going
	 * deeper. Once this limit is reached, the regex of the deepest level is
	 * considered to be a generic ".*". Notice that this limit is the number of
	 * time we call the same {@link Layer}, not the number of layer. Thus, as
	 * long as it is not recursive, a really deep structure is completely
	 * browsed.
	 */
	public static int recursivityDepth = 3;
	private final Collection<ContentListener> listeners = new HashSet<ContentListener>();

	@Override
	public void addContentListener(ContentListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeContentListener(ContentListener listener) {
		listeners.remove(listener);
	}

	@Override
	public final void setContent(String content) {
		if (listeners.isEmpty()) {
			setInternalContent(content);
		} else {
			String oldValue = getContent();
			setInternalContent(content);
			for (ContentListener listener : listeners) {
				listener.contentSet(oldValue, content);
			}
		}
	}

	protected abstract void setInternalContent(String content);

	private static final Map<Class<?>, Integer> calls = new HashMap<Class<?>, Integer>();

	@Override
	public String getRegex() {
		Class<? extends AbstractLayer> clazz = getClass();
		int value = calls.containsKey(clazz) ? calls.get(clazz) : 0;
		if (value >= recursivityDepth) {
			return ".*";
		} else {
			calls.put(clazz, value + 1);
			String regex = buildRegex();
			if (value == 0) {
				calls.remove(clazz);
			} else {
				calls.put(clazz, value);
			}
			return regex;
		}
	}

	/**
	 * This method should provide the complete regular expression which
	 * represents this {@link Layer}. However, capturing parenthesis are
	 * forbidden to avoid conflict with future uses of such captures (but using
	 * "(?:...)" is allowed, as it does not conflict).
	 * 
	 * @return the undecorated regular expression representing this
	 *         {@link Layer}
	 */
	protected abstract String buildRegex();

}
