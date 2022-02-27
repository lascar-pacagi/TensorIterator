package multidimentional;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

import util.Pair;

class ArrayTest {

	@Test
	void testSize() {
		assertEquals(40, new Array<Integer>(2, 4, 5).size());
		assertEquals(2, new Array<Integer>(2).size());
		assertEquals(1, new Array<Integer>(1).size());
		assertEquals(500, new Array<Integer>(10, 1, 50).size());
		assertEquals(240, new Array<Integer>(2, 3, 2, 4, 5).size());
		assertThrows(IllegalArgumentException.class, () -> new Array<Integer>(2, 0, 42));
		assertThrows(IllegalArgumentException.class, () -> new Array<Integer>(0));
		assertThrows(IllegalArgumentException.class, () -> new Array<Integer>(0, 2, 42, 2));
	}

	@Test
	void testShape() {
		int[] dims = new int[] { 2, 5, 20, 1 };
		Array<Integer> a = new Array<>(dims);
		for (int i = 0; i < dims.length; i++) {
			assertEquals(dims[i], a.shape()[i]);
		}
		dims = new int[] { 1 };
		a = new Array<>(dims);
		for (int i = 0; i < dims.length; i++) {
			assertEquals(dims[i], a.shape()[i]);
		}
	}

	@Test
	void testSetAndGet() {
		Array<Integer> a = new Array<>(4, 3);
		for (int i = 0; i < a.shape()[0]; i++) {
			for (int j = 0; j < a.shape()[1]; j++) {
				a.set(i + j, i, j);
			}
		}
		for (int i = 0; i < a.shape()[0]; i++) {
			for (int j = 0; j < a.shape()[1]; j++) {
				assertEquals(i + j, (int) a.get(i, j), "(" + i + "," + j + ")");
			}
		}
		a = new Array<>(2, 8, 1, 20);
		for (int i = 0; i < a.shape()[0]; i++) {
			for (int j = 0; j < a.shape()[1]; j++) {
				for (int k = 0; k < a.shape()[2]; k++) {
					for (int l = 0; l < a.shape()[3]; l++) {
						a.set(i * j * k * l, i, j, k, l);
					}
				}
			}
		}
		for (int i = 0; i < a.shape()[0]; i++) {
			for (int j = 0; j < a.shape()[1]; j++) {
				for (int k = 0; k < a.shape()[2]; k++) {
					for (int l = 0; l < a.shape()[3]; l++) {
						assertEquals(i * j * k * l, (int) a.get(i, j, k, l),
								"(" + i + "," + j + "," + k + "," + l + ")");
					}
				}
			}
		}
	}

	@Test
	void testReshape() {
		Array<Integer> a1 = new Array<>(2, 4);
		for (int j = 0; j < a1.shape()[1]; j++) {
			a1.set(1, 0, j);
		}
		for (int j = 0; j < a1.shape()[1]; j++) {
			a1.set(2, 1, j);
		}
		int sum1 = 0;
		for (int i = 0; i < a1.shape()[0]; i++) {
			for (int j = 0; j < a1.shape()[1]; j++) {
				sum1 += a1.get(i, j);
			}
		}
		Array<Integer> a2 = a1.reshape(4, 2);
		int sum2 = 0;
		for (int i = 0; i < a2.shape()[0]; i++) {
			for (int j = 0; j < a2.shape()[1]; j++) {
				sum2 += a2.get(i, j);
			}
		}
		assertTrue(sum1 == sum2);

		Array<Integer> a3 = a2.reshape(-1);
		assertEquals(a2.shape()[0] * a2.shape()[1], a3.shape()[0]);

		sum2 = 0;
		for (int i = 0; i < a3.shape()[0]; i++) {
			sum2 += a3.get(i);
		}
		assertTrue(sum1 == sum2);

		sum1 = 0;
		for (int j = 0; j < a1.shape()[1]; j++) {
			sum1 += a1.get(0, j);
		}
		sum2 = 0;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < a2.shape()[1]; j++) {
				sum2 += a2.get(i, j);
			}
		}
		assertTrue(sum1 == sum2);

		assertThrows(IllegalArgumentException.class, () -> a2.reshape(4, 5));
		assertThrows(IllegalArgumentException.class, () -> a2.reshape(-1, 2, -1));
		assertThrows(IllegalArgumentException.class, () -> a2.reshape(8, 2));
	}

	@Test
	void testArrayIterator() {
		Array<Integer> a1 = new Array<>(4, 3);
		int sum1 = 0;
		for (int i = 0; i < a1.shape()[0]; i++) {
			for (int j = 0; j < a1.shape()[1]; j++) {
				a1.set(i + j, i, j);
				sum1 += i + j;
			}
		}
		int sum2 = 0;
		for (Integer v : a1) {
			sum2 += v;
		}
		assertEquals(sum1, sum2);
		a1.shape()[0] = 1;
		Array<Integer> a2 = a1.reshape(1, 2, 3, 2);

		sum2 = 0;
		for (Integer v : a2) {
			sum2 += v;
		}
		assertEquals(sum1, sum2);

		for (Array<Integer>.ArrayIterator it = a2.iterator(); it.hasNext(); it.next()) {
			it.set(1);
		}
		sum2 = 0;
		for (Integer v : a2) {
			sum2 += v;
		}
		sum1 = Arrays.stream(a2.shape()).reduce(1, (x, y) -> x * y);
		assertEquals(sum1, sum2);
	}

	@Test
	void testSlice() {
		Array<Integer> a1 = new Array<>(4, 5);
		for (int i = 0; i < a1.shape()[0]; i++) {
			for (int j = 0; j < a1.shape()[1]; j++) {
				a1.set(i + 1, i, j);
			}
		}
		Array<Integer> a2 = a1.slice(new int[][] { { 1, 4 }, { 2, 4 } });
		int sum = 0;
		for (Integer v : a2) {
			sum += v;
		}
		assertEquals(3, (int) a2.get(1, 0));

		Array<Integer> a3 = a2.slice(new int[][] { { 0, 1 }, { 0, 2 } });
		assertEquals(18, sum);
		assertThrows(IndexOutOfBoundsException.class, () -> a3.get(2, 3));

		int sum1 = 0;
		for (Integer v : a3) {
			sum1 += v;
		}
		assertEquals(4, sum1);

		Array<Integer> a4 = a3.slice(new int[][] { { 0, 1 }, { 0, 1 } });
		assertEquals(2, (int) a4.get(0, 0));
		assertEquals(1, a4.size());
		assertTrue(a1.get(1, 2) == a4.get(0, 0));
		a1.set(100, 1, 2);
		assertTrue(a1.get(1, 2) == a4.get(0, 0));
	}

	@Test
	void testBroadcastedDims() {
		Array<Integer> a1 = new Array<>(4, 1);
		Array<Integer> a2 = new Array<>(3);
		int[] dims = Array.broadcastedDims(a1, a2);
		assertArrayEquals(new int[] { 4, 3 }, dims);

		a1 = new Array<>(4, 3);
		a2 = new Array<>(5, 4, 1);
		dims = Array.broadcastedDims(a1, a2);
		assertArrayEquals(new int[] { 5, 4, 3 }, dims);

		final Array<Integer> a3 = new Array<>(4, 3);
		final Array<Integer> a4 = new Array<>(5, 2, 1);
		assertThrows(IllegalArgumentException.class, () -> Array.broadcastedDims(a3, a4));
	}

	@Test
	void testArrayMultiIterator() {
		Array<Integer> a1 = new Array<>(4, 1);
		for (Array<Integer>.ArrayIterator it = a1.iterator(); it.hasNext(); it.next()) {
			it.set(1);
		}
		Array<Integer> a2 = new Array<>(2, 1, 2);
		for (Array<Integer>.ArrayIterator it = a2.iterator(); it.hasNext(); it.next()) {
			it.set(10);
		}
		Iterator<Pair<Integer, Integer>> it = new Array.ArrayMultiIterator<>(a1, a2);
		int sum = 0;
		while (it.hasNext()) {
			Pair<Integer, Integer> p = it.next();
			sum += p.first + p.second;
		}
		assertEquals(176, sum);

		a1 = new Array<>(1);
		for (Array<Integer>.ArrayIterator it1 = a1.iterator(); it1.hasNext(); it1.next()) {
			it1.set(10);
		}
		a2 = new Array<>(5, 2, 3, 4);
		for (Array<Integer>.ArrayIterator it2 = a2.iterator(); it2.hasNext(); it2.next()) {
			it2.set(1);
		}
		it = new Array.ArrayMultiIterator<>(a1, a2);
		sum = 0;
		while (it.hasNext()) {
			Pair<Integer, Integer> p = it.next();
			sum += p.first + p.second;
		}
		assertEquals(1320, sum);

		a1 = new Array<>(3, 3);
		int k = 0;
		for (int i = 0; i < a1.shape()[0]; i++) {
			for (int j = 0; j < a1.shape()[1]; j++) {
				a1.set(k++, i, j);
			}
		}
		a2 = new Array<>(3, 1);
		a2.set(10, 0, 0);
		a2.set(200, 1, 0);
		a2.set(3000, 2, 0);
		it = new Array.ArrayMultiIterator<>(a1, a2);
		sum = 0;
		k = 0;
		while (it.hasNext() && k < 3) {
			Pair<Integer, Integer> p = it.next();
			sum += p.first + p.second;
			k++;
		}
		assertEquals(33, sum);
	}
}
