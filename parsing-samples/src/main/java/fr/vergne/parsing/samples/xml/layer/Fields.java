package fr.vergne.parsing.samples.xml.layer;

import java.util.NoSuchElementException;

import fr.vergne.parsing.layer.impl.Loop;

public class Fields extends Loop<Field> {

	public Fields() {
		super(new Generator<Field>() {

			@Override
			public Field generates() {
				return new Field();
			}
		});
	}

	public Field getField(String name) {
		for (Field field : this) {
			if (field.getName().getContent().equals(name)) {
				return field;
			} else {
				// continue searching
			}
		}
		throw new NoSuchElementException("No " + name
				+ " field has been found.");
	}
}
