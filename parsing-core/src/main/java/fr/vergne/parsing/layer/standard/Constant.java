package fr.vergne.parsing.layer.standard;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.util.Named;

/**
 * An {@link Constant} is a {@link Layer} representing a static piece of text.
 * This is particularly suited to represent keywords, punctuation, and other
 * pieces of text we expect to find exactly as-is.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public interface Constant extends Layer, Named {

	/**
	 * This method provides the reference content of this {@link Constant}. As
	 * opposed to {@link #getContent()}, which may be set or not, this one is always
	 * set and always return the same value. If the content is not set, it is for
	 * example always possible to set a valid content by calling
	 * <code>setContent(getConstant())</code>.
	 * 
	 * @return the expected content of this {@link Constant}
	 */
	public String getConstant();

	@Override
	default String getName() {
		return "CONST";
	}

}
