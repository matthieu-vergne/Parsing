package fr.vergne.parsing.samples.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import fr.vergne.parsing.definition.Definition;
import fr.vergne.parsing.layer.standard.Choice;
import fr.vergne.parsing.layer.standard.Loop;
import fr.vergne.parsing.layer.standard.Regex;
import fr.vergne.parsing.layer.standard.Sequence;
import fr.vergne.parsing.layer.standard.impl.StandardDefinitionFactory;
import fr.vergne.parsing.layer.standard.impl.StandardDefinitionFactory.DefinitionProxy;

public class Xml {

	private final StandardDefinitionFactory factory = new StandardDefinitionFactory();

	private Definition<Sequence> field;
	private Definition<Regex> fieldName;
	private Definition<Regex> fieldValue;
	private Definition<Loop<Sequence>> fields;

	private Definition<Loop<Choice>> childrenNode;
	private Definition<Sequence> autoClosingNode;
	private Definition<Sequence> nonAutoClosingNode;

	private Definition<Sequence> header;
	private final Definition<Sequence> tree;

	private final Sequence xml;

	private Definition<Choice> node;

	public Xml() {
		// An XML file has many blank characters (e.g. spaces, tabs, newlines), so we
		// create a definition for them
		Definition<Regex> blank = factory.defineRegex("\\s*");

		// Each XML node may have fields, with their name and value
		fieldName = factory.defineRegex("[a-z]+");
		fieldValue = factory.defineRegex("[^\"]*");
		field = factory.defineSequence(blank, fieldName, factory.defineConstant("=\""), fieldValue,
				factory.defineConstant("\""), blank);
		fields = factory.defineLoop(field);

		// An auto-closing node is a node which defines no content (<node ... />)
		autoClosingNode = factory.defineSequence(blank, factory.defineConstant("<node"), fields,
				factory.defineConstant("/>"));

		// If it is not auto-closed it may have children, which we will define later
		// through a proxy
		DefinitionProxy<Loop<Choice>> childrenNodeProxy = factory.prepareDefinition();
		childrenNode = childrenNodeProxy.getDefinition();
		nonAutoClosingNode = factory.defineSequence(blank, factory.defineConstant("<node"), fields,
				factory.defineConstant(">"), childrenNode, blank, factory.defineConstant("</node>"));

		// A node can be a auto-closing or not
		node = factory.defineChoice(autoClosingNode, nonAutoClosingNode);

		// Now we know what is a node, so we can define children nodes through the proxy
		childrenNodeProxy.defineAs(factory.defineLoop(node));

		// The XML nodes are all enclosed in a a XML tree
		tree = factory.defineSequence(factory.defineConstant("<tree>"), childrenNode, blank,
				factory.defineConstant("</tree>"));

		// The XML file also has a header, with its own fields
		header = factory.defineSequence(factory.defineConstant("<?xml"), fields, factory.defineConstant("?>"));

		// Finally, a complete XML file is a header followed by a tree.
		// Here, we don't need the definition, so we directly create the instance.
		xml = factory.defineSequence(header, blank, tree).create();
	}

	public Field getHeaderField(int index) {
		return new Field(xml.get(header).get(fields).get(index));
	}

	public String getHeaderField(String name) {
		Loop<Sequence> loop = xml.get(header).get(fields);
		for (Sequence field : loop) {
			if (field.get(fieldName).getContent().equals(name)) {
				return field.get(fieldValue).getContent();
			} else {
				continue;
			}
		}
		throw new NoSuchElementException("No " + name + " field in the header.");
	}

	public String getVersion() {
		return getHeaderField("version");
	}

	public String getEncoding() {
		return getHeaderField("encoding");
	}

	public Node getNode(int index) {
		return new Node(xml.get(tree).get(childrenNode).get(index));
	}

	public void setContent(String content) {
		xml.setContent(content);
	}

	public String getContent() {
		return xml.getContent();
	}

	public class Node {

		private final Choice choice;

		private Node(Choice choice) {
			this.choice = choice;
		}

		public Node(String content) {
			this(node.create());
			setContent(content);
		}

		public String getContent() {
			return choice.getContent();
		}

		public void setContent(String content) {
			choice.setContent(content);
		}

		public Field getField(int index) {
			return new Field(getFieldLoop().get(index));
		}

		private Loop<Sequence> getFieldLoop() {
			if (choice.is(autoClosingNode)) {
				return choice.getAs(autoClosingNode).get(fields);
			} else {
				return choice.getAs(nonAutoClosingNode).get(fields);
			}
		}

		public Field getField(String name) {
			for (Sequence field : getFieldLoop()) {
				if (field.get(fieldName).getContent().equals(name)) {
					return new Field(field);
				} else {
					// continue searching
				}
			}
			throw new NoSuchElementException("No " + name + " field has been found.");
		}

		public boolean hasChildren() {
			return choice.is(nonAutoClosingNode);
		}

		public int getChildrenCount() {
			ensureNonAutoClosingNode();
			return choice.getAs(nonAutoClosingNode).get(childrenNode).size();
		}

		public List<Node> getChildren() {
			ensureNonAutoClosingNode();
			Loop<Choice> loop = choice.getAs(nonAutoClosingNode).get(childrenNode);
			List<Node> children = new ArrayList<>(loop.size());
			for (Choice choice : loop) {
				children.add(new Node(choice));
			}
			return children;
		}

		public Node getChild(int index) {
			ensureNonAutoClosingNode();
			return new Node(choice.getAs(nonAutoClosingNode).get(childrenNode).get(index));
		}

		public void addChild(int index, Node node) {
			ensureNonAutoClosingNode();
			choice.getAs(nonAutoClosingNode).get(childrenNode).add(index, node.choice);
		}

		public void addChild(Node node) {
			addChild(getChildrenCount(), node);
		}

		public Node removeChild(int index) {
			ensureNonAutoClosingNode();
			return new Node(choice.getAs(nonAutoClosingNode).get(childrenNode).remove(index));
		}

		private void ensureNonAutoClosingNode() {
			if (choice.is(autoClosingNode)) {
				throw new NoSuchElementException("This node is auto-closing.");
			} else {
				return;
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (obj instanceof Node) {
				Node n = (Node) obj;
				return n.choice == choice;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return choice.hashCode();
		}
	}

	public class Field {

		private final Sequence sequence;

		private Field(Sequence sequence) {
			this.sequence = sequence;
		}

		public Field(String content) {
			this(field.create());
			setContent(content);
		}

		public String getContent() {
			return sequence.getContent();
		}

		public void setContent(String content) {
			sequence.setContent(content);
		}

		public String getName() {
			return sequence.get(fieldName).getContent();
		}

		public void setName(String name) {
			sequence.get(fieldName).setContent(name);
		}

		public String getValue() {
			return sequence.get(fieldValue).getContent();
		}

		public void setValue(String value) {
			sequence.get(fieldValue).setContent(value);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			} else if (obj instanceof Field) {
				Field n = (Field) obj;
				return n.sequence == sequence;
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return sequence.hashCode();
		}
	}

}
