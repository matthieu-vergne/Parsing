package fr.vergne.parsing.layer.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import fr.vergne.parsing.layer.Layer;

public class RecursivityLimiter<T, R> {

	/**
	 * {@link Layer}s are used in other layers to build tree structures. However, it
	 * is frequent that going deep in the content of a {@link Layer} , we find that
	 * this same {@link Layer} is used recursively, creating a graph with loops.
	 * This variable provides the limit before to stop going deeper. Once this limit
	 * is reached, the regex of the deepest level is considered to be a generic
	 * ".*". Notice that this limit is the number of time we call the same
	 * {@link Layer}, not the number of layer. Thus, as long as it is not recursive,
	 * a really deep structure is completely browsed.
	 */
	public static int DEFAULT_RECURSIVITY_DEPTH = 10;
	private final int depthLimit;
	private final Function<T, R> normalProcessing;
	private final Function<T, R> limitProcessing;
	private final Map<Class<?>, Integer> calls = new HashMap<Class<?>, Integer>();

	public RecursivityLimiter(Function<T, R> normalProcessing, Function<T, R> limitProcessing, int depthLimit) {
		this.normalProcessing = normalProcessing;
		this.limitProcessing = limitProcessing;
		this.depthLimit = depthLimit;
	}

	public RecursivityLimiter(Function<T, R> normalProcessing, Function<T, R> limitProcessing) {
		this(normalProcessing, limitProcessing, DEFAULT_RECURSIVITY_DEPTH);
	}

	public R callOn(T input) {
		Class<?> clazz = input.getClass();
		int depth = calls.containsKey(clazz) ? calls.get(clazz) + 1 : 1;
		R result;
		if (depth >= depthLimit) {
			result = limitProcessing.apply(input);
		} else {
			calls.put(clazz, depth);
			result = normalProcessing.apply(input);
			if (depth == 1) {
				calls.remove(clazz);
			} else {
				calls.put(clazz, depth - 1);
			}
		}
		return result;
	}
}
