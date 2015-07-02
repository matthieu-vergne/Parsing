package fr.vergne.parsing.layer.util;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.standard.AbstractLayer;
import fr.vergne.parsing.layer.standard.GreedyMode;
import fr.vergne.parsing.layer.standard.Loop;
import fr.vergne.parsing.layer.standard.Loop.Generator;
import fr.vergne.parsing.layer.standard.Option;
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
public class SeparatedLoop<Element extends Layer, Separator extends Layer>
		extends AbstractLayer implements Iterable<Element> {

	private final Layer overall;
	private final LayerProxy<Element> head;
	private final Loop<Suite> loop;
	private final Separator separatorDefault;
	private final Generator<Element> elementGenerator;
	private final Generator<Separator> separatorGenerator;

	public SeparatedLoop(final Generator<Element> elementGenerator,
			final Generator<Separator> separatorGenerator, int min, int max) {
		head = new LayerProxy<Element>(elementGenerator.generates());
		int loopMin = Math.max(0, min - 1);
		int loopMax = max == Integer.MAX_VALUE ? max : Math.max(0, max - 1);
		loop = new Loop<Suite>(new Generator<Suite>() {

			@Override
			public Suite generates() {
				return new Suite(separatorGenerator.generates(),
						elementGenerator.generates());
			}
		}, loopMin, loopMax);

		Suite suite = new Suite(head, loop);
		overall = min == 0 ? new Option<Suite>(suite) : suite;

		this.elementGenerator = elementGenerator;
		this.separatorGenerator = separatorGenerator;
		separatorDefault = separatorGenerator.generates();

		overall.addContentListener(new ContentListener() {

			@Override
			public void contentSet(String newContent) {
				fireContentUpdate(getContent());
			}
		});
	}

	public SeparatedLoop(Generator<Element> elementGenerator,
			Generator<Separator> separatorGenerator, int count) {
		this(elementGenerator, separatorGenerator, count, count);
	}

	public SeparatedLoop(Generator<Element> elementGenerator,
			Generator<Separator> separatorGenerator) {
		this(elementGenerator, separatorGenerator, 0, Integer.MAX_VALUE);
	}

	public SeparatedLoop(Element elementTemplate, Separator separatorTemplate,
			int min, int max) {
		this(Loop.createGeneratorFromTemplate(elementTemplate), Loop
				.createGeneratorFromTemplate(separatorTemplate), min, max);
	}

	public SeparatedLoop(Element elementTemplate, Separator separatorTemplate,
			int count) {
		this(elementTemplate, separatorTemplate, count, count);
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
		}
	}

	@Override
	public String getContent() {
		return overall.getContent();
	}

	@Override
	public InputStream getInputStream() {
		return IOUtils.toInputStream(getContent(), Charset.forName("UTF-8"));
	}

	public GreedyMode getMode() {
		return loop.getMode();
	}

	@SuppressWarnings("unchecked")
	public void setMode(GreedyMode mode) {
		if (overall instanceof Option) {
			((Option<Suite>) overall).setMode(mode);
		} else {
			// no mode for suites
		}
		loop.setMode(mode);
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
		if (size() == 0) {
			head.setLayer(element);
		} else {
			String sep = separatorDefault.getContent();
			if (sep == null) {
				throw new RuntimeException("No default separator setup.");
			} else {
				Separator separator = separatorGenerator.generates();
				separator.setContent(separatorDefault.getContent());
				if (index == 0) {
					Suite added = new Suite(separator, head.getLayer());
					head.setLayer(element);
					loop.add(0, added);
				} else {
					Suite added = new Suite(separator, element);
					loop.add(index - 1, added);
				}
			}
		}
	}

	public Element add(int index, String content) {
		Element element = elementGenerator.generates();
		element.setContent(content);
		add(index, element);
		return element;
	}

	public Element remove(int index) {
		if (index == 0) {
			Element removed = head.getLayer();
			if (size() == 1) {
				overall.setContent("");
			} else {
				head.setLayer(loop.remove(0).<Element> get(1));
			}
			return removed;
		} else {
			return loop.remove(index - 1).get(1);
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
}
