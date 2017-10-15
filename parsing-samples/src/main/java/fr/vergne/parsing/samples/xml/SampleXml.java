package fr.vergne.parsing.samples.xml;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import fr.vergne.ioutils.FileUtils;
import fr.vergne.parsing.samples.xml.Xml.Node;

public class SampleXml {

	public static void main(String[] args) throws IOException, URISyntaxException {
		String originalXml = FileUtils
				.readFileToString(new File(ClassLoader.getSystemClassLoader().getResource("simpleXml.xml").toURI()));

		System.out.println("================= ORIGINAL =================");
		System.out.println(originalXml);

		System.out.println("================= UPDATE =================");
		Xml xml = new Xml();
		xml.setContent(originalXml);

		System.out.println("XML Version: " + xml.getVersion());
		System.out.println("XML Encoding: " + xml.getEncoding());
		System.out.println("Updating...");

		Node root = xml.getNode(0);
		for (Node parent : root.getChildren()) {
			if (parent.hasChildren()) {
				String id = parent.getField("id").getValue();
				parent.getField("id").setValue("modified " + id);

				List<Node> children = parent.getChildren();
				Node originalNode = children.get(children.size() - 1);
				Node copyNode = xml.new Node(originalNode.getContent());
				parent.addChild(copyNode);
				copyNode.getField("id").setValue("new child");
			} else {
				// no change
			}
		}

		System.out.println("================= MODIFIED =================");
		System.out.println(xml.getContent());
	}

}
