package krasa.grepconsole.grep;

import com.intellij.openapi.util.io.FileUtil;
import com.squareup.tape.QueueFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

public class Tape {

	private static final int ITEMS = 5000;

	public static void main(String[] args) throws IOException, InterruptedException {
		final QueueFile queueFile = new QueueFile(FileUtil.generateRandomTemporaryPath());
		System.out.println("adding");
		int i = 0;
		while (i < ITEMS) {
			try {
				queueFile.add((++i + "").getBytes());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("reading");

		final AtomicLong atomicLong = new AtomicLong();
		read(queueFile, atomicLong);
		read(queueFile, atomicLong);
		read(queueFile, atomicLong);
		read(queueFile, atomicLong);
		read(queueFile, atomicLong);
		read(queueFile, atomicLong);
		read(queueFile, atomicLong);
		// executorService.shutdownNow();
	}

	protected static void read(final QueueFile queueFile, final AtomicLong atomicLong) throws IOException {
		queueFile.forEach(new QueueFile.ElementReader() {
			@Override
			public void read(InputStream in, int length) throws IOException {
				byte[] bytes = new byte[length];
				in.read(bytes);
				// System.out.println(new String(bytes));
				queueFile.remove();
				atomicLong.incrementAndGet();
			}
		});
		System.out.println("total read " + atomicLong.get());
	}
}
