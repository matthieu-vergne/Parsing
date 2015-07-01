package fr.vergne.parsing.layer.util;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;

import fr.vergne.parsing.layer.Layer;

public class LayerProxy<SubLayer extends Layer> implements Layer {

	private SubLayer layer;
	private Collection<ContentListener> listeners = new HashSet<ContentListener>();
	private ContentListener listener = new ContentListener() {

		@Override
		public void contentSet(String newContent) {
			for (ContentListener listener : listeners) {
				listener.contentSet(newContent);
			}
		}
	};

	public LayerProxy(SubLayer layer) {
		this.layer = layer;
		this.layer.addContentListener(listener);
	}

	public void setLayer(SubLayer layer) {
		if (layer == null) {
			throw new NullPointerException("No layer provided");
		} else {
			this.layer.removeContentListener(listener);
			this.layer = layer;
			this.layer.addContentListener(listener);
			listener.contentSet(this.layer.getContent());
		}
	}

	public SubLayer getLayer() {
		return layer;
	}

	@Override
	public String getRegex() {
		return layer.getRegex();
	}

	@Override
	public void setContent(String content) {
		layer.setContent(content);
	}

	@Override
	public String getContent() {
		return layer.getContent();
	}

	@Override
	public InputStream getInputStream() {
		return layer.getInputStream();
	}

	@Override
	public void addContentListener(ContentListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeContentListener(ContentListener listener) {
		listeners.remove(listener);
	}

}
