package krasa.grepconsole.grep;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;

class FakeQueue implements Queue<Pair<String, Key>> {
	@Override
	public boolean add(Pair<String, Key> stringKeyPair) {
		return false;
	}
	@Override
	public boolean offer(Pair<String, Key> stringKeyPair) {
		return false;
	}

	@Override
	public Pair<String, Key> remove() {
		return null;
	}

	@Override
	public Pair<String, Key> poll() {
		return null;
	}

	@Override
	public Pair<String, Key> element() {
		return null;
	}

	@Override
	public Pair<String, Key> peek() {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean contains(Object o) {
		return false;
	}

	@NotNull
	@Override
	public Iterator<Pair<String, Key>> iterator() {
		return Collections.emptyIterator();
	}

	@NotNull
	@Override
	public Object[] toArray() {
		return new Object[0];
	}

	@NotNull
	@Override
	public <T> T[] toArray(@NotNull T[] a) {
		return null;
	}

	@Override
	public boolean remove(Object o) {
		return false;
	}

	@Override
	public boolean containsAll(@NotNull Collection<?> c) {
		return false;
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends Pair<String, Key>> c) {
		return false;
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> c) {
		return false;
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> c) {
		return false;
	}

	@Override
	public void clear() {

	}
}
