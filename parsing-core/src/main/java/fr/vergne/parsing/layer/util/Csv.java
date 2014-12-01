package fr.vergne.parsing.layer.util;

import java.util.LinkedList;
import java.util.List;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.GreedyMode;
import fr.vergne.parsing.layer.standard.Loop.Generator;
import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.Suite;

/**
 * A {@link Csv} aims at parsing a Comma Separated Value file. While the name
 * indicate that values should be separated by commas, it is usual to use other
 * separators like tabulation. A {@link Csv} allows such flexibility by having a
 * constructor able to specify the separator used.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
// FIXME Ensure that all rows and header have the same number of columns
public class Csv extends Suite {

	/**
	 * Equivalent to {@link #Csv(char)} where the separator used is a comma.
	 */
	public Csv() {
		this(',');
	}

	/**
	 * Equivalent to {@link #Csv(char, TranformerAssigner)} where the
	 * {@link TranformerAssigner} used is a default one, meaning it keeps the
	 * String as-is.
	 */
	public Csv(char separator) {
		this(separator, null);
	}

	/**
	 * Equivalent to {@link #Csv(char, TranformerAssigner)} where the separator
	 * used is a comma.
	 */
	public Csv(TranformerAssigner assigner) {
		this(',', assigner);
	}

	/**
	 * 
	 * @param separator
	 *            the separator to use between the {@link Value}s of each
	 *            {@link Row}
	 * @param assigner
	 *            the {@link TranformerAssigner} to use to identify which
	 *            {@link Transformer} to assign to each {@link Value}
	 */
	public Csv(final char separator, final TranformerAssigner assigner) {
		super(new Header(separator), new Newline(),
				new SeparatedLoop<Record, Newline>(new Generator<Record>() {
					@Override
					public Record generates() {
						return new Record(separator, assigner);
					}
				}, new Generator<Newline>() {
					@Override
					public Newline generates() {
						return new Newline();
					}
				}), new Option<Newline>(new Newline()));
		this.<SeparatedLoop<Record, Newline>> get(2).setMode(
				GreedyMode.POSSESSIVE);
	}

	/**
	 * A {@link Transformer} is a simple translator to transform the
	 * {@link String} content of a {@link Value} into a relevant object. It is
	 * usually a simple parsing, but it can be even more complex.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 * @param <T>
	 */
	public static interface Transformer<T> {
		public T transform(String value);
	}

	/**
	 * A {@link Value} is a specific field in the {@link Csv}. Each
	 * {@link Value} is separated to the next one by a separator (e.g. comma,
	 * tabulation).
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 */
	public static class Value extends Formula {
		public Value(char separator) {
			super("[^" + separator + "\\n\\r]++");
		}
	}

	/**
	 * A {@link TranformerAssigner} allows to specify which {@link Transformer}
	 * to assign to which {@link Value} of a given {@link Row}. Notice that no
	 * exhaustive constraint is applied: if the {@link Row}s contain less
	 * {@link Value}s than the {@link TranformerAssigner} is able to assign
	 * (e.g. a {@link Row} contains 3 {@link Value}s and the
	 * {@link TranformerAssigner} is able to manage indexes until 10), no
	 * {@link Exception} will be thrown.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 */
	public static interface TranformerAssigner {
		/**
		 * 
		 * @param valueIndex
		 *            the index of the {@link Value} in the {@link Row}
		 * @return the {@link Transformer} to assign to this {@link Value}
		 * @throws IndexOutOfBoundsException
		 *             if no {@link Transformer} can be provided for the given
		 *             index
		 */
		public Transformer<?> assign(int valueIndex)
				throws IndexOutOfBoundsException;
	}

	/**
	 * A {@link Row} is a consecutive set of {@link Value}s which represent a
	 * complete line inside the {@link Csv}. Notice that the newline character
	 * which separates the {@link Row} from the next/previous one is not part of
	 * it.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 */
	public static class Row extends SeparatedLoop<Value, Atom> {
		public Row(final char separator) {
			super(new Generator<Value>() {
				@Override
				public Value generates() {
					return new Value(separator);
				}
			}, new Generator<Atom>() {
				@Override
				public Atom generates() {
					return new Atom("" + separator);
				}
			}, 1, Integer.MAX_VALUE);
			setMode(GreedyMode.POSSESSIVE);
		}
	}

	/**
	 * A {@link Header} is a particular {@link Row} inside a {@link Csv}. This
	 * is the first {@link Row} and it provides the names of each {@link Value}
	 * within any other {@link Row}.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 */
	public static class Header extends Row {
		public Header(char separator) {
			super(separator);
		}
	}

	/**
	 * An {@link Record} is a particular {@link Row} inside a {@link Csv}. It
	 * represents a specific "instance" of the {@link Header}, meaning a
	 * complete entry with all the values set.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 */
	public static class Record extends Row {
		private final TranformerAssigner assigner;

		public Record(char separator, final TranformerAssigner assigner) {
			super(separator);
			this.assigner = assigner;
		}

		public String getStringValue(int index) {
			return get(index).getContent();
		}

		@SuppressWarnings("unchecked")
		public <T> T getObjectValue(int index) {
			if (assigner == null) {
				throw new NullPointerException("No assigner provided");
			} else {
				Transformer<?> transformer = assigner.assign(index);
				if (transformer == null) {
					throw new NullPointerException(
							"No transformer available for value " + index);
				} else {
					return (T) transformer.transform(getStringValue(index));
				}
			}
		}
	}

	/**
	 * 
	 * @return the number of values contained in each {@link Row}
	 */
	public int getColumnsCount() {
		return getHeaderRow().size();
	}

	/**
	 * 
	 * @return the {@link Row} corresponding to the {@link Header}
	 */
	public Header getHeaderRow() {
		return this.<Header> get(0);
	}

	/**
	 * 
	 * @return the number of {@link Record}s contained in this {@link Csv}
	 */
	public int getRecordsCount() {
		return getRecordsLayer().size();
	}

	/**
	 * 
	 * @return all the {@link Record}s of this {@link Csv}
	 */
	public Iterable<Record> getRecords() {
		return getRecordsLayer();
	}

	private SeparatedLoop<Record, Newline> getRecordsLayer() {
		return this.<SeparatedLoop<Record, Newline>> get(2);
	}

	/**
	 * 
	 * @return the names of the columns
	 */
	public List<String> getHeaders() {
		List<String> headers = new LinkedList<String>();
		for (Value value : getHeaderRow()) {
			headers.add(value.getContent());
		}
		return headers;
	}

	/**
	 * 
	 * @param index
	 *            the index of the {@link Record}
	 * @return the corresponding {@link Record}
	 */
	public Record getRecord(int index) {
		return getRecordsLayer().get(index);
	}
}
