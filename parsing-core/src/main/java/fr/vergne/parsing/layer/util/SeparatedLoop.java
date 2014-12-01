package fr.vergne.parsing.layer.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Iterator;

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
	private final Element head;
	private final Loop<Suite> loop;

	public SeparatedLoop(Generator<Element> itemGenerator,
			Generator<Separator> separatorGenerator) {
		this(itemGenerator, separatorGenerator, 0, Integer.MAX_VALUE);
	}

	public SeparatedLoop(final Generator<Element> itemGenerator,
			final Generator<Separator> separatorGenerator, int min, int max) {
		head = itemGenerator.generates();
		int loopMin = Math.max(0, min - 1);
		int loopMax = max == Integer.MAX_VALUE ? max : Math.max(0, max - 1);
		loop = new Loop<Suite>(new Generator<Suite>() {

			@Override
			public Suite generates() {
				return new Suite(separatorGenerator.generates(),
						itemGenerator.generates());
			}
		}, loopMin, loopMax);

		Suite suite = new Suite(head, loop);
		overall = min == 0 ? new Option<Suite>(suite) : suite;
	}

	@Override
	protected String buildRegex() {
		return overall.getRegex();
	}

	@Override
	protected void setInternalContent(String content) {
		try {
			overall.setContent(content);
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
		return new InputStream() {
			private final StringReader reader = new StringReader(getContent());

			@Override
			public int read() throws IOException {
				return reader.read();
			}
		};
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
			return index == 0 ? head : (Element) loop.get(index - 1).get(1);
		} else {
			throw new IndexOutOfBoundsException("The index (" + index
					+ ") should be between 0 and " + size());
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
				throw new RuntimeException(
						"You cannot remove an element from this iterator.");
			}
		};
	}
}
