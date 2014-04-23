package fr.vergne.parsing.layer.impl;

import java.util.Collection;
import java.util.HashSet;

import fr.vergne.parsing.layer.Layer;

public abstract class AbstractLayer implements Layer {

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

}
