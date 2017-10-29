package fr.vergne.parsing.layer.standard.impl;

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
import fr.vergne.parsing.layer.standard.Loop;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.util.Named;

// TODO Replace Basic by more explicit term
// TODO Doc
public class BasicLoop<Element extends Layer> extends AbstractLayer implements Loop<Element> {

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
	 * Instantiate an optimized {@link BasicLoop} (through the {@link Quantifier})
	 * which uses a {@link Definition} to instantiate the occurrences it will find.
	 * Notice that requesting two times the same occurrence, for instance calling
	 * {@link #get(int)} with the same index, should provide the same instance as
	 * long as the corresponding content has not been modified. If the
	 * {@link Definition} returns always the same instance(s), it will not work
	 * properly.
	 * 
	 * @param definition
	 *            the occurrences {@link Definition}
	 * @param min
	 *            the minimum size of this {@link BasicLoop}, at least 0
	 * @param max
	 *            the maximum size of this {@link BasicLoop}, at most
	 *            {@link Integer#MAX_VALUE}
	 * @param quantifier
	 *            type of {@link Quantifier} to use to optimize the regex
	 */
	public BasicLoop(Definition<Element> definition, int min, int max, Quantifier quantifier) {
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

	public BasicLoop(Definition<Element> definition, int min, int max) {
		this(definition, min, max, Quantifier.GREEDY);
	}

	public BasicLoop(Definition<Element> definition) {
		this(definition, 0, Integer.MAX_VALUE);
	}

	@Override
	public Definition<Element> getItemDefinition() {
		return itemDefinition;
	}

	@Override
	public int getMin() {
		return min;
	}

	@Override
	public int getMax() {
		return max;
	}

	@Override
	public Quantifier getQuantifier() {
		return quantifier;
	}

	@Override
	protected void setInternalContent(String content) {
		occurrences = new LinkedList<Element>();
		Matcher matcher = Pattern
				.compile(new StandardDefinitionFactory().defineLoop(itemDefinition, min, max, quantifier).getRegex())
				.matcher(content);
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

	@Override
	public int size() {
		return occurrences.size();
	}

	@Override
	public boolean isEmpty() {
		return occurrences.isEmpty();
	}

	@Override
	public Element get(int index) {
		return occurrences.get(index);
	}

	@Override
	public void add(int index, Element element) {
		addAll(index, Arrays.asList(element));
	}

	@Override
	public Element add(int index, String content) {
		if (size() >= max) {
			throw new BoundException("This loop cannot have more than " + max + " elements.");
		} else {
			Element newElement = itemDefinition.create();
			newElement.setContent(content);
			add(index, newElement);
			return newElement;
		}
	}

	@Override
	public void addAll(int index, Collection<Element> elements) {
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

	@Override
	public Collection<Element> addAllContents(int index, Collection<String> contents) {
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

	@Override
	public Element remove(int index) {
		if (size() <= min) {
			throw new BoundException("This loop cannot have less than " + min + " elements.");
		} else {
			Element removed = occurrences.remove(index);
			removed.removeContentListener(deepListener);
			fireContentUpdate();
			return removed;
		}
	}

	public void clear() {
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
	public String toString() {
		String cardinality = StandardDefinitionFactory.buildRegexCardinality(quantifier, min, max);
		return getName() + "[" + Named.name(itemDefinition.create()) + cardinality + "]";
	}

}
