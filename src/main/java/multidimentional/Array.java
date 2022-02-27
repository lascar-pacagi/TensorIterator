package multidimentional;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import util.Pair;

public class Array<T> implements Iterable<T> {
	private T[] array;
	private int[] dims;
	private int[] strides;
	private int[] backStrides;
	private int start;
	private int size;
	private boolean linear;

	public Array(int... dims) {
		this(true, dims);
	}

	@SuppressWarnings("unchecked")
	private Array(boolean allocateArray, int... dims) {
		initWithoutAllocatingArray(dims);
		if (allocateArray) {
			array = (T[]) new Object[size];
		}
	}

	private void initWithoutAllocatingArray(int... dims) {
		this.dims = new int[dims.length];
		strides = new int[dims.length];
		backStrides = new int[dims.length];
		this.dims[dims.length - 1] = dims[dims.length - 1];
		strides[dims.length - 1] = 1;
		backStrides[dims.length - 1] = dims[dims.length - 1] - 1;
		int size = dims[dims.length - 1];
		for (int i = dims.length - 2; i >= 0; i--) {
			this.dims[i] = dims[i];
			size *= dims[i];
			strides[i] = dims[i + 1] * strides[i + 1];
			backStrides[i] = (dims[i] - 1) * strides[i];
		}
		if (size == 0) {
			throw new IllegalArgumentException("Array size is zero");
		}
		this.size = size;
		start = 0;
		linear = true;
	}

	private Array(Array<T> a) {
		array = a.array;
		dims = a.dims;
		strides = a.strides;
		backStrides = a.backStrides;
		start = a.start;
		size = a.size;
		linear = a.linear;
	}

	public int size() {
		return size;
	}

	public int underlyingArrayLength() {
		return array.length;
	}

	public boolean sameUnderlyingArray(Array<T> a) {
		return array == a.array;
	}

	public Array<T> slice(int[][] bounds) {
		if (!correctBounds(bounds)) {
			throw new IllegalArgumentException("Wrong bounds");
		}
		Array<T> res = new Array<>(this);
		res.dims = Arrays.copyOf(dims, dims.length);
		res.backStrides = new int[dims.length];
		int start = this.start;
		boolean linear = true;
		int size = 1;
		for (int i = 0; i < bounds.length; i++) {
			start += bounds[i][0] * strides[i];
			res.dims[i] = bounds[i][1] - bounds[i][0];
			size *= res.dims[i];
			res.backStrides[i] = (res.dims[i] - 1) * strides[i];
			if (res.dims[i] != dims[i]) {
				linear = false;
			}
		}
		res.start = start;
		res.size = size;
		res.linear = linear;
		return res;
	}

	private boolean correctBounds(int[][] bounds) {
		if (bounds.length != dims.length) {
			return false;
		}
		for (int i = 0; i < bounds.length; i++) {
			boolean wrong = 
					bounds[i][0] < 0 || 
					bounds[i][0] >= bounds[i][1] || 
					bounds[i][1] > dims[i];
			if (wrong) {
				return false;
			}
		}
		return true;
	}

	public T get(int... indices) {
		return array[indicesToFlatIndex(indices)];
	}

	public void set(T v, int... indices) {
		array[indicesToFlatIndex(indices)] = v;
	}

	public int indicesToFlatIndex(int[] indices) {
		int res = 0;
		for (int i = 0; i < indices.length; i++) {
			res += indices[i] * strides[i];
		}
		return start + res;
	}

	public int[] flatIndexToIndices(int index) {
		int[] res = new int[dims.length];
		index -= start;
		for (int i = 0; i < strides.length; i++) {
			res[i] = index / strides[i];
			index %= strides[i];
		}
		return res;
	}

	public int[] shape() {
		return Arrays.copyOf(dims, dims.length);
	}

	public Array<T> reshape(int... dims) {
		int nbMinus1 = 
				Arrays.stream(dims)
				.map(x -> x == -1 ? 1 : 0)
				.sum();
		int size = 
				Arrays.stream(dims)
				.filter(x -> x != -1)
				.reduce(1, (x, y) -> x * y);
		boolean wrong = 
				nbMinus1 > 1 || 
				nbMinus1 == 0 && this.size != size || 
				nbMinus1 == 1 && this.size % size != 0;
		if (wrong) {
			throw new IllegalArgumentException("Wrong shape");
		}
		if (nbMinus1 == 1) {
			for (int i = 0; i < dims.length; i++) {
				if (dims[i] == -1) {
					dims[i] = this.size / size;
					break;
				}
			}
		}
		Array<T> res = reshapeShare(dims);
		return res != null ? res : reshapeCopy(dims);
	}

	private Array<T> reshapeShare(int[] dims) {		
		Array<T> res = new Array<>(this);
		res.dims = Arrays.copyOf(dims, dims.length);
		res.backStrides = new int[dims.length];
		res.strides = new int[dims.length];
		Arrays.fill(res.strides, 1);
		ArrayDeque<Pair<Integer, Integer>> q1 = new ArrayDeque<>();
		ArrayDeque<Pair<Integer, Integer>> q2 = new ArrayDeque<>();
		for (int i = 0; i < this.dims.length; i++) {
			if (this.dims[i] != 1) {
				q1.addLast(new Pair<>(i, this.dims[i]));
			}
		}
		for (int i = 0; i < dims.length; i++) {
			q2.addLast(new Pair<>(i, dims[i]));
		}
		while (!q1.isEmpty() && !q2.isEmpty()) {
			Pair<Integer, Integer> p1 = q1.removeFirst();
			Pair<Integer, Integer> p2 = q2.removeFirst();
			int i1 = p1.first, d1 = p1.second;
			int i2 = p2.first, d2 = p2.second;
			if (d1 == d2) {
				res.strides[i2] = strides[i1];
				res.backStrides[i2] = res.strides[i2] * (d2 - 1);
			} else if (d1 > d2) {
				if (d1 % d2 != 0) {
					return null;
				}
				res.strides[i2] = strides[i1] * d1 / d2;
				res.backStrides[i2] = res.strides[i2] * (d2 - 1);
				p1.second /= d2;
				q1.addFirst(p1);				
			} else { // d1 < d2
				if (d2 % d1 != 0) {
					return null;
				}
				Pair<Integer, Integer> p1Next = q1.getFirst();
				if (strides[p1Next.first] * p1Next.second != strides[i1]) {
					return null;
				}
				p2.second /= d1;
				q2.addFirst(p2);
			}
		}
		return res;
	}

	private Array<T> reshapeCopy(int[] dims) {
		Array<T> res = new Array<>(true, dims);
		ArrayIterator it = res.iterator();
		for (T v : this) {
			it.set(v);
			it.next();
		}
		return res;
	}

	public Array<T> copy() {
		Array<T> res = new Array<>(this);
		res.array = Arrays.copyOf(array, array.length);
		return res;
	}

	public static <T, U> int[] broadcastedDims(Array<T> a1, Array<U> a2) {
		int[] res = new int[Math.max(a1.dims.length, a2.dims.length)];
		int i = a1.dims.length - 1;
		int j = a2.dims.length - 1;
		int k = res.length - 1;
		while (i >= 0 && j >= 0) {
			int d1 = a1.dims[i--];
			int d2 = a2.dims[j--];
			if (d1 != d2 && d1 != 1 && d2 != 1) {
				throw new IllegalArgumentException("Cannot broadcast");
			}
			res[k--] = Math.max(d1, d2);
		}
		while (i >= 0) {
			res[k--] = a1.dims[i--];
		}
		while (j >= 0) {
			res[k--] = a2.dims[j--];
		}
		return res;
	}

	@Override
	public ArrayIterator iterator() {
		return new ArrayIterator();
	}

	public class ArrayIterator implements Iterator<T> {
		private int index;
		private int count;
		private int[] coords;

		public ArrayIterator() {
			coords = new int[dims.length];
			index = start;
		}

		@Override
		public boolean hasNext() {
			return count < size;
		}

		@Override
		public T next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			count++;
			if (linear) {
				return array[index++];
			}
			T res = array[index];
			for (int i = dims.length - 1; i >= 0; i--) {
				if (coords[i] < dims[i] - 1) {
					coords[i]++;
					index += strides[i];
					break;
				}
				coords[i] = 0;
				index -= backStrides[i];
			}
			return res;
		}

		public void set(T v) {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			array[index] = v;
		}
	}

	public static class ArrayMultiIterator<T, U> implements Iterator<Pair<T, U>> {
		private Array<T>.ArrayIterator it1;
		private Array<U>.ArrayIterator it2;

		public ArrayMultiIterator(Array<T> a1, Array<U> a2) {
			int[] dims = broadcastedDims(a1, a2);
			Array<T> array1 = create_array(a1, dims);
			Array<U> array2 = create_array(a2, dims);
			it1 = array1.iterator();
			it2 = array2.iterator();
		}

		private static <T> Array<T> create_array(Array<T> a, int[] dims) {
			Array<T> res = new Array<>(false, dims);
			res.array = a.array;
			res.linear = a.linear && Arrays.equals(dims, a.dims);
			updateStrides(res, a, dims);
			return res;
		}

		private static <T> void updateStrides(Array<T> array, Array<T> a, int[] dims) {
			int k = dims.length - 1;
			int i = a.dims.length - 1;
			while (i >= 0) {
				if (a.dims[i] == 1 && dims[k] > 1) {
					array.strides[k] = 0;
					array.backStrides[k] = 0;
				} else {
					array.strides[k] = a.strides[i];
					array.backStrides[k] = a.backStrides[i];
				}
				i--;
				k--;
			}
			while (k >= 0) {
				array.strides[k] = 0;
				array.backStrides[k] = 0;
				k--;
			}
		}

		@Override
		public boolean hasNext() {
			return it1.hasNext();
		}

		@Override
		public Pair<T, U> next() {
			return new Pair<T, U>(it1.next(), it2.next());
		}

	}

}
