package multidimentional;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import util.Pair;

public class Operator {
	public static <T, U, R> Array<R> map2(Array<T> a1, Array<U> a2, BiFunction<T, U, R> f) {
		int[] dims = Array.broadcastedDims(a1, a2);
		Array<R> res = new Array<>(dims);
		Array.ArrayMultiIterator<T, U> multi = new Array.ArrayMultiIterator<>(a1, a2);
		for (Array<R>.ArrayIterator it = res.iterator(); it.hasNext(); it.next()) {
			Pair<T, U> p = multi.next();
			it.set(f.apply(p.first, p.second));
		}
		return res;
	}

	public static <T> void init(Array<T> array, Supplier<T> f) {
		Array<T>.ArrayIterator it = array.iterator();
		while (it.hasNext()) {
			it.set(f.get());
			it.next();
		}
	}
}
