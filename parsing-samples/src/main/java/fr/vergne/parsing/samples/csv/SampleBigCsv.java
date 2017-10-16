package fr.vergne.parsing.samples.csv;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import fr.vergne.ioutils.FileUtils;
import fr.vergne.parsing.samples.csv.Csv.Row;

public class SampleBigCsv {

	public static void main(String[] args) throws IOException, URISyntaxException {
		File file = new File(ClassLoader.getSystemClassLoader().getResource("bigCSV.csv").toURI());
		String originalCsv = FileUtils.readFileToString(file);

		System.out.println("================= BIG CSV =================");
		Csv csv = new Csv();
		long parsingStart = System.currentTimeMillis();
		csv.setContent(originalCsv);
		long parsingTime = System.currentTimeMillis() - parsingStart;

		int textLength = originalCsv.length();
		float textSpeed = (float) textLength / parsingTime * 1000;

		long fileSize = file.length();
		float byteSpeed = (float) fileSize / 1024 / parsingTime * 1000;

		System.out.println("File size: " + fileSize + " bytes (" + (fileSize / 1024) + " kiB)");
		System.out.println("Text Length: " + textLength + " characters");
		System.out.println("Parsing time : " + parsingTime + " ms");
		System.out.println("Parsing speed : " + textSpeed + " characters/s (" + byteSpeed + " kiB/s)");
		System.out.println("Columns: " + csv.getColumns());
		System.out.println("Rows: " + csv.getRows());
		System.out.println("Headers: " + csv.getColumns());
		int cellCount = csv.getRows() * csv.getColumns();
		System.out.println("Cells: " + cellCount);

		int average = 0;
		for (int i = 0; i < csv.getColumns(); i++) {
			average += csv.getHeader(i).length();
		}
		average /= csv.getColumns();
		System.out.println("Header length (Avg) : " + average + " characters");

		long start = System.currentTimeMillis();
		average = 0;
		for (Row row : csv) {
			for (String cell : row) {
				average += cell.length();
			}
		}
		long time = System.currentTimeMillis() - start;
		float timePerCell = (float) time / cellCount;
		average /= cellCount;
		System.out.println("Cell length (Avg) : " + average + " characters (computing time: " + time + " ms, "
				+ timePerCell + " ms/cell)");
	}
}
