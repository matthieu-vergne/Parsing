package fr.vergne.parsing.layer.util;

import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.standard.AbstractLayer;
import fr.vergne.parsing.layer.standard.Choice;
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
		extends AbstractLayer {

	private final Choice overall;
	private final Option<Element> option;
	private final Element head;
	private final Loop<Suite> loop;

	public SeparatedLoop(final Generator<Element> itemGenerator,
			final Generator<Separator> separatorGenerator) {
		head = itemGenerator.generates();
		loop = new Loop<Suite>(new Generator<Suite>() {

			@Override
			public Suite generates() {
				return new Suite(separatorGenerator.generates(),
						itemGenerator.generates());
			}
		});
		option = new Option<Element>(head);
		overall = new Choice(option, new Suite(head, loop));
		overall.setReferenceAlternative(overall.getAlternative(1));
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

	public GreedyMode getMode() {
		return loop.getMode();
	}

	public void setMode(GreedyMode mode) {
		option.setMode(mode);
		loop.setMode(mode);
	}

	/**
	 * 
	 * @return the number of {@link Element}s of this {@link SeparatedLoop}
	 */
	public int size() {
		if (overall.getCurrent() instanceof Option) {
			return option.isPresent() ? 1 : 0;
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
		if (overall.getCurrent() instanceof Option && option.isPresent()
				&& index == 0) {
			return option.getOption();
		} else if (overall.getCurrent() instanceof Suite
				&& loop.size() > index - 1 && index >= 0) {
			return index == 0 ? head : (Element) loop.get(index - 1).get(1);
		} else {
			throw new IndexOutOfBoundsException("The index (" + index
					+ ") should be between 0 and " + size());
		}
	}
}
