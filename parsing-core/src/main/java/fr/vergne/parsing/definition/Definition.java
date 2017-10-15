package fr.vergne.parsing.definition;

import fr.vergne.parsing.layer.Layer;

/**
 * A {@link Definition} defines a {@link Layer} in order to be able to
 * instantiate it through {@link #create()}.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 *
 * @param <T>
 */
public interface Definition<T extends Layer> {
	/**
	 * This method provides the complete regular expression which represents the
	 * {@link Layer}s we instantiate. If capturing parenthesis are used, they are
	 * not capturing ("(?:...)") to not interfere with parsing processes.
	 * 
	 * @return the regular expression describing the {@link Layer}s instantiated by
	 *         this {@link Definition}
	 */
	public String getRegex();

	/**
	 * Each call to this method instantiates a {@link Layer} corresponding to this
	 * {@link Definition}.
	 * 
	 * @return a new {@link Layer} instance
	 */
	public T create();

	/**
	 * 
	 * @param layer
	 *            the {@link Layer} to evaluate
	 * @return <code>true</code> if this {@link Layer} is equivalent to one created
	 *         through {@link #create()}, false otherwise
	 */
	public boolean isCompatibleWith(T layer);
}
