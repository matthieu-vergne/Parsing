package fr.vergne.parsing.layer.standard;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import fr.vergne.ioutils.StringUtils;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.util.Any;

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
	public static int recursivityDepth = 10;
	private static final Charset ENCODING = Charset.forName("UTF-8");
	private static final Map<Class<?>, Integer> calls = new HashMap<Class<?>, Integer>();
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
		if (content == null) {
			throw new NullPointerException("No content has been provided.");
		} else {
			setInternalContent(content);
			fireContentUpdate(content);
		}
	}

	/**
	 * This method allows to notify the {@link ContentListener}s registered
	 * through
	 * {@link #addContentListener(fr.vergne.parsing.layer.Layer.ContentListener)}
	 * that the content of the {@link Layer} has changed. If you plan to call
	 * {@link #getContent()} in order to generate the argument of this method,
	 * prefer to call {@link #fireContentUpdate()} to call it only if necessary.
	 * 
	 * @param newContent
	 *            the new content of this {@link Layer}
	 */
	protected void fireContentUpdate(String newContent) {
		for (ContentListener listener : listeners) {
			listener.contentSet(newContent);
		}
	}

	/**
	 * This method is equivalent to {@link #fireContentUpdate(String)}, excepted
	 * that:
	 * <ul>
	 * <li>the content is retrieved from {@link #getContent()}</li>
	 * <li>nothing is done if no listener is registered</li>
	 * </ul>
	 * Consequently, this method is more suited to cases where the content is
	 * not build yet: if no listeners is registered, then no content will be
	 * generated.
	 */
	protected void fireContentUpdate() {
		if (listeners.isEmpty()) {
			// do not do anything
		} else {
			String newContent = getContent();
			for (ContentListener listener : listeners) {
				listener.contentSet(newContent);
			}
		}
	}

	protected abstract void setInternalContent(String content);

	@Override
	public final String getRegex() {
		synchronized (calls) {
			Class<? extends AbstractLayer> clazz = getClass();
			int value = calls.containsKey(clazz) ? calls.get(clazz) : 0;
			if (value >= recursivityDepth) {
				return new Any().getRegex();
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

	@Override
	public String getContent() {
		InputStream stream;
		try {
			stream = getInputStream();
		} catch (NoContentException e) {
			return null;
		}
		String content;
		try {
			content = StringUtils.readFromInputStream(stream, ENCODING);
			stream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return content;
	}
}
