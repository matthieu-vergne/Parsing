package fr.vergne.parsing.layer.standard;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import fr.vergne.logging.LoggerConfiguration;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;

/**
 * A {@link Loop} is a {@link Layer} representing a variable number of
 * occurrences following a given template. It is well suited for repeated
 * sequences of similar elements, like a vector of numbers or an ordered list of
 * variable length. At the opposite of a {@link Suite}, which considers a static
 * sequence of (possibly) different elements, a {@link Loop} considers a
 * variable repetitions of a single template, possibly constrained in size.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <CLayer>
 */
public class Loop<CLayer extends Layer> extends AbstractLayer implements
		Iterable<CLayer> {

	private final List<String> contents = new LinkedList<String>();
	private final List<CLayer> occurrences = new LinkedList<CLayer>();
	private final Generator<CLayer> generator;
	private final int min;
	private final int max;
	private CLayer template = null;
	private Integer currentIndex = null;
	public static final Logger log = LoggerConfiguration.getSimpleLogger();
	private final ContentListener templateUpdater = new ContentListener() {

		@Override
		public void contentSet(String oldValue, String newValue) {
			if (currentIndex == null) {
				// no synchronization
			} else {
				contents.set(currentIndex, newValue);
			}
		}
	};

	/**
	 * Instantiate a {@link Loop} which uses a {@link Generator} to instantiate
	 * the occurrences it will find. Notice that requesting two times the same
	 * occurrence, for instance calling {@link #get(int)} with the same index,
	 * should provide the same instance as long as the
	 * {@link #setContent(String)} method has not been called. If the
	 * {@link Generator} returns always the same instance, it is the same than
	 * instantiating the {@link Loop} by using {@link #Loop(Layer, int, int)}
	 * with an uncloneable template.
	 * 
	 * @param generator
	 *            the occurrences {@link Generator}
	 * @param min
	 *            the minimum size of this {@link Loop}, at least 0
	 * @param max
	 *            the maximum size of this {@link Loop}, at most
	 *            {@link Integer#MAX_VALUE}
	 */
	public Loop(Generator<CLayer> generator, int min, int max) {
		if (min < 0 || max < 0) {
			throw new IllegalArgumentException(
					"The limits should be positive or null.");
		} else if (min > max) {
			throw new IllegalArgumentException(
					"The minimum should be inferior or equal to the maximum.");
		} else {
			this.generator = generator;
			this.min = min;
			this.max = max;
		}
	}

	/**
	 * Instantiate a {@link Loop} in the same way the
	 * {@link #Loop(Generator, int, int)} but by providing the {@link Layer} to
	 * use as template for the occurrences. If this template is cloneable (the
	 * {@link #clone()} method is public), a {@link Generator} is automatically
	 * instantiated to use clones of this template for future occurrences.
	 * Otherwise, the template itself will be used for all the occurrences.<br/>
	 * <br/>
	 * <b>ATTENTION</b> In the case of an uncloneable template, pay attention to
	 * how you use the methods like {@link #get(int)} or {@link #iterator()}.
	 * The returned element is always this same template instance but with a
	 * different content (corresponding to the occurrence it is supposed to
	 * represent). You have also to pay attention on how you use the template:
	 * it is synchronized to the {@link Loop} (any content modification can
	 * affect the {@link Loop}'s content) and it can have its content modified
	 * at any moment by the {@link Loop}. It is highly recommended to dedicate
	 * such a template to its {@link Loop} and not reuse it, as well as not
	 * reuse the instances returned by this {@link Loop}. To avoid unwanted
	 * behaviors, a warning is displayed if you provide an uncloneable template,
	 * but you can disable this warning with the parameters.
	 * 
	 * @param template
	 *            the {@link CLayer} to use as a template
	 * @param min
	 *            the minimum size of this {@link Loop}, at least 0
	 * @param max
	 *            the maximum size of this {@link Loop}, at most
	 *            {@link Integer#MAX_VALUE}
	 * @param warnUncloneable
	 *            <code>true</code> if the use of an uncloneable template should
	 *            result in a warning, <code>false</code> to remain silent
	 */
	public Loop(final CLayer template, int min, int max, boolean warnUncloneable) {
		this(isCloneable(template) ? new Generator<CLayer>() {

			@SuppressWarnings("unchecked")
			@Override
			public CLayer generates() {
				try {
					return (CLayer) template.getClass().getMethod("clone")
							.invoke(template);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		} : new Generator<CLayer>() {

			@Override
			public CLayer generates() {
				return template;
			}
		}, min, max);
		if (isCloneable(template)) {
			// keep synchronization for clones
		} else {
			if (warnUncloneable) {
				log.warning("Loop " + this
						+ " based on an uncloneable template.");
			} else {
				// do not notice
			}
			template.addContentListener(templateUpdater);
		}
	}

	/**
	 * Same as {@link #Loop(Layer, int, int, boolean)} with the uncloneable
	 * template warning enabled.
	 */
	public Loop(CLayer template, int min, int max) {
		this(template, min, max, true);
	}

	private static <CLayer extends Layer> boolean isCloneable(CLayer template) {
		try {
			return template.getClass().getMethod("clone").invoke(template) != null;
		} catch (Exception e) {
			return false;
		}
	}

	protected void finalize() throws Throwable {
		// no cloneable check because assumed to works anyway
		getTemplate().removeContentListener(templateUpdater);
	};

	/**
	 * Same as {@link #Loop(Generator, int, int)} with the same min/max.
	 */
	public Loop(Generator<CLayer> generator, int count) {
		this(generator, count, count);
	}

	/**
	 * Same as {@link #Loop(CLayer, int, int, boolean)} with the same min/max.
	 */
	public Loop(CLayer template, int count, boolean warnUncloneable) {
		this(template, count, count, warnUncloneable);
	}

	/**
	 * Same as {@link #Loop(Layer, int, boolean)} with the uncloneable template
	 * warning enabled.
	 */
	public Loop(CLayer template, int count) {
		this(template, count, true);
	}

	/**
	 * Same as {@link #Loop(Generator, int, int)} where the minimum is 0 and the
	 * maximum is {@link Integer#MAX_VALUE}.
	 */
	public Loop(Generator<CLayer> generator) {
		this(generator, 0, Integer.MAX_VALUE);
	}

	/**
	 * Same as {@link #Loop(CLayer, int, int, boolean)} where the minimum is 0
	 * and the maximum is {@link Integer#MAX_VALUE}.
	 */
	public Loop(CLayer template, boolean warnUncloneable) {
		this(template, 0, Integer.MAX_VALUE, warnUncloneable);
	}

	/**
	 * Same as {@link #Loop(Layer, boolean)} with the uncloneable template
	 * warning enabled.
	 */
	public Loop(CLayer template) {
		this(template, true);
	}

	private CLayer getTemplate() {
		if (template == null) {
			template = generator.generates();
		} else {
			// already known
		}
		return template;
	}

	@Override
	protected String buildRegex() {
		return "(?:" + getTemplate().getRegex() + ")" + buildRegexCardinality();
	}

	@Override
	protected void setInternalContent(String content) {
		Matcher matcher = Pattern.compile(getRegex()).matcher(content);
		if (matcher.matches()) {
			contents.clear();
			occurrences.clear();
			matcher = Pattern.compile(getTemplate().getRegex())
					.matcher(content);
			while (matcher.find()) {
				contents.add(matcher.group(0));
				occurrences.add(null);
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
		for (int i = 0; i < contents.size(); i++) {
			content += getContent(i);
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
				if (character == -1 && index < contents.size()) {
					if (occurrences.get(index) != null) {
						reader = occurrences.get(index).getInputStream();
					} else {
						reader = IOUtils.toInputStream(contents.get(index),
								Charset.forName("UTF-8"));
					}
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
	 * @return the number of occurrences found
	 */
	public int size() {
		return contents.size();
	}

	/**
	 * 
	 * @return <code>true</code> is no occurrences have been found,
	 *         <code>false</code> otherwise
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * <b>ATTENTION</b> if the {@link Loop} is based on a uncloneable template
	 * (provided to the constructor), the {@link Layer} returned for each
	 * {@link Iterator#next()} is always this same template but set with the
	 * corresponding content. Do not use it for instance to get two occurrences
	 * and compare them, it will be always equal.
	 */
	@Override
	public Iterator<CLayer> iterator() {
		currentIndex = null;
		return new Iterator<CLayer>() {

			private int nextIndex = 0;
			private boolean isRemoved = false;

			@Override
			public boolean hasNext() {
				return nextIndex < size();
			}

			/**
			 * <b>ATTENTION</b> if the {@link Loop} is based on a uncloneable
			 * template (provided to the constructor), the {@link Layer}
			 * returned is always this same template but set with the
			 * corresponding content. Do not use it for instance to get two
			 * occurrences and compare them, it will be always equal.
			 */
			@Override
			public CLayer next() {
				if (hasNext()) {
					CLayer occurrence = get(nextIndex);
					nextIndex++;
					isRemoved = false;
					return occurrence;
				} else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public void remove() {
				if (isRemoved) {
					throw new NoSuchElementException();
				} else {
					nextIndex--;
					Loop.this.remove(nextIndex);
					isRemoved = true;
				}
			}
		};
	}

	/**
	 * This method provides the index-th occurrence found in the content.<br/>
	 * <br/>
	 * <b>ATTENTION</b> if the {@link Loop} is based on a uncloneable template
	 * (provided to the constructor), the {@link Layer} returned is always this
	 * same template but set with the corresponding content. Do not use it for
	 * instance to get two occurrences and compare them, it will be always
	 * equal.
	 * 
	 * @param index
	 *            the index of the occurrence
	 * @return the occurrence
	 */
	public CLayer get(final int index) {
		if (index >= size() || index < 0) {
			throw new IndexOutOfBoundsException(index
					+ " is not between 0 and " + size());
		} else if (occurrences.get(index) != null) {
			return occurrences.get(index);
		} else {
			CLayer occurrence = generator.generates();
			if (occurrence != getTemplate()) {
				String content = contents.get(index);
				try {
					occurrence.setContent(content);
				} catch (ParsingException e) {
					int start = 0;
					for (int i = 0; i < index; i++) {
						start += get(i).getContent().length();
					}
					throw new ParsingException(this, occurrence, getContent(),
							start + e.getStart(), start + content.length(), e);
				}
				occurrences.set(index, occurrence);
				contents.set(index, null);
				return occurrence;
			} else {
				currentIndex = index;
				getTemplate().setContent(contents.get(index));
				return getTemplate();
			}
		}
	}

	/**
	 * This method provides the content of the index-th occurrence found in the
	 * total content. If you want to retrieve the content of each occurrence,
	 * you should prefer this method to {@link #get(int)} followed by
	 * {@link Layer#getContent()}, in order to minimize the cost of the
	 * instantiation.
	 * 
	 * @param index
	 *            the index of the occurrence
	 * @return the content of the occurrence
	 * @throws IndexOutOfBoundsException
	 *             if the index does not correspond to a current occurrence
	 */
	public String getContent(final int index) {
		if (index >= size() || index < 0) {
			throw new IndexOutOfBoundsException(index
					+ " is not between 0 and " + size());
		} else if (occurrences.get(index) != null) {
			return occurrences.get(index).getContent();
		} else {
			return contents.get(index);
		}
	}

	/**
	 * This method removes the occurrence at the given index. The next
	 * occurrences are shifted accordingly.
	 * 
	 * @param index
	 *            the index of the occurrence
	 */
	public void remove(int index) {
		contents.remove(index);
		occurrences.remove(index);
		currentIndex = currentIndex == null || currentIndex == index ? null
				: currentIndex > index ? currentIndex - 1 : currentIndex;
	}

	/**
	 * This methods duplicates an existing occurrence, increasing the size of
	 * the {@link Loop}, and returns the duplicate. In the special case where
	 * the index provided is the size of the {@link Loop}, the interpretation is
	 * to duplicate the last element (index=size-1) but to return the original
	 * element (index=size). This method cannot be used if the {@link Loop} is
	 * empty.<br/>
	 * <br/>
	 * <b>ATTENTION</b> if the {@link Loop} is based on a uncloneable template
	 * (provided to the constructor), the {@link Layer} returned is always this
	 * same template but set with the corresponding content. Do not use it for
	 * instance to get two occurrences and compare them, it will be always
	 * equal.
	 * 
	 * @param index
	 *            the index of the occurrence to duplicate
	 * @return the new occurrence
	 */
	public CLayer duplicate(int index) {
		if (size() > 0) {
			if (index == size()) {
				contents.add(index, get(index - 1).getContent());
			} else {
				contents.add(index, get(index).getContent());
			}
			occurrences.add(index, null);
			return get(index);
		} else {
			throw new IllegalStateException("The loop " + this
					+ " is empty, no duplicate an be done.");
		}
	}

	/**
	 * This method adds a new occurrence to this loop. The content should be
	 * compatible with the regex of this {@link Loop}.
	 * 
	 * @param index
	 *            the index of the new occurrence
	 * @param content
	 *            the content of this new occurrence
	 */
	public void add(int index, String content) {
		CLayer duplicate = duplicate(index);
		try {
			duplicate.setContent(content);
		} catch (ParsingException e) {
			remove(index);
			throw e;
		}
	}

	/**
	 * This method adds a new occurrence at the end of this loop. The content
	 * should be compatible with the regex of this {@link Loop}.
	 * 
	 * @param content
	 *            the content this new occurrence
	 */
	public void add(String content) {
		add(size(), content);
	}

	/**
	 * 
	 * @param from
	 *            the index of the occurrence to move
	 * @param to
	 *            the index to move the occurrence to
	 */
	public void move(int from, int to) {
		contents.add(to, contents.remove(from));
		occurrences.add(to, occurrences.remove(from));
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ getTemplate().getClass().getSimpleName()
				+ buildRegexCardinality() + "]";
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

	private GreedyMode mode = GreedyMode.GREEDY;

	public GreedyMode getMode() {
		return mode;
	}

	public void setMode(GreedyMode mode) {
		this.mode = mode;
	}

	/**
	 * A {@link Generator} allows to create new instances of a specific
	 * {@link Layer}.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 * @param <CLayer>
	 */
	public static interface Generator<CLayer extends Layer> {
		/**
		 * 
		 * @return a new {@link CLayer}
		 */
		public CLayer generates();
	}
}
