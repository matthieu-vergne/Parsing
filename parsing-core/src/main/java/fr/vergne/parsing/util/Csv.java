package fr.vergne.parsing.util;

import java.io.InputStream;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.definition.impl.StandardDefinitionFactory;
import fr.vergne.parsing.layer.Layer;
import fr.vergne.parsing.layer.standard.impl.Constant;
import fr.vergne.parsing.layer.standard.impl.Regex;
import fr.vergne.parsing.layer.standard.impl.SeparatedLoop;
import fr.vergne.parsing.layer.standard.impl.Sequence;
import fr.vergne.parsing.layer.standard.Quantifier;

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
// TODO Move in samples
public class Csv implements Layer {

	private final Definition<Regex> cell;
	private final Definition<Regex> newline;
	private final Definition<SeparatedLoop<Regex, Constant>> row;
	private final Definition<SeparatedLoop<SeparatedLoop<Regex, Constant>, Regex>> rows;
	private final Definition<SeparatedLoop<Regex, Constant>> header;
	private final Sequence csv;

	public Csv() {
		StandardDefinitionFactory factory = new StandardDefinitionFactory();
		cell = factory.defineRegex("[^,\n]*+");
		newline = factory.defineRegex(new Newline().getRegex());
		row = factory.defineSeparatedLoop(cell, ",", 1, Integer.MAX_VALUE, Quantifier.POSSESSIVE);
		rows = factory.defineSeparatedLoop(row, newline, "\n", Quantifier.POSSESSIVE);
		header = factory.defineAs(row);
		csv = factory.defineSequence(header, newline, rows).create();
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

	public String getCell(int row, int column) {
		return csv.get(rows).get(this.row, row).get(cell, column).getContent();
	}

	public void setCell(int row, int column, String content) {
		csv.get(rows).get(this.row, row).get(cell, column).setContent(content);
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
}
