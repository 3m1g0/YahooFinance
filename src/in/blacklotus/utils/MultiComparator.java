package in.blacklotus.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MultiComparator<T> implements Comparator<T> {
	private final List<Comparator<T>> comparators;

	public MultiComparator(List<Comparator<T>> comparators) {
		this.comparators = comparators;
	}

	public int compare(T o1, T o2) {
		for (Comparator<T> c : comparators) {
			int result = c.compare(o1, o2);
			if (result != 0) {
				return result;
			}
		}
		return 0;
	}

	public static <T> void sort(List<T> list, List<Comparator<T>> comparators) {
		Collections.sort(list, new MultiComparator<T>(comparators));
	}
}
