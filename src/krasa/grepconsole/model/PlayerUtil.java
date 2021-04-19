package krasa.grepconsole.model;

import javax.media.Manager;
import javax.media.Player;
import javax.media.StopEvent;
import java.io.File;

public class PlayerUtil {
	public static void play(String path, Sound sound) throws Exception {
		Player player = Manager.createPlayer(new File(path).toURI().toURL());
		player.addControllerListener(controllerEvent -> {
			if (controllerEvent instanceof StopEvent) {
				sound.playing = false;
			}
		});
		player.start();
	}
}
