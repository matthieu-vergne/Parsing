package fr.vergne.parsing.util;

import java.util.function.Supplier;

import fr.vergne.parsing.layer.standard.Quantifier;

public class RegexRecursivityLimiter {

	public static int DEFAULT_RECURSIVITY_DEPTH = 10;
	private final int depthLimit;
	private final Supplier<String> normalRegex;
	private int depth = 0;

	public RegexRecursivityLimiter(Supplier<String> normalRegex, int depthLimit) {
		this.normalRegex = normalRegex;
		this.depthLimit = depthLimit;
	}

	public RegexRecursivityLimiter(Supplier<String> normalProcessing) {
		this(normalProcessing, DEFAULT_RECURSIVITY_DEPTH);
	}

	public String generate() {
		depth++;
		String regex = depth >= depthLimit ? "[\\s\\S]*" : normalRegex.get();
		depth--;
		return regex;
	}

	public static String buildRegexCardinality(Quantifier quantifier, int min, int max) {
		return buildRegexCardinality(quantifier, min, max, 0);
	}

	private static String buildRegexCardinality(Quantifier quantifier, int min, int max, int consumed) {
		int actualMin = Math.max(min - consumed, 0);
		int actualMax = max == Integer.MAX_VALUE ? Integer.MAX_VALUE : Math.max(max - consumed, 0);
		String decorator;
		if (actualMin == 0 && actualMax == Integer.MAX_VALUE) {
			decorator = "*";
		} else if (actualMin == 0 && actualMax == 1) {
			decorator = "?";
		} else if (actualMin == 1 && actualMax == Integer.MAX_VALUE) {
			decorator = "+";
		} else if (actualMin == actualMax) {
			decorator = "{" + actualMin + "}";
		} else if (actualMax == Integer.MAX_VALUE) {
			decorator = "{" + actualMin + ",}";
		} else {
			decorator = "{" + actualMin + "," + actualMax + "}";
		}
		return decorator + quantifier.getDecorator();
	}
}
