package fr.vergne.parsing.layer.util;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.standard.AbstractLayer;
import fr.vergne.parsing.layer.standard.Loop;
import fr.vergne.parsing.layer.standard.Loop.BoundException;
import fr.vergne.parsing.layer.standard.Loop.Generator;
import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.layer.standard.Suite;

/**
 * A {@link SeparatedLoop} provides, rather than a sequence of adjacent
 * {@link Element}s (e.g. AAAAA) like a classical {@link Loop}, a sequence of
 * {@link Element}s is separated by {@link Separator}s (e.g. AXAXAXAXA).
 * Consequently, the number of {@link Separator}s is always equal to the number
 * of {@link Element}s - 1.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <Element>
 * @param <Separator>
 */
// FIXME ensure that it is the same separator everywhere
public class SeparatedLoop<Element extends Layer, Separator extends Layer>
		extends AbstractLayer implements Iterable<Element> {

	private final int min;
	private final int max;
	private final Layer overall;
	private final LayerProxy<Element> head;
	private final Loop<Suite> loop;
	private final Separator separatorDefault;
	private final Generator<Element> elementGenerator;
	private final Generator<Separator> separatorGenerator;
	private final ContentListener deepListener = new ContentListener() {

		@Override
		public void contentSet(String newContent) {
			fireContentUpdate();
		}
	};

	public SeparatedLoop(Quantifier quantifier,
			final Generator<Element> elementGenerator,
			final Generator<Separator> separatorGenerator, int min, int max) {
		this.min = min;
		this.max = max;
		this.head = new LayerProxy<Element>(elementGenerator.generates());
		int loopMin = Math.max(0, min - 1);
		int loopMax = max == Integer.MAX_VALUE ? max : Math.max(0, max - 1);
		this.loop = new Loop<Suite>(quantifier, new Generator<Suite>() {

			@Override
			public Suite generates() {
				return new Suite(separatorGenerator.generates(),
						elementGenerator.generates());
			}
		}, loopMin, loopMax);

		Suite suite = new Suite(head, loop);
		this.overall = min == 0 ? new Option<Suite>(suite) : suite;

		this.elementGenerator = elementGenerator;
		this.separatorGenerator = separatorGenerator;
		this.separatorDefault = separatorGenerator.generates();

		this.overall.addContentListener(deepListener);
	}

	public SeparatedLoop(final Generator<Element> elementGenerator,
			final Generator<Separator> separatorGenerator, int min, int max) {
		this(Quantifier.GREEDY, elementGenerator, separatorGenerator, min, max);
	}

	public SeparatedLoop(Quantifier quantifier,
			Generator<Element> elementGenerator,
			Generator<Separator> separatorGenerator, int count) {
		this(quantifier, elementGenerator, separatorGenerator, count, count);
	}

	public SeparatedLoop(Generator<Element> elementGenerator,
			Generator<Separator> separatorGenerator, int count) {
		this(elementGenerator, separatorGenerator, count, count);
	}

	public SeparatedLoop(Quantifier quantifier,
			Generator<Element> elementGenerator,
			Generator<Separator> separatorGenerator) {
		this(quantifier, elementGenerator, separatorGenerator, 0,
				Integer.MAX_VALUE);
	}

	public SeparatedLoop(Generator<Element> elementGenerator,
			Generator<Separator> separatorGenerator) {
		this(elementGenerator, separatorGenerator, 0, Integer.MAX_VALUE);
	}

	public SeparatedLoop(Quantifier quantifier, Element elementTemplate,
			Separator separatorTemplate, int min, int max) {
		this(quantifier, Loop.createGeneratorFromTemplate(elementTemplate),
				Loop.createGeneratorFromTemplate(separatorTemplate), min, max);
	}

	public SeparatedLoop(Element elementTemplate, Separator separatorTemplate,
			int min, int max) {
		this(Quantifier.GREEDY, elementTemplate, separatorTemplate, min, max);
	}

	public SeparatedLoop(Quantifier quantifier, Element elementTemplate,
			Separator separatorTemplate, int count) {
		this(quantifier, elementTemplate, separatorTemplate, count, count);
	}

	public SeparatedLoop(Element elementTemplate, Separator separatorTemplate,
			int count) {
		this(elementTemplate, separatorTemplate, count, count);
	}

	public SeparatedLoop(Quantifier quantifier, Element elementTemplate,
			Separator separatorTemplate) {
		this(quantifier, elementTemplate, separatorTemplate, 0,
				Integer.MAX_VALUE);
	}

	public SeparatedLoop(Element elementTemplate, Separator separatorTemplate) {
		this(elementTemplate, separatorTemplate, 0, Integer.MAX_VALUE);
	}

	@Override
	protected String buildRegex() {
		return overall.getRegex();
	}

	@Override
	protected void setInternalContent(String content) {
		overall.removeContentListener(deepListener);
		try {
			overall.setContent(content);
			if (size() >= 2) {
				separatorDefault.setContent(getSeparator(0).getContent());
			} else if (separatorDefault.getContent() != null) {
				// keep old value
			} else {
				System.err.println("Warning: no default separator set");
			}
		} catch (ParsingException e) {
			throw new ParsingException(this, overall, content, e.getStart(),
					content.length(), e);
		} finally {
			overall.addContentListener(deepListener);
		}
	}

	@Override
	public InputStream getInputStream() {
		return overall.getInputStream();
	}

	public Quantifier getQuantifier() {
		return loop.getQuantifier();
	}

	/**
	 * 
	 * @return the number of {@link Element}s of this {@link SeparatedLoop}
	 */
	@SuppressWarnings("unchecked")
	public int size() {
		if (overall instanceof Option && !((Option<Suite>) overall).isPresent()) {
			return 0;
		} else {
			return 1 + loop.size();
		}
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 
	 * @param index
	 *            the index of a parsed {@link Element}
	 * @return the {@link Element} requested
	 * @throws IndexOutOfBoundsException
	 *             if the index relate to an inexistent {@link Element}
	 */
	@SuppressWarnings("unchecked")
	public Element get(int index) throws IndexOutOfBoundsException {
		if ((overall instanceof Suite || ((Option<Suite>) overall).isPresent())
				&& loop.size() > index - 1 && index >= 0) {
			return index == 0 ? head.getLayer() : (Element) loop.get(index - 1)
					.get(1);
		} else {
			throw new IndexOutOfBoundsException("The index (" + index
					+ ") should be between 0 and " + size());
		}
	}

	@SuppressWarnings("unchecked")
	public Separator getSeparator(int index) {
		if ((overall instanceof Suite || ((Option<Suite>) overall).isPresent())
				&& loop.size() > index - 2 && index >= 0) {
			return loop.get(index).get(0);
		} else {
			throw new IndexOutOfBoundsException("The index (" + index
					+ ") should be between 0 and " + size());
		}
	}

	public void setDefaultSeparator(String separator) {
		separatorDefault.setContent(separator);
	}

	public String getDefaultSeparator() {
		return separatorDefault.getContent();
	}

	public void add(int index, Element element) {
		addAll(index, Arrays.asList(element));
	}

	public Element add(int index, String content) {
		Element element = elementGenerator.generates();
		element.setContent(content);
		add(index, element);
		return element;
	}

	public Collection<Element> addAllContents(int index,
			Collection<String> contents) {
		Collection<Element> elements = new LinkedList<Element>();
		for (String content : contents) {
			Element element = elementGenerator.generates();
			element.setContent(content);
			elements.add(element);
		}
		addAll(index, elements);
		return elements;
	}

	public void addAll(int index, Collection<Element> elements) {
		overall.removeContentListener(deepListener);
		try {
			if (elements.isEmpty()) {
				// nothing to add
			} else {
				LinkedList<Element> remaining = new LinkedList<Element>(
						elements);
				if (size() == 0) {
					head.setLayer(remaining.removeFirst());
					loop.setContent("");
					((Option<?>) overall).setPresent(true);
					index++;
				} else if (index == 0) {
					Suite added = new Suite(createFilledSeparator(),
							head.getLayer());
					head.setLayer(remaining.removeFirst());
					loop.add(0, added);
					index++;
				} else {
					// add all the the loop
				}

				index--;
				Collection<Suite> added = new LinkedList<Suite>();
				for (Element element : remaining) {
					added.add(new Suite(createFilledSeparator(), element));
				}
				loop.addAll(index, added);
			}
		} finally {
			overall.addContentListener(deepListener);
		}
		fireContentUpdate();
	}

	private Separator createFilledSeparator() {
		if (separatorDefault.getContent() == null) {
			throw new RuntimeException("No default separator setup.");
		} else {
			Separator separator = separatorGenerator.generates();
			separator.setContent(separatorDefault.getContent());
			return separator;
		}
	}

	public Element remove(int index) {
		if (index == 0) {
			overall.removeContentListener(deepListener);
			Element removed = head.getLayer();
			try {
				if (size() == 1) {
					overall.setContent("");
				} else {
					head.setLayer(loop.remove(0).<Element> get(1));
				}
			} finally {
				overall.addContentListener(deepListener);
			}
			fireContentUpdate();
			return removed;
		} else {
			return loop.remove(index - 1).get(1);
		}
	}

	public void clear() {
		if (overall instanceof Option) {
			((Option<?>) overall).setPresent(false);
		} else {
			throw new BoundException("This loop cannot have less than "
					+ (loop.getMin() + 1) + " elements.");
		}
	}

	@Override
	public Iterator<Element> iterator() {
		return new Iterator<Element>() {

			private int currentIndex = -1;

			@Override
			public boolean hasNext() {
				return currentIndex < size() - 1;
			}

			@Override
			public Element next() {
				return get(++currentIndex);
			}

			@Override
			public void remove() {
				SeparatedLoop.this.remove(currentIndex);
				currentIndex--;
			}
		};
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	@Override
	public Object clone() {
		SeparatedLoop<Element, Separator> loop = new SeparatedLoop<Element, Separator>(
				getQuantifier(), elementGenerator, separatorGenerator, min, max);
		String content = getContent();
		if (content != null) {
			loop.setContent(content);
		} else {
			// keep it not filled
		}
		return loop;
	}
}
