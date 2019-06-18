package krasa.grepconsole.filter;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LockingInputFilterWrapper implements InputFilter {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(LockingInputFilterWrapper.class);
	protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	protected InputFilter inputFilter;
	private long lockedSince;

	public LockingInputFilterWrapper(@NotNull InputFilter inputFilter) {
		this.inputFilter = inputFilter;
		if (inputFilter instanceof AbstractFilter) {
			((AbstractFilter) inputFilter).setLockingInputFilterWrapper(this);
		}
	}

	@Nullable
	@Override
	public List<Pair<String, ConsoleViewContentType>> applyFilter(@NotNull String s, @NotNull ConsoleViewContentType consoleViewContentType) {
		try {
			lock.readLock().lock();
			return inputFilter.applyFilter(s, consoleViewContentType);
		} finally {
			lock.readLock().unlock();
		}
	}

	public void lock() {
		ApplicationManager.getApplication().assertIsDispatchThread();
		if (!lock.isWriteLocked()) {
			lockedSince = System.currentTimeMillis();
		}
		LOG.info("Locking console input " + this);
		lock.writeLock().lock();
	}

	public long getLockedSince() {
		return lockedSince;
	}

	public void unlock() {
		LOG.info("Unlocking console input, locked= " + (System.currentTimeMillis() - getLockedSince()) + "ms " + this);
		lock.writeLock().unlock();
	}

	@Override
	public String toString() {
		return "LockingInputFilterWrapper{" +
				"lock=" + lock +
				", inputFilter=" + inputFilter +
				'}';
	}
}
