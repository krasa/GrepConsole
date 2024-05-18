package krasa.grepconsole.grep;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;

class FakeQueue implements Queue<GrepBeforeAfterModel.Line> {
	@Override
	public boolean add(GrepBeforeAfterModel.Line stringKeyPair) {
		return false;
	}
	@Override
	public boolean offer(GrepBeforeAfterModel.Line stringKeyPair) {
		return false;
	}

	@Override
	public GrepBeforeAfterModel.Line remove() {
		return null;
	}

	@Override
	public GrepBeforeAfterModel.Line poll() {
		return null;
	}

	@Override
	public GrepBeforeAfterModel.Line element() {
		return null;
	}

	@Override
	public GrepBeforeAfterModel.Line peek() {
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
	public Iterator<GrepBeforeAfterModel.Line> iterator() {
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
	public boolean addAll(@NotNull Collection<? extends GrepBeforeAfterModel.Line> c) {
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
