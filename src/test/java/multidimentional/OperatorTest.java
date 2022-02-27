package multidimentional;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class OperatorTest {

	@Test
	void testMap2() {
		Array<String> a1 = new Array<>(2, 4, 5);
		for (Array<String>.ArrayIterator it = a1.iterator(); it.hasNext(); it.next()) {
			it.set("Test");
		}
		Array<Integer> a2 = new Array<>(1, 5);
		int k = 0;
		for (Array<Integer>.ArrayIterator it = a2.iterator(); it.hasNext(); it.next()) {
			it.set(k++);
		}
		Array<String> res = Operator.map2(a1, a2, (s, i) -> s + " " + i);
		assertEquals(res.get(1, 2, 0), "Test 0");
		assertEquals(res.get(1, 3, 1), "Test 1");
		assertEquals(res.get(1, 2, 2), "Test 2");
		assertEquals(res.get(0, 1, 3), "Test 3");
		assertEquals(res.get(1, 0, 4), "Test 4");
	}

}
