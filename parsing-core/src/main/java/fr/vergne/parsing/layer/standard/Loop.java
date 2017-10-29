package fr.vergne.parsing.layer.standard;

import java.util.Collection;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.util.Named;

/**
 * A {@link Loop} is a {@link Layer} representing a variable number of
 * occurrences following a given pattern. It is well suited for repeated
 * sequences of similar elements, like an ordered list of variable length. At
 * the opposite of a {@link Sequence}, which considers a static sequence of
 * (possibly) different elements, a {@link Loop} considers a variable
 * repetitions of a single pattern, possibly constrained in size.<br/>
 * <br/>
 * It is common to deal with sequences having separators, like an ordered list
 * of numbers separated by comas or spaces. In such a case, prefer to use a
 * {@link SeparatedLoop} which comes in hand to manage these separators and get
 * rid of them to get only the listed elements.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <Element>
 */
// TODO Split bounded & unbounded loops or remove clear()
// TODO Test bounds exceptions
public interface Loop<Element extends Layer> extends Layer, Iterable<Element>, Named {

	public Definition<Element> getItemDefinition();

	public int getMin();

	public int getMax();

	public Quantifier getQuantifier();

	/**
	 * 
	 * @return the number of {@link Element}s in this {@link Loop}
	 */
	public int size();

	/**
	 * 
	 * @return <code>true</code> if this {@link Loop} has no {@link Element} (empty
	 *         content), <code>false</code> otherwise
	 */
	public boolean isEmpty();

	/**
	 * 
	 * @param index
	 *            the index of an {@link Element} in this {@link Loop}
	 * @return the {@link Element} at this index
	 */
	public Element get(int index);

	/**
	 * This method adds a new {@link Element} to this {@link Loop}. The
	 * {@link Element} should have the same regex than the usual {@link Element} s
	 * of this {@link Loop}.
	 * 
	 * @param index
	 *            the index of the new {@link Element}
	 * @param element
	 *            the new {@link Element}
	 * @throws BoundException
	 *             if the addition implies to reach a size above the maximum
	 */
	public void add(int index, Element element) throws BoundException;

	/**
	 * This method adds a new {@link Element} to this {@link Loop}. The content
	 * should be compatible with the regex of the {@link Element}s of this
	 * {@link Loop}.
	 * 
	 * @param index
	 *            the index of the new occurrence
	 * @param content
	 *            the content of this new occurrence
	 * @return the new {@link Element}
	 * @throws BoundException
	 *             if the addition implies to reach a size above the maximum
	 */
	public Element add(int index, String content) throws BoundException;

	/**
	 * This method adds new {@link Element}s to this {@link Loop}. The
	 * {@link Element}s should all have the same regex than the usual
	 * {@link Element}s of this {@link Loop}.<br/>
	 * <br/>
	 * For providing a collection of {@link String}s, use
	 * {@link #addAllContents(int, Collection)}, which has a different name to avoid
	 * type erasure issues.
	 * 
	 * @param index
	 *            the index form which to start the addition
	 * @param elements
	 *            the new {@link Element}s
	 * @throws BoundException
	 *             if the addition implies to reach a size above the maximum
	 */
	public void addAll(int index, Collection<Element> elements) throws BoundException;

	/**
	 * This method adds new {@link Element}s to this {@link Loop}. Each content
	 * should be compatible with the regex of the {@link Element}s of this
	 * {@link Loop}.
	 * 
	 * @param index
	 *            the index from which to start the addition
	 * @param contents
	 *            the contents of the new occurrences
	 * @return the new {@link Element}s
	 * @throws BoundException
	 *             if the addition implies to reach a size above the maximum
	 */
	public Collection<Element> addAllContents(int index, Collection<String> contents) throws BoundException;

	/**
	 * 
	 * @param index
	 *            the index of an {@link Element} to remove from this {@link Loop}
	 * @return {@link Element} removed
	 * @throws BoundException
	 *             if the removal implies to reach a size below the minimum
	 */
	public Element remove(int index) throws BoundException;

	public void clear() throws BoundException;

	@Override
	default String getName() {
		return "LOOP";
	}

	@SuppressWarnings("serial")
	public static class BoundException extends RuntimeException {
		public BoundException(String message, Throwable cause) {
			super(message, cause);
		}

		public BoundException(String message) {
			super(message);
		}
	}
}
