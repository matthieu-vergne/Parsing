package fr.vergne.parsing.layer.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private final CLayer template;
	private final int min;
	private final int max;
	private Integer currentIndex = null;
	private final ContentListener updater = new ContentListener() {

		@Override
		public void contentSet(String oldValue, String newValue) {
			if (currentIndex == null) {
				// no synchronization
			} else {
				contents.set(currentIndex, newValue);
			}
		}
	};

	public Loop(CLayer template, int min, int max) {
		if (min < 0 || max < 0) {
			throw new IllegalArgumentException(
					"The limits should be positive or null.");
		} else if (min > max) {
			throw new IllegalArgumentException(
					"The minimum should be inferior or equal to the maximum.");
		} else {
			this.template = template;
			this.min = min;
			this.max = max;
			template.addContentListener(updater);
		}
	}

	protected void finalize() throws Throwable {
		template.removeContentListener(updater);
	};

	public Loop(CLayer template, int count) {
		this(template, count, count);
	}

	public Loop(CLayer template) {
		this(template, 0, Integer.MAX_VALUE);
	}

	@Override
	public String getRegex() {
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
		return "^(?:" + template.getRegex() + ")" + decorator + "$";
	}

	@Override
	protected void setInternalContent(String content) {
		Matcher matcher = Pattern.compile(getRegex()).matcher(content);
		if (matcher.matches()) {
			contents.clear();
			matcher = Pattern.compile(template.getRegex()).matcher(content);
			while (matcher.find()) {
				contents.add(matcher.group(0));
			}
		} else {
			matcher = Pattern.compile(template.getRegex()).matcher(content);
			int start = 0;
			int count = 0;
			boolean found;
			while ((found = matcher.find()) && matcher.start() == start) {
				count++;
				if (count > max) {
					throw new ParsingException(null, content, start,
							content.length());
				} else {
					start += matcher.group(0).length();
				}
			}
			if (count < min) {
				throw new ParsingException(template.getRegex(), content,
						content.length(), content.length());
			} else {
				throw new ParsingException(template.getRegex(), content, start,
						found ? matcher.start() : content.length());
			}
		}
	}

	@Override
	public String getContent() {
		String content = "";
		for (String chunk : contents) {
			content += chunk;
		}
		return content;
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

	@Override
	public Iterator<CLayer> iterator() {
		currentIndex = null;
		return new Iterator<CLayer>() {

			private int nextIndex = 0;

			@Override
			public boolean hasNext() {
				return nextIndex < size();
			}

			@Override
			public CLayer next() {
				if (hasNext()) {
					CLayer template = get(nextIndex);
					nextIndex++;
					return template;
				} else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public void remove() {
				if (currentIndex == null) {
					throw new NoSuchElementException();
				} else {
					Loop.this.remove(currentIndex);
					currentIndex = null;
					nextIndex--;
				}
			}
		};
	}

	/**
	 * This method provides the index-th occurrence found in the content.<br/>
	 * <br/>
	 * <b>ATTENTION</b> the {@link Layer} returned is always the same (the one
	 * provided in the constructor) but set with the corresponding content. Do
	 * not use it for instance to get two occurrences and compare them, it will
	 * be always equal.
	 * 
	 * @param index
	 *            the index of the occurrence
	 * @return the occurrence
	 */
	public CLayer get(int index) {
		currentIndex = index;
		template.setContent(contents.get(index));
		return template;
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
		currentIndex = currentIndex == null || currentIndex == index ? null
				: currentIndex > index ? currentIndex - 1 : currentIndex;
	}

	/**
	 * This methods duplicates an existing occurrence, increasing the size of
	 * the {@link Loop}, and returns the duplicate. In the special case where
	 * the index provided is the size of the {@link Loop}, the interpretation is
	 * to duplicate the last element (index=size-1) but to return the original
	 * element (index=size). This method cannot be used if the {@link Loop} is
	 * empty.
	 * 
	 * @param index
	 *            the index of the occurrence to duplicate
	 * @return the new occurrence
	 */
	public CLayer duplicate(int index) {
		if (index == size()) {
			contents.add(index, contents.get(index - 1));
		} else {
			contents.add(index, contents.get(index));
		}
		return get(index);
	}

	/**
	 * This method adds a new occurrence to this loop. This occurrence should be
	 * compatible with the template of the {@link Loop}.
	 * 
	 * @param index
	 *            the index of the new occurrence
	 * @param content
	 *            the content this new occurrence
	 */
	public void add(int index, String content) {
		duplicate(index).setContent(content);
	}
}
