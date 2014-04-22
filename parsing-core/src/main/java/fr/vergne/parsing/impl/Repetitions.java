package fr.vergne.parsing.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.vergne.parsing.Structure;
import fr.vergne.parsing.exception.IncompatibilityException;

public class Repetitions<CStructure extends Structure> extends AbstractStructure
		implements Iterable<CStructure> {

	private final List<String> contents = new LinkedList<String>();
	private final CStructure structure;
	private final int min;
	private final int max;
	private Integer currentIndex = null;

	public Repetitions(CStructure structure, int min, int max) {
		if (min < 0 || max < 0) {
			throw new IllegalArgumentException(
					"The limits should be positive or null.");
		} else if (min > max) {
			throw new IllegalArgumentException(
					"The minimum should be inferior or equal to the maximum.");
		} else {
			this.structure = structure;
			this.min = min;
			this.max = max;
			structure.addContentListener(new ContentListener() {

				@Override
				public void contentSet(String oldValue, String newValue) {
					if (currentIndex == null) {
						// no synchronization
					} else {
						contents.set(currentIndex, newValue);
					}
				}
			});
		}
	}

	public Repetitions(CStructure structure, int count) {
		this(structure, count, count);
	}

	public Repetitions(CStructure structure) {
		this(structure, 0, Integer.MAX_VALUE);
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
		return "^(?:" + structure.getRegex() + ")" + decorator + "$";
	}

	@Override
	protected void setInternalContent(String content) {
		Matcher matcher = Pattern.compile(getRegex()).matcher(content);
		if (matcher.matches()) {
			contents.clear();
			matcher = Pattern.compile(structure.getRegex()).matcher(content);
			while (matcher.find()) {
				contents.add(matcher.group(0));
			}
		} else {
			matcher = Pattern.compile(structure.getRegex()).matcher(content);
			int start = 0;
			int count = 0;
			boolean found;
			while ((found = matcher.find()) && matcher.start() == start) {
				count++;
				if (count > max) {
					throw new IncompatibilityException(null, content, start,
							content.length());
				} else {
					start += matcher.group(0).length();
				}
			}
			if (count < min) {
				throw new IncompatibilityException(structure.getRegex(),
						content, content.length(), content.length());
			} else {
				throw new IncompatibilityException(structure.getRegex(),
						content, start, found ? matcher.start()
								: content.length());
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

	public int size() {
		return contents.size();
	}

	@Override
	public Iterator<CStructure> iterator() {
		currentIndex = null;
		return new Iterator<CStructure>() {

			private int nextIndex = 0;

			@Override
			public boolean hasNext() {
				return nextIndex < size();
			}

			@Override
			public CStructure next() {
				if (hasNext()) {
					CStructure structure = get(nextIndex);
					nextIndex++;
					return structure;
				} else {
					throw new NoSuchElementException();
				}
			}

			@Override
			public void remove() {
				if (currentIndex == null) {
					throw new NoSuchElementException();
				} else {
					Repetitions.this.remove(currentIndex);
					currentIndex = null;
					nextIndex--;
				}
			}
		};
	}

	public CStructure get(int index) {
		currentIndex = index;
		structure.setContent(contents.get(index));
		return structure;
	}

	public void remove(int index) {
		contents.remove(index);
		currentIndex = currentIndex == null || currentIndex == index ? null
				: currentIndex > index ? currentIndex - 1 : currentIndex;
	}

	public CStructure duplicate(int index) {
		if (index == size()) {
			contents.add(index, contents.get(index - 1));
		} else {
			contents.add(index, contents.get(index));
		}
		return get(index);
	}

	public void add(int index, String content) {
		duplicate(index).setContent(content);
	}
}
