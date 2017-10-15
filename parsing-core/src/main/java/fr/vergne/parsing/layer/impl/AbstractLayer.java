package fr.vergne.parsing.layer.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;

import fr.vergne.ioutils.StringUtils;
import fr.vergne.parsing.layer.Layer;

public abstract class AbstractLayer implements Layer {

	/**
	 * {@link Layer}s are used in other layers to build tree structures. However, it
	 * is frequent that going deep in the content of a {@link Layer} , we find that
	 * this same {@link Layer} is used recursively, creating a graph with loops.
	 * This variable provides the limit before to stop going deeper. Once this limit
	 * is reached, the regex of the deepest level is considered to be a generic
	 * ".*". Notice that this limit is the number of time we call the same
	 * {@link Layer}, not the number of layer. Thus, as long as it is not recursive,
	 * a really deep structure is completely browsed.
	 */
	private static final Charset ENCODING = Charset.forName("UTF-8");
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
	 * This method allows to notify the {@link ContentListener}s registered through
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
	 * Consequently, this method is more suited to cases where the content is not
	 * build yet: if no listeners is registered, then no content will be generated.
	 */
	protected void fireContentUpdate() {
		if (listeners.isEmpty()) {
			// do not do anything
		} else {
			fireContentUpdate(getContent());
		}
	}

	protected abstract void setInternalContent(String content);

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
