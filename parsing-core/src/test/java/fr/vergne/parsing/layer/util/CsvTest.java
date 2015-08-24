package fr.vergne.parsing.layer.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import fr.vergne.ioutils.FileUtils;
import fr.vergne.ioutils.StringUtils;
import fr.vergne.parsing.layer.util.Csv.Record;

public class CsvTest {

	@Test
	public void testColumnCountWithComma() {
		Csv csv = new Csv();

		csv.setContent("H1,H2,H3\nA1,A2,A3");
		assertEquals(3, csv.getColumnsCount());

		csv.setContent("H1,H2,H3,H4\nA1,A2,A3,A4");
		assertEquals(4, csv.getColumnsCount());
	}

	@Test
	public void testColumnCountWithTabs() {
		Csv csv = new Csv('\t');

		csv.setContent("H1\tH2\tH3\nA1\tA2\tA3");
		assertEquals(3, csv.getColumnsCount());

		csv.setContent("H1\tH2\tH3\tH4\nA1\tA2\tA3\tA4");
		assertEquals(4, csv.getColumnsCount());
	}

	@Test
	public void testRecordCount() {
		Csv csv = new Csv();

		csv.setContent("H1,H2,H3\nA1,A2,A3");
		assertEquals(1, csv.getRecordsCount());

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");
		assertEquals(3, csv.getRecordsCount());

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3");
		assertEquals(2, csv.getRecordsCount());

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\n");
		assertEquals(2, csv.getRecordsCount());
	}

	@Test
	public void testHeaderValues() {
		Csv csv = new Csv();

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");
		List<String> headers = csv.getHeaders();
		assertEquals("H1", headers.get(0));
		assertEquals("H2", headers.get(1));
		assertEquals("H3", headers.get(2));
	}

	@Test
	public void testGetRecords() {
		Csv csv = new Csv();

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");
		Iterator<Record> iterator = csv.getRecords().iterator();
		assertTrue(iterator.hasNext());
		assertEquals("A1,A2,A3", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("B1,B2,B3", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("C1,C2,C3", iterator.next().getContent());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testRecordValues() {
		Csv csv = new Csv();
		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");

		Record record = csv.getRecord(0);
		assertEquals("A1", record.getStringValue(0));
		assertEquals("A2", record.getStringValue(1));
		assertEquals("A3", record.getStringValue(2));

		record = csv.getRecord(1);
		assertEquals("B1", record.getStringValue(0));
		assertEquals("B2", record.getStringValue(1));
		assertEquals("B3", record.getStringValue(2));

		record = csv.getRecord(2);
		assertEquals("C1", record.getStringValue(0));
		assertEquals("C2", record.getStringValue(1));
		assertEquals("C3", record.getStringValue(2));
	}

	@Test
	public void testColumnValuesContainerRetrieveProperValues() {
		Csv csv = new Csv();
		Set<String> container0 = new HashSet<String>();
		Set<String> container1 = new HashSet<String>();
		Set<String> container2 = new HashSet<String>();
		csv.setColumnValuesContainer(0, container0);
		csv.setColumnValuesContainer(1, container1);
		csv.setColumnValuesContainer(2, container2);
		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");

		assertEquals(container0.toString(), 3, container0.size());
		assertTrue(container0.toString(), container0.contains("A1"));
		assertTrue(container0.toString(), container0.contains("B1"));
		assertTrue(container0.toString(), container0.contains("C1"));

		assertEquals(container1.toString(), 3, container1.size());
		assertTrue(container1.toString(), container1.contains("A2"));
		assertTrue(container1.toString(), container1.contains("B2"));
		assertTrue(container1.toString(), container1.contains("C2"));

		assertEquals(container2.toString(), 3, container2.size());
		assertTrue(container2.toString(), container2.contains("A3"));
		assertTrue(container2.toString(), container2.contains("B3"));
		assertTrue(container2.toString(), container2.contains("C3"));
	}

	@Test
	public void testSmallFile() throws IOException {
		Csv csv = new Csv('\t');
		csv.setContent(FileUtils.readFileToString(new File(
				"src/test/resources/smallFile.csv")));
	}

	@Test
	public void testBigFile() throws IOException {
		Csv csv = new Csv('\t');
		csv.setContent(FileUtils.readFileToString(new File(
				"src/test/resources/bigFile.csv")));
	}

	@Test
	public void testInputStreamPreservesSpecialCharacters() throws IOException {
		char sep = '\t';
		String original = "";
		original += "a" + sep + "b" + sep + "c" + "\n";
		original += "Σ" + sep + "w(x,y).δQ(y)" + sep + "Σ max(w(x',y).δQ(y))"
				+ "\n";

		Csv csv = new Csv(sep);
		csv.setContent(original);
		String copy = StringUtils.readFromInputStream(csv.getInputStream());
		assertEquals(original, copy);
	}

}
