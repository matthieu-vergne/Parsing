package fr.vergne.parsing.samples.csv;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import fr.vergne.ioutils.FileUtils;
import fr.vergne.parsing.samples.csv.Csv.Row;

public class SampleCsv {

	public static void main(String[] args) throws IOException, URISyntaxException {
		String originalCsv = FileUtils
				.readFileToString(new File(ClassLoader.getSystemClassLoader().getResource("smallCSV.csv").toURI()));

		System.out.println("================= ORIGINAL =================");
		System.out.println(originalCsv);

		System.out.println("================= UPDATE =================");
		Csv csv = new Csv();
		csv.setContent(originalCsv);

		System.out.println("Columns: " + csv.getColumns());
		System.out.println("Rows: " + csv.getRows());
		System.out.println("Updating...");

		csv.setCell(2, 1, "Cauldron");
		csv.setCell(0, "Product", "Aluminium");
		csv.setRow(1, new Row("2", "Brick"));
		csv.removeRow(4);
		csv.addRow(0, new Row("0", "Zebra"));

		System.out.println("================= MODIFIED =================");
		System.out.println(csv.getContent());
	}

}
