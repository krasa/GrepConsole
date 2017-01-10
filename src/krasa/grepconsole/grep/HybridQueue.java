package krasa.grepconsole.grep;

import com.google.common.math.IntMath;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.io.FileUtil;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.squareup.tape.QueueFile;
import krasa.grepconsole.grep.listener.EventConsumer;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class HybridQueue implements Disposable {
	private static final Logger log = LoggerFactory.getLogger(HybridQueue.class);
	private LongEventProducerWithTranslator bufferProducer;
	private Disruptor<LogEvent> disruptor;
	private FileBackingQueue fileBackingQueue;
	private State state;

	public HybridQueue(EventConsumer eventConsumer) {
		// The factory for the event
		LongEventFactory factory = new LongEventFactory();

		// Specify the size of the ring buffer, must be power of 2.
		int bufferSize = IntMath.pow(2, 16); //2^14 16k  , ^16=65536

		// Construct the Disruptor
		state = new State();
		fileBackingQueue = new FileBackingQueue(state);
		// Connect the handler
		ThreadFactory grepConsole = new ThreadFactory() {
			@Override
			public Thread newThread(@NotNull Runnable r) {
				Thread thread = new Thread(r, "GrepConsole");
				thread.setDaemon(true);
				return thread;
			}
		};
		this.disruptor = new Disruptor<LogEvent>(factory, bufferSize, grepConsole, ProducerType.MULTI, new BlockingWaitStrategy());
		this.disruptor.handleEventsWith(new LogEventHandler(this.disruptor.getRingBuffer(), fileBackingQueue, state, eventConsumer));

		// Start the Disruptor, starts all threads running
		this.disruptor.start();

		// Get the ring buffer from the Disruptor to be used for publishing.
		RingBuffer<LogEvent> ringBuffer = this.disruptor.getRingBuffer();
		bufferProducer = new LongEventProducerWithTranslator(fileBackingQueue, ringBuffer,
				state);
	}

	public void onData(String s, ConsoleViewContentType type) {
		bufferProducer.onData(s);
	}

	@Override
	public void dispose() {
		try {
			try {
				disruptor.shutdown(0, TimeUnit.NANOSECONDS);
				// if shutdown is successful:
				// 1. exception is not thrown (obviously)
				// Disruptor.halt() is called automatically (less obvious)
			} catch (TimeoutException e) {
				disruptor.halt();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		fileBackingQueue.dispose();
	}

	public void clearStats() {
		state.clear();
	}

	public static class LongEventProducerWithTranslator {
		private final FileBackingQueue fileBackingQueue;
		private final RingBuffer<LogEvent> ringBuffer;
		private final State state;

		public LongEventProducerWithTranslator(FileBackingQueue fileBackingQueue, RingBuffer<LogEvent> ringBuffer,
											   State state) {
			this.fileBackingQueue = fileBackingQueue;
			this.ringBuffer = ringBuffer;
			this.state = state;
		}

		public void onData(String bb) {
			state.producerDelay();
			state.itemsProduced.incrementAndGet();
			add(bb, 0);
		}

		protected void add(String bb, int tries) {
			if (tries > 1) {
				throw new IllegalStateException(
						"unable to add to queue. " + state + "; " + ringBuffer + "; " + fileBackingQueue);
			}
			boolean added;
			if (!state.isFileBacking()) {
//				System.out.println("added to ringBuffer " + bb);

				boolean fileBackingEnabled = false;
				if (fileBackingEnabled) {
					added = ringBuffer.tryPublishEvent(TRANSLATOR, bb, null, null);
				} else {
					ringBuffer.publishEvent(TRANSLATOR, bb, null, null);
					added = true;
				} 

				if (!added) {
//					System.out.println("ringBuffer full, queing to file");
					fileBackingQueue.activateFileBacking();
					added = fileBackingQueue.tryAdd(bb);
				}
			} else {
				if (!fileBackingQueue.isQueuePublished()) {
					fileBackingQueue.tryPublishFileQueue(ringBuffer);
				}
				added = fileBackingQueue.tryAdd(bb);
			}
			if (!added) {
//				System.out.println("item not added, race condition, trying to add again  " + state);
				add(bb, ++tries);
			}
		}
	}

	public static class LogEventHandler implements EventHandler<LogEvent> {

		private final RingBuffer<LogEvent> ringBuffer;
		private final FileBackingQueue fileBackingQueue;
		private final State state;
		private final EventConsumer eventConsumer;

		public LogEventHandler(RingBuffer<LogEvent> ringBuffer, FileBackingQueue fileBackingQueue, State state, EventConsumer eventConsumer) {
			this.ringBuffer = ringBuffer;
			this.fileBackingQueue = fileBackingQueue;
			this.state = state;
			this.eventConsumer = eventConsumer;
		}

		public void onEvent(LogEvent event, long sequence, boolean endOfBatch) {
			try {
				state.consumerDelay();

				if (event.getFileBuffer() == null) {
					processEvent(event.get());
					event.set((ConsoleViewContentType) null);
					event.set((QueueFile) null);
					event.set((ConsoleViewContentType) null);
					
					if (!fileBackingQueue.isQueuePublished()) {
						fileBackingQueue.tryPublishFileQueue(ringBuffer);
					}
				} else {
					fileBackingQueue.read(this, event.getFileBuffer());
				}


			} catch (Throwable e) {
				e.printStackTrace();
				log.error(e.getMessage(), e);
			}
		}

		private void processEvent(String s) {
			eventConsumer.processEvent(s);

			long l = state.itemsConsumed.incrementAndGet();
//			if (l != Long.valueOf(s)) {
//				throw new RuntimeException(l + "!=" + s);
//			}

		}


	}

	public static class FileBackingQueue {
		private final State state;
		private QueueFile lastFileQueue;
		private boolean queuePublished = true;
		private File lastFile;

		public FileBackingQueue(State state) {
			this.state = state;
		}

		public synchronized QueueFile activateFileBacking() {
			if (state.fileBacking.get()) {
//				System.out.println("FileBacking already activated - race condition");
				return lastFileQueue;
			}
			System.out.println("activateFileBacking " + state);
			state.fileBacking.set(true);
			try {
				lastFile = FileUtil.generateRandomTemporaryPath();
				lastFileQueue = new QueueFile(lastFile);
				queuePublished = false;
				return lastFileQueue;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private synchronized void deactivateFileBacking() {
//			System.out.println("deactivateFileBacking");
			if (!queuePublished) {
				throw new IllegalStateException("fileQueue not published, therefore processed, yet it is being disabled, fuck");
			}
			state.fileBacking.set(false);
		}

		public synchronized boolean isQueuePublished() {
			return queuePublished;
		}

		public synchronized void tryPublishFileQueue(RingBuffer<LogEvent> ringBuffer) {
			if (queuePublished) {
//				System.out.println("queue already published, race condition");
				return;
			}
			if (lastFileQueue == null) {
				throw new IllegalStateException("publishing null lastFileQueue, fuck");
			}
			if (ringBuffer.tryPublishEvent(TRANSLATOR, null, null, lastFileQueue)) {
//				System.out.println("FileQueue published to ringBuffer");
				queuePublished = true;
			} else {
//				System.out.println("FileQueue not published - ringBuffer full - will try on the next event");
			}
		}

		public synchronized boolean tryAdd(String s) {
			if (!state.isFileBacking()) {
//				System.out.println("fileBacking is deactivated - race condition");
				return false;
			}

//			System.out.println("adding to fileQueue " + s);
			try {
				lastFileQueue.add(s.getBytes());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return true;
		}

		public void read(final LogEventHandler logEventConsumer, QueueFile fileBuffer) {
			try {
				while (readAll(fileBuffer, logEventConsumer) > 0) {
				}
				deactivateFileBacking();
				readAll(fileBuffer, logEventConsumer);
				fileBuffer.close();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		protected int readAll(QueueFile fileBuffer, final LogEventHandler logEventConsumer) throws IOException {
			long start = System.currentTimeMillis();
			int i = 0;
			while (true) {
				byte[] bytes = fileBuffer.peek();
				if (bytes == null) {
					break;
				}
				i++;
				fileBuffer.remove();
				logEventConsumer.processEvent(new String(bytes));
			}
//			System.out.println("readAll " + i + " " + (System.currentTimeMillis() - start));
			return i;

		}


		public void dispose() {
			QueueFile lastFileQueue = this.lastFileQueue;
			if (lastFileQueue != null) {
				try {
					lastFileQueue.close();
					lastFile.delete();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static final EventTranslatorThreeArg<LogEvent, String, ConsoleViewContentType, QueueFile> TRANSLATOR = new EventTranslatorThreeArg<LogEvent, String, ConsoleViewContentType, QueueFile>() {
		@Override
		public void translateTo(LogEvent event, long sequence, String arg0, ConsoleViewContentType arg1,
								QueueFile arg2) {
			event.set(arg0);
			event.set(arg1);
			event.set(arg2);
		}
	};

	public static class LongEventFactory implements EventFactory<LogEvent> {

		public LogEvent newInstance() {
			return new LogEvent();
		}
	}

	public static class LogEvent {
		private String value;
		private ConsoleViewContentType type;
		private QueueFile fileBuffer;

		public String get() {
			return value;
		}

		public void set(String value) {
			this.value = value;
		}

		public void set(ConsoleViewContentType arg1) {
			type = arg1;
		}

		public void set(QueueFile arg2) {
			fileBuffer = arg2;
		}

		public String getValue() {
			return value;
		}

		public ConsoleViewContentType getType() {
			return type;
		}

		public QueueFile getFileBuffer() {
			return fileBuffer;
		}
	}

	public static class State {
		AtomicBoolean fileBacking = new AtomicBoolean();
		AtomicLong itemsConsumed = new AtomicLong();
		AtomicLong itemsProduced = new AtomicLong();

		public boolean isFileBacking() {
			return fileBacking.get();
		}


		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("fileBacking", fileBacking.get())
					.append("itemsConsumed", itemsConsumed.get())
					.append("itemsProduced", itemsProduced.get())
					.toString();
		}

		protected void producerDelay() {
		}

		protected void consumerDelay() {
		}

		public void clear() {
			itemsConsumed.set(0);
			itemsProduced.set(0);
		}
	}
}
