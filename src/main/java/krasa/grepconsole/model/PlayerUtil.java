package krasa.grepconsole.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.ConcurrencyUtil;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PlayerUtil {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(PlayerUtil.class);

	private static final ThreadPoolExecutor ourThreadExecutorsService =
			new ThreadPoolExecutor(0, 5, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10),
					ConcurrencyUtil.newNamedThreadFactory("GrepConsole player"));

	public static void play(String path, Sound sound) throws Exception {
		ourThreadExecutorsService.submit(() -> {
			try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(path));
				 Clip clip = AudioSystem.getClip();
			) {
				clip.open(audioInputStream);
				clip.addLineListener(new LineListener() {
					@Override
					public void update(LineEvent event) {
						LineEvent.Type type = event.getType();
						if (type == LineEvent.Type.STOP) {
							synchronized (sound) {
								sound.notifyAll();
							}
						}
					}
				});
				clip.start();

				synchronized (sound) {
					sound.wait();
				}
			} catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
				LOG.warn(e);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			} finally {
				sound.playing = false;
			}
		});
	}
}
