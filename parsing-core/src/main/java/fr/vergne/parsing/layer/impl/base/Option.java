package fr.vergne.parsing.layer.impl.base;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.AbstractLayer;

/**
 * An {@link Option} make a {@link Layer} optional. Thus, a compatible content
 * is one which fits the optional {@link Layer} or an empty one.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <CLayer>
 */
public class Option<CLayer extends Layer> extends AbstractLayer implements
		Layer {

	private final CLayer option;
	private boolean isPresent = false;

	public Option(CLayer layer) {
		this.option = layer;
	}

	@Override
	protected String buildRegex() {
		return "(?:" + option.getRegex() + ")?";
	}

	@Override
	public String getContent() {
		return isPresent() ? option.getContent() : "";
	}

	@Override
	protected void setInternalContent(String content) {
		if (content.isEmpty()) {
			isPresent = false;
		} else {
			try {
				option.setContent(content);
				isPresent = true;
			} catch (ParsingException e) {
				throw new ParsingException(this, option, content, 0,
						content.length(), e);
			}
		}
	}

	/**
	 * 
	 * @return <code>true</code> if the current content fits the option,
	 *         <code>false</code> otherwise (empty content)
	 */
	public boolean isPresent() {
		return isPresent;
	}

	/**
	 * 
	 * @return the option wrapped by this {@link Option}, whether it is used or
	 *         not
	 */
	public CLayer getOption() {
		return option;
	}

	@Override
	public String toString() {
		return option.toString() + "(opt)";
	}
}
