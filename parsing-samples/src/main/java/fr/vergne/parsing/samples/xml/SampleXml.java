package fr.vergne.parsing.samples.xml;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import fr.vergne.ioutils.FileUtils;
import fr.vergne.parsing.layer.standard.Formula;
import fr.vergne.parsing.samples.xml.layer.Node;
import fr.vergne.parsing.samples.xml.layer.NodeList;
import fr.vergne.parsing.samples.xml.layer.Xml;

public class SampleXml {

	public static void main(String[] args) throws IOException,
			URISyntaxException {
		String original = FileUtils.readFileToString(new File(ClassLoader
				.getSystemClassLoader().getResource("simpleXml.xml").toURI()));

		System.out.println("================= ORIGINAL =================");
		System.out.println(original);

		System.out.println("================= UPDATE =================");
		Xml xml = new Xml();
		xml.setContent(original);

		System.out.println("XML Version: "
				+ xml.getHeader().getVersion().getValue().getContent());
		System.out.println("XML Encoding: "
				+ xml.getHeader().getEncoding().getValue().getContent());
		System.out.println("Updating...");

		Node root = xml.getTree().getNodes().get(0);
		for (Node parent : root.getChildren()) {
			if (parent.hasChildren()) {
				Formula id = parent.getField("id").getValue();
				id.setContent("modified " + id.getContent());

				NodeList manager = parent.getChildrenManager();
				String duplicateContent = manager.get(manager.size() - 1)
						.getContent();
				Node duplicate = manager.add(manager.size(), duplicateContent);
				duplicate.getField("id").getValue().setContent("new child");
			} else {
				// no change
			}
		}

		System.out.println("================= MODIFIED =================");
		System.out.println(xml.getContent());
	}

}
