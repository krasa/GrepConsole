package krasa.grepconsole.grep;

import com.google.common.math.IntMath;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import krasa.grepconsole.grep.listener.EventConsumer;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

import java.util.concurrent.ThreadFactory;

public class HybridQueueTest {
	public static void main(String[] args) throws Exception {

		// The factory for the event
		HybridQueue.LongEventFactory factory = new HybridQueue.LongEventFactory();

		// Specify the size of the ring buffer, must be power of 2.
		int bufferSize = IntMath.pow(2, 3); //2^14 16k

		// Construct the Disruptor
		Disruptor<HybridQueue.LogEvent> disruptor = new Disruptor<HybridQueue.LogEvent>(factory, bufferSize, new ThreadFactory() {
			@Override
			public Thread newThread(@NotNull Runnable r) {
				Thread thread = new Thread(r, "GrepConsole");
				thread.setDaemon(true);
				return thread;
			}
		}, ProducerType.MULTI, new BlockingWaitStrategy());
		HybridQueue.State state = new HybridQueue.State() {

			protected void producerDelay() {
				if (isFileBacking()) {
					try {
						Thread.sleep(2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

			protected void consumerDelay() {
				try {
					if (!isFileBacking()) {
						Thread.sleep(2);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		HybridQueue.FileBackingQueue fileBackingQueue = new HybridQueue.FileBackingQueue(state);
		// Connect the handler
		disruptor.handleEventsWith(new HybridQueue.LogEventHandler(disruptor.getRingBuffer(), fileBackingQueue, state, new EventConsumer() {
			@Override
			public void processEvent(String s) {
				System.err.println(s);
			}
		}));

		// Start the Disruptor, starts all threads running
		disruptor.start();

		// Get the ring buffer from the Disruptor to be used for publishing.
		RingBuffer<HybridQueue.LogEvent> ringBuffer = disruptor.getRingBuffer();
		HybridQueue.LongEventProducerWithTranslator bufferProducer = new HybridQueue.LongEventProducerWithTranslator(fileBackingQueue, ringBuffer,
				state);

		int iterations = 0;
		do {
			int i = 0;
			while (i < 10000) {
				for (long l = 0; l < 10; l++) {
					if (i % 1 == 0) {
						Thread.sleep(1);
					}
					bufferProducer.onData(++i + "");
				}

			}
			Thread.sleep(3000);
			Assert.assertEquals(state.itemsProduced.get(), state.itemsConsumed.get());
		} while (++iterations < 5);
		//		System.err.println("itemsConsumed " + state.itemsConsumed.get());

		disruptor.shutdown();
	}
}