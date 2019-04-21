package krasa.grepconsole.filter;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import krasa.grepconsole.model.StreamBufferSettings;
import krasa.grepconsole.utils.Utils;

import java.util.concurrent.ConcurrentLinkedDeque;

public class StreamBuffer implements Disposable {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(StreamBuffer.class);

	private final long currentlyPrintingDeltaNano;
	private final long maxWaitTimeNano;
	private final SleepingPolicy sleepingPolicy;
	private ConsoleView console;

	private final ConcurrentLinkedDeque<Pair<String, ConsoleViewContentType>> otherOutput = new ConcurrentLinkedDeque<>();
	private final ConcurrentLinkedDeque<String> errorOutput = new ConcurrentLinkedDeque<>();
	private final ConcurrentLinkedDeque<String> systemOutput = new ConcurrentLinkedDeque<>();

	private volatile long firstErrorNano = 0;
	private volatile boolean lastErrorMissingNewLine;
	private volatile long lastErrorNano;
	private volatile long lastNonErrorNano = 0;

	private volatile Thread worker;
	private boolean lastPolledError;
	private volatile boolean exit = false;

	private final Object STICK = new Object();
	public final Object LOOP_GUARD = new Object();


	public StreamBuffer(ConsoleView console, StreamBufferSettings streamBufferSettings) {
		this.console = console;
		Disposer.register(console, this);

		currentlyPrintingDeltaNano = Utils.toNano(streamBufferSettings.getCurrentlyPrintingDelta(), StreamBufferSettings.CURRENTLY_PRINTING_DELTA);
		maxWaitTimeNano = Utils.toNano(streamBufferSettings.getMaxWaitTime(), StreamBufferSettings.MAX_WAIT_TIME);
		sleepingPolicy = new SleepingPolicy(streamBufferSettings.getSleepTimeWhenWasActive(), streamBufferSettings.getSleepTimeWhenIdle());
		startWorker();
	}

	private void startWorker() {
		worker = new Thread(new Runnable() {
			@Override
			public void run() {
				StreamBuffer.this.threadWork(StreamBuffer.this);
			}
		}, StreamBuffer.class.getName());
		worker.setDaemon(true);
		worker.start();
	}

	public boolean buffer(String text, ConsoleViewContentType consoleViewContentType) {
		if (exit) {
			return false;
		}
		if (consoleViewContentType == ConsoleViewContentType.ERROR_OUTPUT) {
			checkIfEndsWithNewLine(text);
			bufferError(text);
		} else if (consoleViewContentType == ConsoleViewContentType.SYSTEM_OUTPUT) {
			lastNonErrorNano = System.nanoTime();
			return false;
//				bufferSystem(text);
		} else if (consoleViewContentType == ConsoleViewContentType.USER_INPUT) {
			return false;
		} else {
			bufferOther(text, consoleViewContentType);
		}
//		synchronized (STICK) {
//			STICK.notify();
//		}
		return true;
	}

	private void bufferSystem(String text) {
		systemOutput.add(text);
		lastNonErrorNano = System.nanoTime();
	}

	private void bufferError(String text) {
		errorOutput.add(text);
		if (firstErrorNano == 0) {
			firstErrorNano = System.nanoTime();
		}
		lastErrorNano = System.nanoTime();
	}

	private void bufferOther(String text, ConsoleViewContentType consoleViewContentType) {
		lastNonErrorNano = System.nanoTime();
		otherOutput.add(Pair.create(text, consoleViewContentType));
	}


	private void threadWork(StreamBuffer streamBuffer) {
		while (!exit) {
			boolean worked;
			synchronized (LOOP_GUARD) {
				worked = streamBuffer.flush();
			}

			synchronized (STICK) {
				try {
					STICK.wait(sleepingPolicy.getTimeToSleep(worked));
				} catch (InterruptedException e) {
					LOG.error(e);
					exit = true;
					return;
				}
			}
		}

	}

	private boolean flush() {
		boolean anyPolled = false;
		if (lastPolledError) {
			anyPolled |= flushError();
			anyPolled |= flushNonError();
		} else {
			anyPolled |= flushNonError();
			anyPolled |= flushError();
		}
//		anyPolled |= flushSystem();
		return anyPolled;
	}


	private boolean flushNonError() {
		boolean anyPolled = false;
		Pair<String, ConsoleViewContentType> temp = null;
		try {
			Pair<String, ConsoleViewContentType> poll = otherOutput.poll();
			if (poll != null) {
				anyPolled = true;
				lastPolledError = false;
			}

			while (poll != null) {
				if (poll.first.endsWith("\n")) {
					console.print(poll.first, poll.second);
					poll = otherOutput.poll();
				} else {
					temp = poll;
					poll = otherOutput.poll();
					if (poll != null) {
						if (poll.second == temp.second) {
							poll = Pair.create(temp.first + poll.first, poll.second);
							temp = null;
						} else {
							console.print(temp.first, temp.second);
							temp = null;
						}
					}
				}
			}
		} finally {
			if (temp != null) {
				otherOutput.addFirst(temp);
			}
		}
		return anyPolled;
	}

	private boolean flushError() {
		long current = System.nanoTime();
		if ((nonErrorBeingPrinted(current) || errorsBeingPrinted(current) || lastErrorMissingNewLine
		)
				&& notWaitingTooLong(current) && consistencyCheck()
		) {
			return false;
		}

		return flush(errorOutput, ConsoleViewContentType.ERROR_OUTPUT);
	}


	private boolean flushSystem() {
		return flush(systemOutput, ConsoleViewContentType.SYSTEM_OUTPUT);
	}

	private boolean flush(ConcurrentLinkedDeque<String> queue, ConsoleViewContentType contentType) {
		String temp = null;
		String poll = queue.poll();
		boolean anyPolled = poll != null;

		try {
			while (poll != null) {
				if (poll.endsWith("\n")) {
					console.print(poll, contentType);
					poll = queue.poll();
				} else {
					temp = poll;
					poll = queue.poll();
					if (poll != null) {
						poll = temp + poll;
						temp = null;
					}
				}
			}
		} finally {
			if (temp != null) {
				queue.addFirst(temp);
			}

			if (contentType == ConsoleViewContentType.ERROR_OUTPUT) {
				if (temp != null) {
					firstErrorNano = System.nanoTime();
				} else {
					firstErrorNano = 0;    //something new could already be in the queue				
				}
				if (anyPolled) {
					lastPolledError = true;
				}
			}
		}
		return anyPolled;
	}


	protected void checkIfEndsWithNewLine(String text) {
		//something wrong, better to wait before flushing errors
		this.lastErrorMissingNewLine = text.length() > 0 && !text.endsWith("\n");
	}

	private boolean errorsBeingPrinted(long current) {
		return lastErrorNano != 0 && current - lastErrorNano < currentlyPrintingDeltaNano;
	}

	private boolean nonErrorBeingPrinted(long current) {
		return lastNonErrorNano != 0 && current - lastNonErrorNano < currentlyPrintingDeltaNano;
	}

	private boolean notWaitingTooLong(long current) {
		return firstErrorNano != 0 && current - firstErrorNano < maxWaitTimeNano;
	}

	private boolean consistencyCheck() {
		boolean consistent = firstErrorNano != 0 || firstErrorNano == 0 && errorOutput.isEmpty();
		return consistent;
	}

	@Override
	public void dispose() {
		console = null;
		exit = true;
	}

	public static class SleepingPolicy {
		private int sleepTimeWhenWasActive;
		private int sleepTimeWhenIdle;

		public SleepingPolicy(String sleepTimeWhenWasActive, String sleepTimeWhenIdle) {
			this.sleepTimeWhenWasActive = Utils.toPositiveInt(sleepTimeWhenWasActive, StreamBufferSettings.SLEEP_TIME_WHEN_WAS_ACTIVE);
			this.sleepTimeWhenIdle = Utils.toPositiveInt(sleepTimeWhenIdle, StreamBufferSettings.SLEEP_TIME_WHEN_IDLE);
		}

		public int getTimeToSleep(boolean wasActive) {
			return wasActive ? sleepTimeWhenWasActive : sleepTimeWhenIdle;
		}
	}
}
