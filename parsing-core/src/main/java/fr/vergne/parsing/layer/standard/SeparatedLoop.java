package fr.vergne.parsing.layer.standard;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Pattern;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.exception.ParsingException;
import fr.vergne.parsing.layer.impl.AbstractLayer;
import fr.vergne.parsing.layer.standard.Loop.BoundException;
import fr.vergne.parsing.util.Named;
import fr.vergne.parsing.util.RegexRecursivityLimiter;

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
// TODO Doc
// TODO Revise exceptions (inspire from Loop)
// TODO reduce responsibilities (min/max, quantifier, default, etc.)
public class SeparatedLoop<Item extends Layer, Separator extends Layer> extends AbstractLayer
		implements Iterable<Item>, Named {

	private final int min;
	private final int max;
	private Layer overall;
	private final Definition<? extends Layer> overallDefinition;
	private final Definition<Item> itemDefinition;
	private final Definition<Separator> separatorDefinition;
	private String separatorDefault = null;
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

		this.head = Definition.like(itemDefinition);
		this.couple = Sequence.define(separatorDefinition, itemDefinition);
		int loopMin = Math.max(0, min - 1);
		int loopMax = max == Integer.MAX_VALUE ? max : Math.max(0, max - 1);
		this.loop = Loop.define(couple, loopMin, loopMax, quantifier);
		this.sequence = Sequence.define(head, loop);

		// TODO Use Choice in both cases instead of Option only in one
		if (min == 0) {
			this.overallDefinition = Option.define(sequence, quantifier);
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

	// TODO remove
	public String getRegex() {
		return overallDefinition.getRegex();
	}

	@Override
	protected void setInternalContent(String content) {
		Executor undo = deactivateNotifications(false);
		try {
			overall.setContent(content);
		} catch (ParsingException e) {
			throw new ParsingException(this, overallDefinition, content, e.getStart(), content.length(), e);
		} finally {
			undo.execute();
		}
	}

	@Override
	public InputStream getInputStream() {
		return overall.getInputStream();
	}

	public Definition<Item> getItemDefinition() {
		return itemDefinition;
	}

	public Definition<Separator> getSeparatorDefinition() {
		return separatorDefinition;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
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
		if (Pattern.matches(separatorDefinition.getRegex(), separator)) {
			separatorDefault = separator;
		} else {
			throw new IllegalArgumentException("Invalid separator: " + separator);
		}
	}

	public String getDefaultSeparator() {
		return separatorDefault;
	}

	private String getSeparatorIfExists() {
		String sep = getDefaultSeparator();
		if (sep != null) {
			return sep;
		} else {
			try {
				Loop<Sequence> loop2 = getOverallSequence().get(loop);
				Sequence sequence2 = loop2.get(0);
				Separator separator = sequence2.get(separatorDefinition);
				return separator.getContent();
			} catch (Exception cause) {
				System.err.println("Warning: no default separator set");
				return null;
			}
		}
	}

	public Item add(String content) {
		return add(size(), content);
	}

	public Item add(int index, String content) {
		return addAll(index, Arrays.asList(content)).iterator().next();
	}

	public Collection<Item> addAll(int index, Collection<String> contents) {
		Collection<Item> addeds = new LinkedList<>();
		if (contents.isEmpty()) {
			// nothing to add
		} else {
			for (String content : contents) {
				if (content == null) {
					throw new IllegalArgumentException("Null content provided: " + contents);
				} else {
					continue;
				}
			}

			Executor undo = deactivateNotifications(true);
			try {
				LinkedList<String> remaining = new LinkedList<>(contents);
				if (index == 0) {
					if (size() > 0) {
						String movedContent = getExistingSeparatorContent() + getHead().getContent();
						getOverallSequence().get(loop).add(0, movedContent);
					} else {
						// Nothing to move
					}

					setHead(remaining.removeFirst());
					addeds.add(getHead());
					index++;
				} else {
					// add all to the loop
				}

				index--;
				for (String content : remaining) {
					String addedContent = getExistingSeparatorContent() + content;
					Sequence added = getOverallSequence().get(loop).add(index, addedContent);
					addeds.add(added.get(itemDefinition));
					index++;
				}
			} finally {
				undo.execute();
			}
		}
		return addeds;
	}

	private String getExistingSeparatorContent() {
		String content = getSeparatorIfExists();
		if (content == null) {
			throw new RuntimeException("No default separator setup.");
		} else {
			return content;
		}
	}

	public void sort(Comparator<Item> comparator) {
		if (size() <= 1) {
			// Nothing to sort
		} else {
			Executor undo = deactivateNotifications(true);

			// Duplicate head to have all content in loop
			// TODO This strategy should fail with a complete bounded loop, should do better
			add(get(0).getContent());

			// Sort loop
			Comparator<Sequence> seqComparator = (s1, s2) -> {
				Item i1 = s1.get(itemDefinition);
				Item i2 = s2.get(itemDefinition);
				return comparator.compare(i1, i2);
			};
			getOverallSequence().get(loop).sort(seqComparator);

			// Remove duplicate head
			remove(0);

			undo.execute();
		}
	}

	private interface Executor {
		void execute();
	}

	private int notificationDeactivationLevel = 0;

	/**
	 * A method might do several things on the current instance, which may lead to
	 * fire several notifications. To avoid this, this method should be called to
	 * cancel notifications. The returned {@link Executor} allows to reactivate the
	 * notifications and, optionally, fire a notification. Calls to this method are
	 * stackable: if a parent method deactivates and call a child method which
	 * deactivates too, the reactivation of the child will be ineffective. Only the
	 * first deactivator will actually reactivate notifications.
	 * 
	 * @param fireContentUpdate
	 *            tells whether the {@link #fireContentUpdate()} should be called
	 *            when notifications are reactivated
	 * @return the reactivation {@link Executor}
	 */
	private Executor deactivateNotifications(boolean fireContentUpdate) {
		if (notificationDeactivationLevel == 0) {
			overall.removeContentListener(deepListener);
		} else {
			// Already removed
		}
		notificationDeactivationLevel++;

		return () -> {
			notificationDeactivationLevel--;
			if (notificationDeactivationLevel == 0) {
				overall.addContentListener(deepListener);
				if (fireContentUpdate) {
					fireContentUpdate();
				} else {
					// Don't fire
				}
			} else {
				// Don't restore yet
			}
		};
	}

	public Item remove(int index) {
		if (index == 0) {
			Executor undo = deactivateNotifications(true);
			Item removed = itemDefinition.create();
			removed.setContent(getHead().getContent());
			try {
				if (size() == 1) {
					overall.setContent("");
				} else {
					setHead(getOverallSequence().get(loop).remove(0).get(itemDefinition).getContent());
				}
			} finally {
				undo.execute();
			}
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

	@SuppressWarnings("unchecked")
	private void setHead(String content) {
		if (size() == 0) {
			if (min == 0) {
				((Option<Sequence>) overall).setContent(content);
			} else {
				Sequence seq = sequence.create();
				seq.setContent(content);
				overall = seq;
			}
		} else {
			getOverallSequence().set(head, content);
		}
	}

	@SuppressWarnings("unchecked")
	private Sequence getOverallSequence() {
		if (min == 0) {
			return ((Option<Sequence>) overall).getOption();
		} else {
			return (Sequence) overall;
		}
	}

	public static <Item extends Layer, Separator extends Layer> Definition<SeparatedLoop<Item, Separator>> define(
			Definition<Item> item, Definition<Separator> separator) {
		return define(item, separator, 0, Integer.MAX_VALUE, Quantifier.GREEDY);
	}

	public static <Item extends Layer, Separator extends Layer> Definition<SeparatedLoop<Item, Separator>> define(
			Definition<Item> item, Definition<Separator> separator, Quantifier quantifier) {
		return define(item, separator, 0, Integer.MAX_VALUE, quantifier);
	}

	public static <Item extends Layer, Separator extends Layer> Definition<SeparatedLoop<Item, Separator>> define(
			Definition<Item> item, Definition<Separator> separator, String defaultSeparator, Quantifier quantifier) {
		return define(item, separator, defaultSeparator, 0, Integer.MAX_VALUE, quantifier);
	}

	public static <Item extends Layer, Separator extends Layer> Definition<SeparatedLoop<Item, Separator>> define(
			Definition<Item> item, Definition<Separator> separator, int min, int max) {
		return define(item, separator, min, max, Quantifier.GREEDY);
	}

	public static <Item extends Layer, Separator extends Layer> Definition<SeparatedLoop<Item, Separator>> define(
			Definition<Item> item, Definition<Separator> separator, int min, int max, Quantifier quantifier) {
		return new Definition<SeparatedLoop<Item, Separator>>() {

			private final RegexRecursivityLimiter regexComputer = new RegexRecursivityLimiter(
					() -> create().getRegex());

			@Override
			public String getRegex() {
				return regexComputer.generate();
			}

			@Override
			public SeparatedLoop<Item, Separator> create() {
				return new SeparatedLoop<>(item, separator, min, max, quantifier);
			}
		};
	}

	public static <Item extends Layer, Separator extends Layer> Definition<SeparatedLoop<Item, Separator>> define(
			Definition<Item> item, Definition<Separator> separator, String defaultSeparator, int min, int max,
			Quantifier quantifier) {
		return new Definition<SeparatedLoop<Item, Separator>>() {

			@Override
			public String getRegex() {
				return create().getRegex();
			}

			@Override
			public SeparatedLoop<Item, Separator> create() {
				SeparatedLoop<Item, Separator> loop = define(item, separator, min, max, quantifier).create();
				loop.setDefaultSeparator(defaultSeparator);
				return loop;
			}
		};
	}

	public static <Item extends Layer> Definition<SeparatedLoop<Item, Constant>> define(Definition<Item> item,
			String separator) {
		return define(item, Constant.define(separator));
	}

	public static <Item extends Layer> Definition<SeparatedLoop<Item, Constant>> define(Definition<Item> item,
			String separator, Quantifier quantifier) {
		return define(item, Constant.define(separator), quantifier);
	}

	public static <Item extends Layer> Definition<SeparatedLoop<Item, Constant>> define(Definition<Item> item,
			String separator, int min, int max, Quantifier quantifier) {
		return define(item, Constant.define(separator), min, max, quantifier);
	}
}
