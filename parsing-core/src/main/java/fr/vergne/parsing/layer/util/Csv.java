package fr.vergne.parsing.layer.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.vergne.parsing.layer.standard.Atom;
import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.layer.standard.Loop.Generator;
import fr.vergne.parsing.layer.standard.Option;
import fr.vergne.parsing.layer.standard.Quantifier;
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
// TODO Ensure that all rows and header have the same number of columns
public class Csv extends Suite {

	/**
	 * Equivalent to {@link #Csv(char)} where the separator used is a comma.
	 */
	public Csv() {
		this(',');
	}

	/**
	 * 
	 * @param separator
	 *            the separator to use between the values of each
	 *            {@link Row}
	 */
	public Csv(final char separator) {
		this(separator, new HashMap<Integer, Collection<String>>());
	}

	private Csv(final char separator,
			final Map<Integer, Collection<String>> valueContainers) {
		super(new Header(separator), new Newline(),
				new SeparatedLoop<Record, Newline>(Quantifier.POSSESSIVE,
						new Generator<Record>() {
							@Override
							public Record generates() {
								return new Record(separator, valueContainers);
							}
						}, new Generator<Newline>() {
							@Override
							public Newline generates() {
								return new Newline();
							}
						}), new Option<Newline>(new Newline()));
		this.valueContainers = valueContainers;
	}

	/**
	 * A {@link Row} is a consecutive set of values which represent a
	 * complete line inside the {@link Csv}. Notice that the newline character
	 * which separates the {@link Row} from the next/previous one is not part of
	 * it.
	 * 
	 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
	 * 
	 */
	public static class Row extends SeparatedLoop<Formula, Atom> {
		public Row(final char separator) {
			super(Quantifier.POSSESSIVE, new Formula("[^" + separator
					+ "\\n\\r]++"), new Atom("" + separator), 1,
					Integer.MAX_VALUE);
		}
	}

	/**
	 * A {@link Header} is a particular {@link Row} inside a {@link Csv}. This
	 * is the first {@link Row} and it provides the names of each
	 * value within any other {@link Row}.
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

		public Record(char separator,
				final Map<Integer, Collection<String>> valueContainers) {
			super(separator);
			addContentListener(new ContentListener() {

				@Override
				public void contentSet(String newContent) {
					for (int index = 0; index < Record.this.size(); index++) {
						Collection<String> container = valueContainers
								.get(index);
						if (container == null) {
							// ignore it
						} else {
							container.add(Record.this.get(index).getContent());
						}
					}
				}
			});
		}

		public String getStringValue(int index) {
			return get(index).getContent();
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
		for (Formula value : getHeaderRow()) {
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

	private final Map<Integer, Collection<String>> valueContainers;

	public void setColumnValuesContainer(int columnIndex,
			Collection<String> container) {
		valueContainers.put(columnIndex, container);
	}

	public Collection<String> getColumnValuesContainer(int columnIndex) {
		return valueContainers.get(columnIndex);
	}
}
