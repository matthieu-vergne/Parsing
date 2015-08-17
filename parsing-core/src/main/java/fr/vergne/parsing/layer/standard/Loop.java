package fr.vergne.parsing.layer.standard;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.vergne.logging.LoggerConfiguration;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.util.SeparatedLoop;

/**
 * A {@link Loop} is a {@link Layer} representing a variable number of
 * occurrences following a given template. It is well suited for repeated
 * sequences of similar elements, like an ordered list of variable length. At
 * the opposite of a {@link Suite}, which considers a static sequence of
 * (possibly) different elements, a {@link Loop} considers a variable
 * repetitions of a single template, possibly constrained in size.<br/>
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
public class Loop<Element extends Layer> extends AbstractLayer implements
		Iterable<Element> {

	private final int min;
	private final int max;
	private final Generator<Element> generator;
	private final ContentListener deepListener = new ContentListener() {

		@Override
		public void contentSet(String newContent) {
			fireContentUpdate();
		}
	};
	private List<Element> occurrences;
	private final Quantifier quantifier;
	public static final Logger log = LoggerConfiguration.getSimpleLogger();

	/**
	 * Instantiate an optimized {@link Loop} (through the {@link Quantifier})
	 * which uses a {@link Generator} to instantiate the occurrences it will
	 * find. Notice that requesting two times the same occurrence, for instance
	 * calling {@link #get(int)} with the same index, should provide the same
	 * instance as long as the corresponding content has not been modified. If
	 * the {@link Generator} returns always the same instance(s), it will not
	 * work properly.
	 * 
	 * @param quantifier
	 *            type of {@link Quantifier} to use to optimize the regex
	 * @param generator
	 *            the occurrences {@link Generator}
	 * @param min
	 *            the minimum size of this {@link Loop}, at least 0
	 * @param max
	 *            the maximum size of this {@link Loop}, at most
	 *            {@link Integer#MAX_VALUE}
	 */
	public Loop(Quantifier quantifier, Generator<Element> generator, int min,
			int max) {
		if (quantifier == null) {
			throw new NullPointerException("No quantifier provided");
		} else if (generator == null) {
			throw new NullPointerException("No generator provided");
		} else if (min < 0) {
			throw new IllegalArgumentException(
					"The minimum should be positive: " + min);
		} else if (max < min) {
			throw new IllegalArgumentException("The maximum (" + max
					+ ") should be superior or equal to the minimum (" + min
					+ ")");
		} else {
			this.generator = generator;
			this.min = min;
			this.max = max;
			this.quantifier = quantifier;
		}
	}

	/**
	 * Instantiate an optimised {@link Loop} (through the {@link Quantifier}) in
	 * the same way than {@link #Loop(Quantifier, Generator, int, int)}, but by
	 * providing the {@link Layer} to use as template for the occurrences. If
	 * this template is clonable (a {@link #clone()} method is available), a
	 * {@link Generator} is automatically instantiated to use clones of this
	 * template for future occurrences. Otherwise, an exception is thrown.<br/>
	 * <br/>
	 * While standard components (e.g. {@link Formula}, {@link Suite},
	 * {@link Loop}) are well suited for this purpose, pay attention when you
	 * use custom implementations: if they extend standard components but do not
	 * override {@link #clone()}, their clones will be standard components, not
	 * custom ones. This can lead to {@link ClassCastException} issues.
	 * 
	 * @param template
	 *            the {@link Element} to use as a template
	 * @param min
	 *            the minimum size of this {@link Loop}, at least 0
	 * @param max
	 *            the maximum size of this {@link Loop}, at most
	 *            {@link Integer#MAX_VALUE}
	 * @throws IllegalArgumentException
	 *             if the template is not clonable
	 */
	public Loop(Quantifier quantifier, Element template, int min, int max) {
		this(quantifier, createGeneratorFromTemplate(template), min, max);
	}

	/**
	 * Same as {@link #Loop(Quantifier, Generator, int, int)} with no particular
	 * optimization ({@link Quantifier#GREEDY}).
	 */
	public Loop(Generator<Element> generator, int min, int max) {
		this(Quantifier.GREEDY, generator, min, max);
	}

	/**
	 * Same as {@link #Loop(Quantifier, Layer, int, int)} with no particular
	 * optimization ({@link Quantifier#GREEDY}).
	 */
	public Loop(Element template, int min, int max) {
		this(Quantifier.GREEDY, template, min, max);
	}

	/**
	 * Same as {@link #Loop(Quantifier, Generator, int, int)} with the same
	 * min/max.
	 */
	public Loop(Quantifier quantifier, Generator<Element> generator, int count) {
		this(quantifier, generator, count, count);
	}

	/**
	 * Same as {@link #Loop(Generator, int, int)} with the same min/max.
	 */
	public Loop(Generator<Element> generator, int count) {
		this(generator, count, count);
	}

	/**
	 * Same as {@link #Loop(Quantifier, Layer, int, int)} with the same min/max.
	 */
	public Loop(Quantifier quantifier, Element template, int count) {
		this(quantifier, template, count, count);
	}

	/**
	 * Same as {@link #Loop(Layer, int, int)} with the same min/max.
	 */
	public Loop(Element template, int count) {
		this(template, count, count);
	}

	/**
	 * Same as {@link #Loop(Quantifier, Generator, int, int)} where the minimum
	 * is 0 and the maximum is {@link Integer#MAX_VALUE}.
	 */
	public Loop(Quantifier quantifier, Generator<Element> generator) {
		this(quantifier, generator, 0, Integer.MAX_VALUE);
	}

	/**
	 * Same as {@link #Loop(Generator, int, int)} where the minimum is 0 and the
	 * maximum is {@link Integer#MAX_VALUE}.
	 */
	public Loop(Generator<Element> generator) {
		this(generator, 0, Integer.MAX_VALUE);
	}

	/**
	 * Same as {@link #Loop(Quantifier, Layer, int, int)} where the minimum is 0
	 * and the maximum is {@link Integer#MAX_VALUE}.
	 */
	public Loop(Quantifier quantifier, Element template) {
		this(quantifier, template, 0, Integer.MAX_VALUE);
	}

	/**
	 * Same as {@link #Loop(Layer, int, int)} where the minimum is 0 and the
	 * maximum is {@link Integer#MAX_VALUE}.
	 */
	public Loop(Element template) {
		this(template, 0, Integer.MAX_VALUE);
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	private Element template;

	private Element getTemplate() {
		if (template == null) {
			// build it only if requested to avoid infinite loop in constructor
			template = generator.generates();
		} else {
			// reuse the existing one
		}
		return template;
	}

	/**
	 * This method create a {@link Generator} based on an existing instance,
	 * which acts like a template to generate new instances. The generated
	 * instances are clones of the template, so the template should have an
	 * implemented {@link #clone()} method.
	 * 
	 * @param template
	 *            the instance to use as a template
	 * @return a {@link Generator} generating clones from this template
	 */
	public static <Element extends Layer> Generator<Element> createGeneratorFromTemplate(
			final Element template) {
		if (template == null) {
			throw new NullPointerException("No template has been provided");
		} else {
			Method method;
			try {
				method = template.getClass().getMethod("clone");
				method.setAccessible(true);
			} catch (SecurityException e) {
				throw new IllegalArgumentException(
						"The provided template cannot be used: " + template, e);
			} catch (NoSuchMethodException e) {
				throw new IllegalArgumentException(
						"The provided template has no clone method: "
								+ template, e);
			}

			if (!method.getDeclaringClass().equals(template.getClass())) {
				throw new IllegalArgumentException(
						"The provided template does not implement its own clone() method: "
								+ template);
			} else {
				Object clone;
				try {
					clone = method.invoke(template);
				} catch (InvocationTargetException e) {
					throw new IllegalArgumentException("The clone() method of "
							+ template + " is not reliable", e.getCause());
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("This case should not happen", e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("This case should not happen", e);
				}
				if (clone == null) {
					throw new IllegalArgumentException(
							"The clone() method of the provided template returns null: "
									+ template);
				} else if (clone == template) {
					throw new IllegalArgumentException(
							"The clone() method of the provided template returns the template itself: "
									+ template);
				} else {
					try {
						final Method cloneMethod = method;
						return new Generator<Element>() {

							@SuppressWarnings("unchecked")
							@Override
							public Element generates() {
								try {
									return (Element) cloneMethod
											.invoke(template);
								} catch (Exception e) {
									throw new RuntimeException(e);
								}
							}
						};
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	@Override
	protected String buildRegex() {
		return "(?:" + getTemplate().getRegex() + ")" + buildRegexCardinality();
	}

	@Override
	protected void setInternalContent(String content) {
		occurrences = new LinkedList<Element>();
		Matcher matcher = Pattern.compile(getRegex()).matcher(content);
		if (matcher.matches()) {
			Iterator<Element> iterator = occurrences.iterator();
			while (iterator.hasNext()) {
				Element element = (Element) iterator.next();
				element.removeContentListener(deepListener);
				iterator.remove();
			}

			String regex = getTemplate().getRegex();
			matcher = Pattern.compile(
					"(" + regex + ")(?:(?=" + regex + ")|(?=$))").matcher(
					content);
			while (matcher.find()) {
				String match = matcher.group(1);
				Element occurrence = generator.generates();
				occurrence.setContent(match);
				occurrence.addContentListener(deepListener);
				occurrences.add(occurrence);
			}
		} else {
			matcher = Pattern.compile(getTemplate().getRegex())
					.matcher(content);
			int start = 0;
			int count = 0;
			while (matcher.find() && matcher.start() == start) {
				count++;
				if (count > max) {
					throw new ParsingException(this, null, content, start,
							content.length());
				} else {
					start += matcher.group(0).length();
				}
			}
			if (count < min) {
				throw new ParsingException(this, getTemplate(), content,
						content.length(), content.length());
			} else {
				try {
					getTemplate().setContent(content.substring(start));
				} catch (ParsingException e) {
					throw new ParsingException(this, getTemplate(), content,
							start + e.getStart(), content.length(), e);
				}
			}
		}
	}

	@Override
	public InputStream getInputStream() {
		/*
		 * We first store the InputStreams to ensure that all of them are
		 * available. This allows to throw NoContentException immediately if the
		 * sequence is not complete.
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
	 * @return <code>true</code> if this {@link Loop} has no {@link Element}
	 *         (empty content), <code>false</code> otherwise
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
	 * This method adds a new {@link Element} to this {@link Loop}. The
	 * {@link Element} should have the same regex than the usual {@link Element}
	 * s of this {@link Loop}.
	 * 
	 * @param index
	 *            the index of the new {@link Element}
	 * @param element
	 *            the new {@link Element}
	 */
	@SuppressWarnings("unchecked")
	public void add(int index, Element element) {
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
	 */
	public Element add(int index, String content) {
		if (size() >= max) {
			throw new BoundException("This loop cannot have more than " + max
					+ " elements.");
		} else {
			Element newElement = generator.generates();
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
	 * {@link #addAllContents(int, Collection)}, which has a different name to
	 * avoid type erasure issues.
	 * 
	 * @param index
	 *            the index form which to start the addition
	 * @param elements
	 *            the new {@link Element}s
	 */
	public void addAll(int index, Collection<Element> elements) {
		if (size() + elements.size() > max) {
			throw new BoundException("This loop cannot have more than " + max
					+ " elements.");
		} else {
			Collection<Element> validElements = new LinkedList<Element>();
			for (Element element : elements) {
				if (!element.getRegex().equals(getTemplate().getRegex())) {
					throw new IllegalArgumentException(
							"The regex of the element ("
									+ element.getRegex()
									+ ") is not the same than the elements of this loop: "
									+ getTemplate().getRegex());
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
	 */
	public Collection<Element> addAllContents(int index,
			Collection<String> contents) {
		if (size() >= max) {
			throw new BoundException("This loop cannot have more than " + max
					+ " elements.");
		} else {
			Collection<Element> elements = new LinkedList<Element>();
			for (String content : contents) {
				Element element = generator.generates();
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
	 *            the index of an {@link Element} to remove from this
	 *            {@link Loop}
	 * @return {@link Element} removed
	 */
	public Element remove(int index) {
		if (size() <= min) {
			throw new BoundException("This loop cannot have less than " + min
					+ " elements.");
		} else {
			Element removed = occurrences.remove(index);
			removed.removeContentListener(deepListener);
			fireContentUpdate();
			return removed;
		}
	}

	public void clear() {
		if (size() <= min) {
			throw new BoundException("This loop cannot have less than " + min
					+ " elements.");
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
						throw new BoundException(
								"This loop cannot have less than " + min
										+ " elements.");
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
	public String toString() {
		return getClass().getSimpleName() + "["
				+ getTemplate().getClass().getSimpleName()
				+ buildRegexCardinality() + "]";
	}

	public Quantifier getQuantifier() {
		return quantifier;
	}

	private String buildRegexCardinality() {
		return buildRegexCardinality(0);
	}

	private String buildRegexCardinality(int consumed) {
		int min = Math.max(this.min - consumed, 0);
		int max = this.max == Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(
				this.max - consumed, 0);
		String decorator;
		if (min == 0 && max == Integer.MAX_VALUE) {
			decorator = "*";
		} else if (min == 0 && max == 1) {
			decorator = "?";
		} else if (min == 1 && max == Integer.MAX_VALUE) {
			decorator = "+";
		} else if (min == max) {
			decorator = "{" + min + "}";
		} else if (max == Integer.MAX_VALUE) {
			decorator = "{" + min + ",}";
		} else {
			decorator = "{" + min + "," + max + "}";
		}
		return decorator + quantifier.getDecorator();
	}

	/**
	 * A {@link Generator} allows to create new instances of a specific
	 * {@link Layer}.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 * @param <Element>
	 */
	public static interface Generator<Element extends Layer> {
		/**
		 * 
		 * @return a new {@link Element}
		 */
		public Element generates();
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

	@Override
	public Object clone() {
		Loop<Element> loop = new Loop<Element>(quantifier, generator, min, max);
		String content = getContent();
		if (content != null) {
			loop.setContent(content);
		} else {
			// keep it not filled
		}
		return loop;
	}
}
