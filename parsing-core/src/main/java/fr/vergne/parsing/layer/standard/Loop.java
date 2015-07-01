package fr.vergne.parsing.layer.standard;

import java.io.IOException;
import java.io.InputStream;
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
	private final ContentListener occurrenceListener = new ContentListener() {

		@Override
		public void contentSet(String newContent) {
			fireContentUpdate(getContent());
		}
	};
	private final List<Element> occurrences = new LinkedList<Element>();
	private GreedyMode mode = GreedyMode.GREEDY;
	public static final Logger log = LoggerConfiguration.getSimpleLogger();

	/**
	 * Instantiate a {@link Loop} which uses a {@link Generator} to instantiate
	 * the occurrences it will find. Notice that requesting two times the same
	 * occurrence, for instance calling {@link #get(int)} with the same index,
	 * should provide the same instance as long as the corresponding content has
	 * not been modified. If the {@link Generator} returns always the same
	 * instance(s), it will not work properly.
	 * 
	 * @param generator
	 *            the occurrences {@link Generator}
	 * @param min
	 *            the minimum size of this {@link Loop}, at least 0
	 * @param max
	 *            the maximum size of this {@link Loop}, at most
	 *            {@link Integer#MAX_VALUE}
	 */
	public Loop(Generator<Element> generator, int min, int max) {
		if (min < 0) {
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
		}
	}

	private Element template;

	private Element getTemplate() {
		if (template == null) {
			template = generator.generates();
		} else {
			// reuse the existing one
		}
		return template;
	}

	/**
	 * Instantiate a {@link Loop} in the same way the
	 * {@link #Loop(Generator, int, int)} but by providing the {@link Layer} to
	 * use as template for the occurrences. If this template is clonable (the
	 * {@link #clone()} method is public), a {@link Generator} is automatically
	 * instantiated to use clones of this template for future occurrences.
	 * Otherwise, an exception is thrown.
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
	public Loop(final Element template, int min, int max) {
		this(createGeneratorFromTemplate(template), min, max);
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
		boolean isCloneable;
		try {
			Object clone = template.getClass().getMethod("clone")
					.invoke(template);
			isCloneable = clone != null && clone != template;
		} catch (Exception e) {
			isCloneable = false;
		}

		if (isCloneable) {
			return new Generator<Element>() {

				@SuppressWarnings("unchecked")
				@Override
				public Element generates() {
					try {
						return (Element) template.getClass().getMethod("clone")
								.invoke(template);
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			};
		} else {
			throw new IllegalArgumentException(
					"The provided template is not clonable");
		}
	}

	/**
	 * Same as {@link #Loop(Generator, int, int)} with the same min/max.
	 */
	public Loop(Generator<Element> generator, int count) {
		this(generator, count, count);
	}

	/**
	 * Same as {@link #Loop(Layer, int, int)} with the same min/max.
	 */
	public Loop(Element template, int count) {
		this(template, count, count);
	}

	/**
	 * Same as {@link #Loop(Generator, int, int)} where the minimum is 0 and the
	 * maximum is {@link Integer#MAX_VALUE}.
	 */
	public Loop(Generator<Element> generator) {
		this(generator, 0, Integer.MAX_VALUE);
	}

	/**
	 * Same as {@link #Loop(Layer, int, int)} where the minimum is 0 and the
	 * maximum is {@link Integer#MAX_VALUE}.
	 */
	public Loop(Element template) {
		this(template, 0, Integer.MAX_VALUE);
	}

	@Override
	protected String buildRegex() {
		return "(?:" + getTemplate().getRegex() + ")" + buildRegexCardinality();
	}

	@Override
	protected void setInternalContent(String content) {
		Matcher matcher = Pattern.compile(getRegex()).matcher(content);
		if (matcher.matches()) {
			occurrences.clear();
			matcher = Pattern.compile(getTemplate().getRegex())
					.matcher(content);
			while (matcher.find()) {
				Element occurrence = generator.generates();
				occurrence.setContent(matcher.group(0));
				occurrence.addContentListener(occurrenceListener);
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
	public String getContent() {
		String content = "";
		for (Element occurrence : occurrences) {
			content += occurrence.getContent();
		}
		return content;
	}

	@Override
	public InputStream getInputStream() {
		return new InputStream() {
			private InputStream reader = new InputStream() {

				@Override
				public int read() throws IOException {
					return -1;
				}
			};
			private int index = 0;

			@Override
			public int read() throws IOException {
				int character = reader.read();
				if (character == -1 && index < size()) {
					reader = occurrences.get(index).getInputStream();
					index++;
					character = reader.read();
				} else {
					// keep the current reader
				}
				return character;
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
	public void add(int index, Element element) {
		if (size() >= max) {
			throw new BoundException("This loop cannot have more than " + max
					+ " elements.");
		} else if (!element.getRegex().equals(getTemplate().getRegex())) {
			throw new IllegalArgumentException("The regex of the element ("
					+ element.getRegex()
					+ ") is not the same than the elements of this loop: "
					+ getTemplate().getRegex());
		} else if (element.getContent() == null) {
			throw new IllegalArgumentException(
					"You cannot add an element which has no content: set it before to add it to this loop.");
		} else {
			element.addContentListener(occurrenceListener);
			occurrences.add(index, element);
			fireContentUpdate(getContent());
		}
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
			removed.removeContentListener(occurrenceListener);
			fireContentUpdate(getContent());
			return removed;
		}
	}

	@Override
	public Iterator<Element> iterator() {
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
					throw new BoundException("This loop cannot have less than "
							+ min + " elements.");
				} else {
					occurenceIterator.remove();
					lastReturned.removeContentListener(occurrenceListener);
					fireContentUpdate(getContent());
				}
			}
		};
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ getTemplate().getClass().getSimpleName()
				+ buildRegexCardinality() + "]";
	}

	public GreedyMode getMode() {
		return mode;
	}

	public void setMode(GreedyMode mode) {
		this.mode = mode;
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
		return decorator + mode.getDecorator();
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
		Loop<Element> loop = new Loop<Element>(generator, min, max);
		loop.setMode(mode);
		String content = getContent();
		if (content != null) {
			loop.setContent(content);
		} else {
			// keep it not filled
		}
		return loop;
	}
}
