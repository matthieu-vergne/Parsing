package fr.vergne.parsing.layer.standard.impl;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.definition.impl.StandardDefinitionFactory;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.AbstractLayer;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.layer.standard.impl.Loop.BoundException;
import fr.vergne.parsing.util.Named;

/**
 * A {@link SeparatedLoop} provides, rather than a sequence of adjacent
 * {@link Item}s (e.g. AAAAA) like a classical {@link Loop}, a sequence of
 * {@link Item}s is separated by {@link Separator}s (e.g. AXAXAXAXA).
 * Consequently, the number of {@link Separator}s is always equal to the number
 * of {@link Item}s - 1.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 * @param <Item>
 * @param <Separator>
 */
// TODO Transform standards into interfaces
public class SeparatedLoop<Item extends Layer, Separator extends Layer> extends AbstractLayer
		implements Iterable<Item>, Named {

	private final int min;
	private final int max;
	private Layer overall;
	private final Definition<? extends Layer> overallDefinition;
	private final Separator separatorDefault;
	private final Definition<Item> itemDefinition;
	private final Definition<Separator> separatorDefinition;
	private final Definition<Sequence> couple;
	private final Definition<Loop<Sequence>> loop;
	private final Definition<Item> head;
	private final Definition<Sequence> sequence;
	private final ContentListener deepListener = new ContentListener() {

		@Override
		public void contentSet(String newContent) {
			fireContentUpdate();
		}
	};

	public SeparatedLoop(Definition<Item> itemDefinition, Definition<Separator> separatorDefinition, int min, int max,
			Quantifier quantifier) {
		this.min = min;
		this.max = max;

		this.itemDefinition = itemDefinition;
		this.separatorDefinition = separatorDefinition;
		this.separatorDefault = separatorDefinition.create();

		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		this.head = factory.defineAs(itemDefinition);
		this.couple = factory.defineSequence(separatorDefinition, itemDefinition);
		int loopMin = Math.max(0, min - 1);
		int loopMax = max == Integer.MAX_VALUE ? max : Math.max(0, max - 1);
		this.loop = factory.defineLoop(couple, loopMin, loopMax, quantifier);
		this.sequence = factory.defineSequence(head, loop);

		// TODO Use Choice in both cases instead of Option only in one
		if (min == 0) {
			this.overallDefinition = factory.defineOptional(sequence, quantifier);
		} else {
			this.overallDefinition = sequence;
		}
		this.overall = overallDefinition.create();
		this.overall.addContentListener(deepListener);
	}

	public SeparatedLoop(Definition<Item> itemDefinition, Definition<Separator> separatorDefinition, int min, int max) {
		this(itemDefinition, separatorDefinition, min, max, Quantifier.GREEDY);
	}

	public SeparatedLoop(Definition<Item> itemDefinition, Definition<Separator> separatorDefinition) {
		this(itemDefinition, separatorDefinition, 0, Integer.MAX_VALUE);
	}

	// TODO Deprecate & remove
	public String getRegex() {
		return overallDefinition.getRegex();
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
			throw new ParsingException(this, overallDefinition, content, e.getStart(), content.length(), e);
		} finally {
			overall.addContentListener(deepListener);
		}
	}

	@Override
	public InputStream getInputStream() {
		return overall.getInputStream();
	}

	public Quantifier getQuantifier() {
		return getOverallSequence().get(loop).getQuantifier();
	}

	/**
	 * 
	 * @return the number of {@link Item}s of this {@link SeparatedLoop}
	 */
	@SuppressWarnings("unchecked")
	public int size() {
		if (overall instanceof Option && !((Option<Sequence>) overall).isPresent()) {
			return 0;
		} else {
			return 1 + getOverallSequence().get(loop).size();
		}
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * 
	 * @param index
	 *            the index of a parsed {@link Item}
	 * @return the {@link Item} requested
	 * @throws IndexOutOfBoundsException
	 *             if the index relate to an inexistent {@link Item}
	 */
	@SuppressWarnings("unchecked")
	public Item get(int index) throws IndexOutOfBoundsException {
		if ((overall instanceof Sequence || ((Option<Sequence>) overall).isPresent())
				&& getOverallSequence().get(loop).size() > index - 1 && index >= 0) {
			return index == 0 ? getHead() : (Item) getOverallSequence().get(loop).get(index - 1).get(1);
		} else {
			throw new IndexOutOfBoundsException("The index (" + index + ") should be between 0 and " + size());
		}
	}

	@SuppressWarnings("unchecked")
	public Separator getSeparator(int index) {
		if ((overall instanceof Sequence || ((Option<Sequence>) overall).isPresent())
				&& getOverallSequence().get(loop).size() > index - 2 && index >= 0) {
			return getOverallSequence().get(loop).get(index).get(0);
		} else {
			throw new IndexOutOfBoundsException("The index (" + index + ") should be between 0 and " + size());
		}
	}

	@SuppressWarnings("unchecked")
	public <CLayer extends Layer> CLayer get(Definition<CLayer> definition, int index) {
		if (definition == itemDefinition) {
			return (CLayer) get(index);
		} else if (definition == separatorDefinition) {
			return (CLayer) getSeparator(index);
		} else {
			throw new IllegalArgumentException("Unknown definition: " + definition);
		}
	}

	public void setDefaultSeparator(String separator) {
		separatorDefault.setContent(separator);
	}

	public String getDefaultSeparator() {
		return separatorDefault.getContent();
	}

	public void add(int index, Item item) {
		addAll(index, Arrays.asList(item));
	}

	public Item add(int index, String content) {
		Item item = itemDefinition.create();
		item.setContent(content);
		add(index, item);
		return item;
	}

	public Collection<Item> addAllContents(int index, Collection<String> contents) {
		Collection<Item> items = new LinkedList<Item>();
		for (String content : contents) {
			Item item = itemDefinition.create();
			item.setContent(content);
			items.add(item);
		}
		addAll(index, items);
		return items;
	}

	public void addAll(int index, Collection<Item> items) {
		if (items.isEmpty()) {
			// nothing to add
		} else {
			for (Item item : items) {
				if (item == null) {
					throw new IllegalArgumentException("Null item provided: " + items);
				} else if (!itemDefinition.isCompatibleWith(item)) {
					throw new IllegalArgumentException("Invalid item provided: " + item);
				} else {
					continue;
				}
			}

			overall.removeContentListener(deepListener);
			try {
				LinkedList<Item> remaining = new LinkedList<Item>(items);
				if (size() == 0 && index == 0) {
					setHead(remaining.removeFirst());
					getOverallSequence().get(loop).setContent("");
					index++;
				} else if (index == 0) {
					Sequence added = couple.create();
					added.setContent(separatorDefault.getContent() + getHead().getContent());
					added.set(separatorDefinition, createFilledSeparator());
					added.set(itemDefinition, getHead());
					setHead(remaining.removeFirst());
					getOverallSequence().get(loop).add(0, added);
					index++;
				} else {
					// add all to the loop
				}

				index--;
				for (Item item : remaining) {
					Sequence sequence = couple.create();
					sequence.setContent(separatorDefault.getContent() + item.getContent());
					sequence.set(separatorDefinition, createFilledSeparator());
					sequence.set(itemDefinition, item);
					getOverallSequence().get(loop).add(index, sequence);
					index++;
				}
			} finally {
				overall.addContentListener(deepListener);
			}
			fireContentUpdate();
		}
	}

	private Separator createFilledSeparator() {
		if (separatorDefault.getContent() == null) {
			throw new RuntimeException("No default separator setup.");
		} else {
			Separator separator = separatorDefinition.create();
			separator.setContent(separatorDefault.getContent());
			return separator;
		}
	}

	public Item remove(int index) {
		if (index == 0) {
			overall.removeContentListener(deepListener);
			Item removed = itemDefinition.create();
			removed.setContent(getHead().getContent());
			try {
				if (size() == 1) {
					overall.setContent("");
				} else {
					setHead(getOverallSequence().get(loop).remove(0).get(itemDefinition));
				}
			} finally {
				overall.addContentListener(deepListener);
			}
			fireContentUpdate();
			return removed;
		} else {
			return getOverallSequence().get(loop).remove(index - 1).get(itemDefinition);
		}
	}

	public void clear() {
		if (overall instanceof Option) {
			overall.setContent("");
		} else {
			throw new BoundException(
					"This loop cannot have less than " + (getOverallSequence().get(loop).getMin() + 1) + " items.");
		}
	}

	@Override
	public Iterator<Item> iterator() {
		return new Iterator<Item>() {

			private int currentIndex = -1;

			@Override
			public boolean hasNext() {
				return currentIndex < size() - 1;
			}

			@Override
			public Item next() {
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
	public String getName() {
		return "SEPLOOP";
	}

	@Override
	public String toString() {
		return getName() + "[" + Named.name(itemDefinition.create()) + "][" + Named.name(separatorDefinition.create())
				+ "]";
	}

	private Item getHead() {
		return getOverallSequence().get(head);
	}

	private void setHead(Item item) {
		if (item == null) {
			throw new IllegalArgumentException("No item provided: " + item);
		} else if (!itemDefinition.isCompatibleWith(item)) {
			throw new IllegalArgumentException("Invalid item: " + item);
		} else {
			if (size() == 0) {
				Sequence seq = sequence.create();
				seq.setContent(item.getContent());
				seq.set(head, item);
				setOverallSequence(seq);
			} else {
				getOverallSequence().set(head, item);
			}
		}
	}

	private Sequence getOverallSequence() {
		Sequence seq;
		if (min == 0) {
			@SuppressWarnings("unchecked")
			Option<Sequence> option = (Option<Sequence>) overall;
			seq = option.getOption();
		} else {
			seq = (Sequence) overall;
		}
		return seq;
	}

	private void setOverallSequence(Sequence sequence) {
		if (min == 0) {
			@SuppressWarnings("unchecked")
			Option<Sequence> option = (Option<Sequence>) overall;
			option.setOption(sequence);
		} else {
			overall = sequence;
		}
	}

	public Definition<Item> getItemDefinition() {
		return itemDefinition;
	}

	public Definition<Separator> getSeparatorDefinition() {
		return separatorDefinition;
	}
}
