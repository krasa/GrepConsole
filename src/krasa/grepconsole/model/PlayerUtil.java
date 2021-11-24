package krasa.grepconsole.model;

import com.intellij.openapi.diagnostic.Logger;

import javax.media.Manager;
import javax.media.Player;
import javax.media.StopEvent;
import java.io.File;

public class PlayerUtil {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(PlayerUtil.class);

	public static void play(String path, Sound sound) throws Exception {
		Player player = Manager.createPlayer(new File(path).toURI().toURL());
		player.addControllerListener(controllerEvent -> {
			if (controllerEvent instanceof StopEvent) {
				sound.playing = false;
				try {
					player.close();
					player.deallocate();
				} catch (Exception e) {
					LOG.error(e);
				}
			}
		});
		player.start();
	}
}
