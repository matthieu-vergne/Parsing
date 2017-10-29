package fr.vergne.parsing.samples.csv;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.standard.Constant;
import fr.vergne.parsing.layer.standard.Quantifier;
import fr.vergne.parsing.layer.standard.Regex;
import fr.vergne.parsing.layer.standard.SeparatedLoop;
import fr.vergne.parsing.layer.standard.Sequence;
import fr.vergne.parsing.layer.standard.impl.StandardDefinitionFactory;
import fr.vergne.parsing.samples.csv.Csv.Row;
import fr.vergne.parsing.util.Newline;

/**
 * A {@link Csv} aims at parsing a Comma Separated Value file.
 * 
 * @author Matthieu Vergne <matthieu.vergne@gmail.com>
 * 
 */
// TODO Ensure that all rows and headers have the same number of columns
public class Csv implements Layer, Iterable<Row> {

	private static final Definition<Regex> cell;
	private static final Definition<Regex> newline;
	private static final Definition<SeparatedLoop<Regex, Constant>> row;
	private static final Definition<SeparatedLoop<SeparatedLoop<Regex, Constant>, Regex>> rows;
	private static final Definition<SeparatedLoop<Regex, Constant>> header;
	private static final Definition<Sequence> csvDefinition;
	private final Sequence csv;

	static {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		cell = factory.defineRegex("[^,\n]*+");
		newline = factory.defineRegex(new Newline().getRegex());
		row = factory.defineSeparatedLoop(cell, ",", 1, Integer.MAX_VALUE, Quantifier.POSSESSIVE);
		rows = factory.defineSeparatedLoop(row, newline, "\n", Quantifier.POSSESSIVE);
		header = factory.defineAs(row);
		csvDefinition = factory.defineSequence(header, newline, rows);
	}

	public Csv() {
		csv = csvDefinition.create();
	}

	public int getColumns() {
		return csv.get(header).size();
	}

	public int getRows() {
		int size = csv.get(rows).size();
		if (csv.get(rows).get(size - 1).getContent().isEmpty()) {
			return size - 1;
		} else {
			return size;
		}
	}

	public String getHeader(int column) {
		return csv.get(header).get(cell, column).getContent();
	}

	public void setHeader(int column, String content) {
		csv.get(header).get(cell, column).setContent(content);
	}

	public Row getRow(int row) {
		return new Row(csv.get(rows).get(Csv.row, row));
	}

	public String getCell(int row, int column) {
		return csv.get(rows).get(Csv.row, row).get(cell, column).getContent();
	}

	public String getCell(int row, String column) {
		return getCell(row, getColumnFromName(column));
	}

	public void setCell(int row, int column, String content) {
		csv.get(rows).get(Csv.row, row).get(cell, column).setContent(content);
	}

	public void setCell(int row, String column, String content) {
		setCell(row, getColumnFromName(column), content);
	}

	private int getColumnFromName(String name) {
		for (int i = 0; i < getColumns(); i++) {
			if (getHeader(i).equals(name)) {
				return i;
			} else {
				continue;
			}
		}
		throw new IllegalArgumentException("Unknown column: " + name);
	}

	public void addRow(int row, Row content) {
		csv.get(rows).add(row, content.getLayer());
	}

	public Row setRow(int row, Row content) {
		addRow(row, content);
		return removeRow(row + 1);
	}

	public Row removeRow(int row) {
		return new Row(csv.get(rows).remove(row));
	}

	@Override
	public void setContent(String content) {
		csv.setContent(content);
	}

	@Override
	public String getContent() {
		return csv.getContent();
	}

	@Override
	public InputStream getInputStream() throws NoContentException {
		return csv.getInputStream();
	}

	@Override
	public void addContentListener(ContentListener listener) {
		csv.addContentListener(listener);
	}

	@Override
	public void removeContentListener(ContentListener listener) {
		csv.removeContentListener(listener);
	}

	@Override
	public Iterator<Row> iterator() {
		Iterator<SeparatedLoop<Regex, Constant>> iterator = csv.get(rows).iterator();
		return new Iterator<Row>() {

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public Row next() {
				return new Row(iterator.next());
			}
		};
	}

	public static class Row implements Iterable<String> {
		private final SeparatedLoop<Regex, Constant> row;

		public Row(List<String> cells) {
			this.row = Csv.row.create();
			Iterator<String> iterator = cells.iterator();
			this.row.setContent(iterator.next());
			int index = 1;
			while (iterator.hasNext()) {
				String content = iterator.next();
				this.row.add(index, content);
				index++;
			}
		}

		public Row(String... cells) {
			this(Arrays.asList(cells));
		}

		private Row(SeparatedLoop<Regex, Constant> row) {
			this.row = row;
		}

		private SeparatedLoop<Regex, Constant> getLayer() {
			return row;
		}

		public String getCell(int column) {
			return row.get(column).getContent();
		}

		public String setCell(int column, String content) {
			Regex cell = row.get(column);
			String oldContent = cell.getContent();
			cell.setContent(content);
			return oldContent;
		}

		@Override
		public Iterator<String> iterator() {
			Iterator<Regex> iterator = row.iterator();
			return new Iterator<String>() {

				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public String next() {
					return iterator.next().getContent();
				}
			};
		}
	}
}
