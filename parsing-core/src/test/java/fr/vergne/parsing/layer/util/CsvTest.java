package fr.vergne.parsing.layer.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import fr.vergne.parsing.layer.util.Csv.Row;
import fr.vergne.parsing.layer.util.Csv.TranformerAssigner;
import fr.vergne.parsing.layer.util.Csv.Transformer;
import fr.vergne.parsing.layer.util.Csv.Value;

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
	public void testRowCount() {
		Csv csv = new Csv();

		csv.setContent("H1,H2,H3\nA1,A2,A3");
		assertEquals(1, csv.getRowsCount());

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");
		assertEquals(3, csv.getRowsCount());

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3");
		assertEquals(2, csv.getRowsCount());
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
	public void testGetRows() {
		Csv csv = new Csv();

		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");
		Iterator<Row> iterator = csv.getRows().iterator();
		assertTrue(iterator.hasNext());
		assertEquals("A1,A2,A3", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("B1,B2,B3", iterator.next().getContent());
		assertTrue(iterator.hasNext());
		assertEquals("C1,C2,C3", iterator.next().getContent());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testRowValues() {
		Csv csv = new Csv();
		csv.setContent("H1,H2,H3\nA1,A2,A3\nB1,B2,B3\nC1,C2,C3");

		Row row = csv.getRow(0);
		assertEquals("A1", row.get(0).getContent());
		assertEquals("A2", row.get(1).getContent());
		assertEquals("A3", row.get(2).getContent());

		row = csv.getRow(1);
		assertEquals("B1", row.get(0).getContent());
		assertEquals("B2", row.get(1).getContent());
		assertEquals("B3", row.get(2).getContent());

		row = csv.getRow(2);
		assertEquals("C1", row.get(0).getContent());
		assertEquals("C2", row.get(1).getContent());
		assertEquals("C3", row.get(2).getContent());
	}

	@Test
	public void testTransformers() {
		Csv csv = new Csv(new TranformerAssigner() {

			@Override
			public Transformer<?> assign(int valueIndex) {
				return new Transformer<Integer>() {

					@Override
					public Integer transform(String value) {
						return Integer.parseInt(value);
					}
				};
			}
		});
		csv.setContent("H1,H2,H3\n1,2,3\n6,5,4");

		Row row = csv.getRow(0);
		assertEquals(1, row.get(0).transform());
		assertEquals(2, row.get(1).transform());
		assertEquals(3, row.get(2).transform());

		row = csv.getRow(1);
		assertEquals(6, row.get(0).transform());
		assertEquals(5, row.get(1).transform());
		assertEquals(4, row.get(2).transform());
	}

	@Test
	public void testTransformersSetWithGet() {
		Csv csv = new Csv(new TranformerAssigner() {

			@Override
			public Transformer<?> assign(int valueIndex) {
				return new Transformer<Integer>() {

					@Override
					public Integer transform(String value) {
						return Integer.parseInt(value);
					}
				};
			}
		});
		csv.setContent("H1,H2,H3\n1,2,3\n6,5,4");

		Row row = csv.getRow(0);
		assertTrue(row.get(0).transform() instanceof Integer);
	}

	@Test
	public void testTransformersSetWithIterator() {
		Csv csv = new Csv(new TranformerAssigner() {

			@Override
			public Transformer<?> assign(int valueIndex) {
				return new Transformer<Integer>() {

					@Override
					public Integer transform(String value) {
						return Integer.parseInt(value);
					}
				};
			}
		});
		csv.setContent("H1,H2,H3\n1,2,3\n6,5,4");

		Row row = csv.getRow(0);
		for (Value value : row) {
			assertTrue(value.transform() instanceof Integer);
		}
	}
	
	@Test
	public void testSmallFile() throws IOException {
		Csv csv = new Csv('\t');
		csv.setContent(FileUtils.readFileToString(new File("src/test/resources/smallFile.csv")));
	}
	
	@Test
	public void testBigFile() throws IOException {
		Csv csv = new Csv('\t');
		csv.setContent(FileUtils.readFileToString(new File("src/test/resources/bigFile.csv")));
	}
}
