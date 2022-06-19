package krasa.grepconsole.model;

import com.intellij.openapi.diagnostic.Logger;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class PlayerUtil {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(PlayerUtil.class);

	public static void play(String path, Sound sound) throws Exception {
		new Thread(() -> {
			try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(path));
				 Clip clip = AudioSystem.getClip();
			) {
				clip.open(audioInputStream);
				clip.addLineListener(new LineListener() {
					@Override
					public void update(LineEvent event) {
						LineEvent.Type type = event.getType();
						if (type == LineEvent.Type.STOP) {
							sound.playing = false;

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
				sound.playing = false;
				LOG.warn(e);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}, "GrepConsole player").start();
	}
}
