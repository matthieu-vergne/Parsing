package fr.vergne.parsing.layer.standard;

import java.util.Collection;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.util.Named;

/**
 * A {@link Choice} is a {@link Layer} representing a piece of text which can
 * have different types of content, like an ID which can be a number or a name.
 * In the case of a pure syntaxic variability (e.g. different representations of
 * a same number), it is better to use a {@link Regex}.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
public interface Choice extends Layer, Named {

	/**
	 * 
	 * @return the number of {@link Definition}s managed by this {@link Choice}
	 */
	public int size();

	/**
	 * 
	 * @param index
	 *            the {@link Definition} index
	 * @return the {@link Definition}
	 */
	public Definition<?> getDefinition(int index);

	/**
	 * 
	 * @return the {@link Definition}s covered by this {@link Choice}
	 */
	public Collection<Definition<?>> getDefinitions();

	/**
	 * 
	 * @return the {@link Definition} corresponding to the current content
	 */
	public Definition<?> getCurrentDefinition();

	/**
	 * 
	 * @param definition
	 *            the {@link Definition} to check
	 * @return <code>true</code> if the current content is considered as this
	 *         {@link Definition}, <code>false</code> otherwise
	 */
	public boolean is(Definition<?> definition);

	/**
	 * {@link Choice} can receive the content of various {@link Layer}s. As such,
	 * {@link #get()} returns a generic {@link Layer}. This method allows to specify
	 * the specific {@link Definition} we expect it to be, thus obtaining the right
	 * type of {@link Layer}.
	 * 
	 * @param definition
	 *            the {@link Definition} corresponding to the content
	 * @return the {@link Layer} currently used with the current content
	 * @throws InvalidChoiceException
	 *             if the provided {@link Definition} is not one of the available
	 *             choices
	 */
	public <Item extends Layer> Item getAs(Definition<Item> definition) throws InvalidChoiceException;

	/**
	 * 
	 * @return the {@link Layer} currently used with the current content
	 */
	public Layer get();

	/**
	 * This method aims at providing a convenient way to improve parsing exception.
	 * A {@link Choice}, by essence, is prone to have many alternatives which does
	 * not fit (throwing a {@link ParsingException}) and a few alternatives which
	 * fits (applying the content successfully). Thus, throwing exceptions is
	 * expected, but in the case where no alternative fits, it makes a lot of
	 * exceptions with each its different message. In such a case, a generic
	 * exception is thrown, loosing all the information provided by the specific
	 * ones. To avoid that, one can set a reference alternative through this method
	 * in order to get the exception of this specific alternative (only in the case
	 * where no alternative fits).
	 * 
	 * @param alternative
	 *            the alternative to consider as reference
	 * @throws InvalidChoiceException
	 *             if the provided {@link Definition} is not one of the available
	 *             choices
	 */
	public void setReferenceDefinition(Definition<?> alternative) throws InvalidChoiceException;

	/**
	 * 
	 * @return the alternative set as reference, <code>null</code> by default
	 * @see #setReferenceDefinition(Layer)
	 */
	public Definition<?> getReferenceDefinition();

	@Override
	default String getName() {
		return "CHOICE";
	}

	@SuppressWarnings("serial")
	public class InvalidChoiceException extends IllegalArgumentException {

		public InvalidChoiceException(Definition<?> wrongDefinition) {
			super("Invalid choice: " + wrongDefinition.create());
		}

		public InvalidChoiceException(Definition<?> correctDefinition, Definition<?> wrongDefinition) {
			super("Invalid choice: " + wrongDefinition.create() + " instead of " + correctDefinition.create());
		}

	}
}
