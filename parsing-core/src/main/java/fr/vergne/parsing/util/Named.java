package fr.vergne.parsing.util;

import java.util.function.Function;

/**
 * This interface is a facility for naming elements. As much as possible,
 * {@link #getName()} should return a constant.
 */
public interface Named {
	String getName();

	static <T> String name(T object, Function<? super T, String> defaultNamer) {
		if (object instanceof Named) {
			return ((Named) object).getName();
		} else {
			return defaultNamer.apply(object);
		}
	}

	static String name(Object object) {
		return name(object, (x) -> x.getClass().getSimpleName());
	}
}
