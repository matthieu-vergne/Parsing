package fr.vergne.parsing.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import fr.vergne.parsing.Structure;
import fr.vergne.parsing.exception.IncompatibilityException;

public class Alternatives implements Structure {

	private final List<? extends Structure> alternatives;
	private Integer currentAlternative = null;

	public Alternatives(Collection<? extends Structure> alternatives) {
		this.alternatives = Collections
				.unmodifiableList(new LinkedList<Structure>(alternatives));
	}

	@Override
	public String getRegex() {
		String regex = "";
		for (Structure structure : alternatives) {
			regex += "|(?:" + structure.getRegex() + ")";
		}
		return "(?:" + regex.substring(1) + ")";
	}

	@Override
	public String getContent() {
		if (currentAlternative == null) {
			throw new NoSuchElementException();
		} else {
			return alternatives.get(currentAlternative).getContent();
		}
	}

	@Override
	public void setContent(String content) {
		for (Structure alternative : alternatives) {
			try {
				alternative.setContent(content);
				currentAlternative = alternatives.indexOf(alternative);
				return;
			} catch (IncompatibilityException e) {
				// try another
			}
		}
		throw new IncompatibilityException(getRegex(), content, 0,
				content.length());
	}

}
