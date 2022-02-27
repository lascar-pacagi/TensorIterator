package util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

	public static String intArrayToString(int[] a) {
		String res = "(";
		for (int i = 0; i < a.length - 1; i++) {
			res += a[i] + ",";
		}
		res += a[a.length - 1] + ")";
		return res;
	}

	public static int[] stringToIntArray(String s) {
		StringTokenizer st = new StringTokenizer(s, "(,) ");
		int[] res = new int[st.countTokens()];
		int i = 0;
		while (st.hasMoreTokens()) {
			res[i++] = Integer.parseInt(st.nextToken());
		}
		return res;
	}

	public static int[][] stringToBounds(String s) {
		StringTokenizer st = new StringTokenizer(s, "(,)[] ");
		int[][] res = new int[st.countTokens()][2];
		int i = 0;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			Pattern p = Pattern.compile("(\\d*):(\\d*)");
			Matcher m = p.matcher(token);
			res[i][0] = 0;
			res[i][1] = -1;
			if (m.matches()) {
				res[i][0] = m.group(1).length() == 0 ? 0 : Integer.parseInt(m.group(1));
				res[i][1] = m.group(2).length() == 0 ? -1 : Integer.parseInt(m.group(2));
			}
			i++;
		}
		return res;
	}

	public static int[] accumulate(int[] a) {
		int[] res = Arrays.copyOf(a, a.length);
		for (int i = a.length - 2; i >= 0; i--) {
			res[i] *= res[i + 1];
		}
		return res;
	}
	
	public static class EmptyIterator<T> implements Iterator<T> {

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public T next() {
			throw new UnsupportedOperationException();
		}

	}

}
