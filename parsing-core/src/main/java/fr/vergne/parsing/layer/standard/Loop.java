package fr.vergne.parsing.layer.standard;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.vergne.logging.LoggerConfiguration;
import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.AbstractLayer;
import fr.vergne.parsing.util.Named;
import fr.vergne.parsing.util.RegexRecursivityLimiter;

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
public class Loop<Element extends Layer> extends AbstractLayer implements Iterable<Element>, Named {

	// TODO make it private
	public static final Logger log = LoggerConfiguration.getSimpleLogger();

	private final Definition<Element> itemDefinition;
	private final int min;
	private final int max;
	private final Quantifier quantifier;
	private List<Element> occurrences;
	private final ContentListener deepListener = new ContentListener() {

		@Override
		public void contentSet(String newContent) {
			fireContentUpdate();
		}
	};

	/**
	 * Instantiate an optimized {@link Loop} (through the {@link Quantifier}) which
	 * uses a {@link Definition} to instantiate the occurrences it will find. Notice
	 * that requesting two times the same occurrence, for instance calling
	 * {@link #get(int)} with the same index, should provide the same instance as
	 * long as the corresponding content has not been modified. If the
	 * {@link Definition} returns always the same instance(s), it will not work
	 * properly.
	 * 
	 * @param definition
	 *            the occurrences {@link Definition}
	 * @param min
	 *            the minimum size of this {@link Loop}, at least 0
	 * @param max
	 *            the maximum size of this {@link Loop}, at most
	 *            {@link Integer#MAX_VALUE}
	 * @param quantifier
	 *            type of {@link Quantifier} to use to optimize the regex
	 */
	public Loop(Definition<Element> definition, int min, int max, Quantifier quantifier) {
		if (quantifier == null) {
			throw new NullPointerException("No quantifier provided");
		} else if (definition == null) {
			throw new NullPointerException("No occurrence definition provided");
		} else if (min < 0) {
			throw new IllegalArgumentException("The minimum should be positive: " + min);
		} else if (max < min) {
			throw new IllegalArgumentException(
					"The maximum (" + max + ") should be superior or equal to the minimum (" + min + ")");
		} else {
			this.itemDefinition = definition;
			this.min = min;
			this.max = max;
			this.quantifier = quantifier;
		}
	}

	public Loop(Definition<Element> definition, int min, int max) {
		this(definition, min, max, Quantifier.GREEDY);
	}

	public Loop(Definition<Element> definition) {
		this(definition, 0, Integer.MAX_VALUE);
	}

	public Definition<Element> getItemDefinition() {
		return itemDefinition;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public Quantifier getQuantifier() {
		return quantifier;
	}

	@Override
	protected void setInternalContent(String content) {
		occurrences = new LinkedList<Element>();
		Matcher matcher = Pattern.compile(define(itemDefinition, min, max, quantifier).getRegex()).matcher(content);
		if (matcher.matches()) {
			Iterator<Element> iterator = occurrences.iterator();
			while (iterator.hasNext()) {
				Element element = (Element) iterator.next();
				element.removeContentListener(deepListener);
				iterator.remove();
			}

			String regex = itemDefinition.getRegex();
			matcher = Pattern.compile("(" + regex + ")(?:(?=" + regex + ")|(?=$))").matcher(content);
			while (matcher.find()) {
				String match = matcher.group(1);
				Element occurrence = itemDefinition.create();
				occurrence.setContent(match);
				occurrence.addContentListener(deepListener);
				occurrences.add(occurrence);
			}
		} else {
			matcher = Pattern.compile(itemDefinition.getRegex()).matcher(content);
			int start = 0;
			int count = 0;
			while (matcher.find() && matcher.start() == start) {
				count++;
				if (count > max) {
					throw new ParsingException(this, null, content, start, content.length());
				} else {
					start += matcher.group(0).length();
				}
			}
			if (count < min) {
				throw new ParsingException(this, itemDefinition, content, content.length(), content.length());
			} else {
				try {
					itemDefinition.create().setContent(content.substring(start));
				} catch (ParsingException e) {
					throw new ParsingException(this, itemDefinition, content, start + e.getStart(), content.length(),
							e);
				}
			}
		}
	}

	@Override
	public InputStream getInputStream() {
		/*
		 * We first store the InputStreams to ensure that all of them are available.
		 * This allows to throw NoContentException immediately if the sequence is not
		 * complete.
		 */
		final List<InputStream> streams = new LinkedList<InputStream>();
		for (Layer sublayer : this) {
			streams.add(sublayer.getInputStream());
		}
		return new InputStream() {
			private InputStream reader = new InputStream() {

				@Override
				public int read() throws IOException {
					return -1;
				}
			};
			private Iterator<InputStream> iterator = streams.iterator();

			@Override
			public int read() throws IOException {
				int character = reader.read();
				while (character == -1 && iterator.hasNext()) {
					reader.close();
					reader = iterator.next();
					character = reader.read();
				}
				return character;
			}

			@Override
			public void close() throws IOException {
				reader.close();
				super.close();
			}
		};
	}

	/**
	 * 
	 * @return the number of {@link Element}s in this {@link Loop}
	 */
	public int size() {
		return occurrences.size();
	}

	/**
	 * 
	 * @return <code>true</code> if this {@link Loop} has no {@link Element} (empty
	 *         content), <code>false</code> otherwise
	 */
	public boolean isEmpty() {
		return occurrences.isEmpty();
	}

	/**
	 * 
	 * @param index
	 *            the index of an {@link Element} in this {@link Loop}
	 * @return the {@link Element} at this index
	 */
	public Element get(int index) {
		return occurrences.get(index);
	}

	/**
	 * This method adds a new {@link Element} to the end of this {@link Loop}. The
	 * {@link Element} should have the same regex than the usual {@link Element}s of
	 * this {@link Loop}.
	 * 
	 * @param element
	 *            the new {@link Element}
	 * @throws BoundException
	 *             if the addition implies to reach a size above the maximum
	 */
	public void add(Element element) throws BoundException {
		add(size(), element);
	}

	/**
	 * This method adds a new {@link Element} to the end of this {@link Loop}. The
	 * content should be compatible with the regex of the {@link Element}s of this
	 * {@link Loop}.
	 * 
	 * @param content
	 *            the content of this new occurrence
	 * @return the new {@link Element}
	 * @throws BoundException
	 *             if the addition implies to reach a size above the maximum
	 */
	public Element add(String content) throws BoundException {
		return add(size(), content);
	}

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
	public void add(int index, Element element) throws BoundException {
		addAll(index, Arrays.asList(element));
	}

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
	public Element add(int index, String content) throws BoundException {
		if (size() >= max) {
			throw new BoundException("This loop cannot have more than " + max + " elements.");
		} else {
			Element newElement = itemDefinition.create();
			newElement.setContent(content);
			add(index, newElement);
			return newElement;
		}
	}

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
	public void addAll(int index, Collection<Element> elements) throws BoundException {
		if (size() + elements.size() > max) {
			throw new BoundException("This loop cannot have more than " + max + " elements.");
		} else {
			Collection<Element> validElements = new LinkedList<Element>();
			for (Element element : elements) {
				if (!itemDefinition.isCompatibleWith(element)) {
					throw new IllegalArgumentException("The element (" + element
							+ ") is not compatible with the definition of this loop: " + itemDefinition.getRegex());
				} else if (element.getContent() == null) {
					throw new IllegalArgumentException(
							"You cannot add an element which has no content: set it before to add it to this loop.");
				} else {
					validElements.add(element);
				}
			}

			for (Element element : validElements) {
				element.addContentListener(deepListener);
			}
			occurrences.addAll(index, validElements);
			fireContentUpdate();
		}
	}

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
	public Collection<Element> addAllContents(int index, Collection<String> contents) throws BoundException {
		if (size() >= max) {
			throw new BoundException("This loop cannot have more than " + max + " elements.");
		} else {
			Collection<Element> elements = new LinkedList<Element>();
			for (String content : contents) {
				Element element = itemDefinition.create();
				element.setContent(content);
				elements.add(element);
			}
			addAll(index, elements);
			return elements;
		}
	}

	/**
	 * 
	 * @param index
	 *            the index of an {@link Element} to remove from this {@link Loop}
	 * @return {@link Element} removed
	 * @throws BoundException
	 *             if the removal implies to reach a size below the minimum
	 */
	public Element remove(int index) throws BoundException {
		if (size() <= min) {
			throw new BoundException("This loop cannot have less than " + min + " elements.");
		} else {
			Element removed = occurrences.remove(index);
			removed.removeContentListener(deepListener);
			fireContentUpdate();
			return removed;
		}
	}

	public void clear() throws BoundException {
		if (size() <= min) {
			throw new BoundException("This loop cannot have less than " + min + " elements.");
		} else {
			Iterator<Element> iterator = occurrences.iterator();
			while (iterator.hasNext()) {
				Element removed = iterator.next();
				removed.removeContentListener(deepListener);
				iterator.remove();
			}
			fireContentUpdate("");
		}
	}

	@Override
	public Iterator<Element> iterator() {
		if (occurrences == null) {
			throw new NoContentException();
		} else {
			final Iterator<Element> occurenceIterator = occurrences.iterator();
			return new Iterator<Element>() {

				private Element lastReturned = null;

				@Override
				public boolean hasNext() {
					return occurenceIterator.hasNext();
				}

				@Override
				public Element next() {
					lastReturned = occurenceIterator.next();
					return lastReturned;
				}

				@Override
				public void remove() {
					if (size() <= min) {
						throw new BoundException("This loop cannot have less than " + min + " elements.");
					} else {
						occurenceIterator.remove();
						lastReturned.removeContentListener(deepListener);
						fireContentUpdate();
					}
				}
			};
		}
	}

	@Override
	public String getName() {
		return "LOOP";
	}

	@Override
	public String toString() {
		String cardinality = RegexRecursivityLimiter.buildRegexCardinality(quantifier, min, max);
		return getName() + "[" + Named.name(itemDefinition.create()) + cardinality + "]";
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

	public static <Item extends Layer> Definition<Loop<Item>> define(Definition<Item> itemDefinition, int min, int max,
			Quantifier quantifier) {
		return new Definition<Loop<Item>>() {

			private final RegexRecursivityLimiter regexComputer = new RegexRecursivityLimiter(
					() -> "(?:" + itemDefinition.getRegex() + ")"
							+ RegexRecursivityLimiter.buildRegexCardinality(quantifier, min, max));

			@Override
			public String getRegex() {
				return regexComputer.generate();
			}

			@Override
			public Loop<Item> create() {
				return new Loop<>(itemDefinition, min, max, quantifier);
			}

			@Override
			public boolean isCompatibleWith(Loop<Item> layer) {
				return layer.getItemDefinition().equals(itemDefinition) && layer.getMin() == min
						&& layer.getMax() == max && layer.getQuantifier().equals(quantifier);
			}
		};
	}

	public static <Item extends Layer> Definition<Loop<Item>> define(Definition<Item> item, int min, int max) {
		return define(item, min, max, Quantifier.GREEDY);
	}

	public static <Item extends Layer> Definition<Loop<Item>> define(Definition<Item> item) {
		return define(item, 0, Integer.MAX_VALUE, Quantifier.GREEDY);
	}

	public static <Item extends Layer> Definition<Loop<Item>> define(Definition<Item> item, Quantifier quantifier) {
		return define(item, 0, Integer.MAX_VALUE, quantifier);
	}
}
