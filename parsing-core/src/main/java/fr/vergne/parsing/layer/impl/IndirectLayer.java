package fr.vergne.parsing.layer.impl;

import java.io.InputStream;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;

/**
 * An {@link IndirectLayer} is an implementation which relies on another
 * {@link Layer} instance to provide the actual implementation of the
 * {@link Layer} interface. It is similar to a proxy, excepted that one must
 * extend this class to set its delegate. The main purpose of this class is to
 * ease the creation of new classes of {@link Layer} by relying on existing
 * {@link Definition}s<br>
 * <br>
 * The typical use case of {@link IndirectLayer} is to:
 * <ol>
 * <li>create a custom class, with its own specific methods,
 * <li>make it extends {@link IndirectLayer} in order to implement the
 * {@link Layer} interface,
 * <li>generate a {@link Layer} instance corresponding to this custom class, for
 * example based on standard {@link Definition}s,
 * <li>use {@link #setInnerLayer(Layer)} in the custom class to store this
 * {@link Layer} instance.
 * </ol>
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 *
 * @param <L> The {@link Layer} to use internally
 */
// TODO test
public class IndirectLayer<L extends Layer> implements Layer {

	private L innerLayer;

	public IndirectLayer() {
		this(null);
	}

	public IndirectLayer(L layer) {
		setInnerLayer(layer);
	}

	protected void setInnerLayer(L layer) {
		innerLayer = layer;
	}

	public L getInnerLayer() {
		return innerLayer;
	}

	@Override
	public void setContent(String content) {
		innerLayer.setContent(content);
	}

	@Override
	public String getContent() {
		return innerLayer.getContent();
	}

	@Override
	public InputStream getInputStream() throws NoContentException {
		return innerLayer.getInputStream();
	}

	@Override
	public void addContentListener(ContentListener listener) {
		innerLayer.addContentListener(listener);
	}

	@Override
	public void removeContentListener(ContentListener listener) {
		innerLayer.removeContentListener(listener);
	}

}
