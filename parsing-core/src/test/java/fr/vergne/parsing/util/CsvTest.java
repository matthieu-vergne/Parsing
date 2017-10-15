package fr.vergne.parsing.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import fr.vergne.ioutils.FileUtils;
import fr.vergne.ioutils.StringUtils;
import fr.vergne.parsing.layer.exception.ParsingException;

public class CsvTest {

	@Test
	public void testColumnsHaveCorrectCount() {
		Csv csv = new Csv();

		csv.setContent("H1,H2,H3\nA1,A2,A3");
		assertEquals(3, csv.getColumns());

		csv.setContent("H1,H2,H3,H4\nA1,A2,A3,A4");
		assertEquals(4, csv.getColumns());
	}

	@Test
	public void testRowsHaveCorrectCount() {
		Csv csv = new Csv();

		csv.setContent("H1,H2,H3\nA1,A2,A3");
		assertEquals(1, csv.getRows());

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");
		assertEquals(3, csv.getRows());

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3");
		assertEquals(2, csv.getRows());

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\n");
		assertEquals(2, csv.getRows());
	}

	@Test
	public void testHeaderCellsHaveCorrectValues() {
		Csv csv = new Csv();

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");
		assertEquals("H1", csv.getHeader(0));
		assertEquals("H2", csv.getHeader(1));
		assertEquals("H3", csv.getHeader(2));
	}

	@Test
	public void testHeaderCellsAreCorrectlyModified() {
		Csv csv = new Csv();

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");
		csv.setHeader(0, "h1");
		csv.setHeader(1, "h2");
		csv.setHeader(2, "h3");
		assertEquals("h1,h2,h3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3", csv.getContent());
	}

	@Test
	public void testHeaderCellModificationFailsOnContentWithComma() {
		Csv csv = new Csv();

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");
		try {
			csv.setHeader(0, "h,1");
			fail("No exception thrown");
		} catch (ParsingException cause) {
		}
	}

	@Test
	public void testRowCellsHaveCorrectValues() {
		Csv csv = new Csv();
		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");

		assertEquals("A1", csv.getCell(0, 0));
		assertEquals("A2", csv.getCell(0, 1));
		assertEquals("A3", csv.getCell(0, 2));

		assertEquals("B1", csv.getCell(1, 0));
		assertEquals("B2", csv.getCell(1, 1));
		assertEquals("B3", csv.getCell(1, 2));

		assertEquals("C1", csv.getCell(2, 0));
		assertEquals("C2", csv.getCell(2, 1));
		assertEquals("C3", csv.getCell(2, 2));
	}

	@Test
	public void testRowCellsAreCorrectlyModified() {
		Csv csv = new Csv();
		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");

		csv.setCell(0, 0, "a1");
		csv.setCell(0, 1, "a2");
		csv.setCell(0, 2, "a3");

		csv.setCell(1, 0, "b1");
		csv.setCell(1, 1, "b2");
		csv.setCell(1, 2, "b3");

		csv.setCell(2, 0, "c1");
		csv.setCell(2, 1, "c2");
		csv.setCell(2, 2, "c3");
		
		assertEquals("H1,H2,H3\na1,a2,a3\nb1,b2,b3\nc1,c2,c3", csv.getContent());
	}

	@Test
	public void testRowCellModificationFailsOnContentWithComma() {
		Csv csv = new Csv();

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");
		try {
			csv.setCell(0, 0, "a,1");
			fail("No exception thrown");
		} catch (ParsingException cause) {
		}
	}

	@Test
	public void testSmallFile() throws IOException {
		Csv csv = new Csv();
		csv.setContent(FileUtils.readFileToString(new File("src/test/resources/smallFile.csv")));
	}

	@Test
	public void testBigFile() throws IOException {
		Csv csv = new Csv();
		csv.setContent(FileUtils.readFileToString(new File("src/test/resources/bigFile.csv")));
	}

	@Test
	public void testInputStreamPreservesSpecialCharacters() throws IOException {
		char sep = ',';
		String original = "";
		original += "a" + sep + "b" + sep + "c" + "\n";
		original += "Σ" + sep + "w(x,y).δQ(y)" + sep + "Σ max(w(x',y).δQ(y))" + "\n";

		Csv csv = new Csv();
		csv.setContent(original);
		String copy = StringUtils.readFromInputStream(csv.getInputStream());
		assertEquals(original, copy);
	}

}
