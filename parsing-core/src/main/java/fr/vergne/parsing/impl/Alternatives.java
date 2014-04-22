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
		IncompatibilityException optimist = null;
		for (Structure alternative : alternatives) {
			try {
				alternative.setContent(content);
				currentAlternative = alternatives.indexOf(alternative);
			} catch (IncompatibilityException e) {
				if (optimist == null) {
					optimist = e;
				} else {
					int delta1 = optimist.getEnd() - optimist.getStart();
					int delta2 = e.getEnd() - e.getStart();
					optimist = delta1 <= delta2 ? optimist : e;
				}
			}
		}
		optimist.printStackTrace();
		throw new IncompatibilityException(getRegex(), content, 0,
				content.length());
	}

}
