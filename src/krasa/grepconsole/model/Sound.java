package krasa.grepconsole.model;

import java.io.File;
import java.io.IOException;

import javax.media.*;

import krasa.grepconsole.filter.support.SoundMode;

import com.intellij.openapi.diagnostic.Logger;

/**
 * @author Vojtech Krasa
 */
public class Sound extends DomainObject implements ControllerListener {
	private static final Logger log = Logger.getInstance(Sound.class.getName());

	public static SoundMode soundMode = SoundMode.ENABLED;

	private String path;

	private boolean enabled;

	private volatile transient boolean playing;

	public String getPath() {
		return path;
	}

	public synchronized void play() {
		if (enabled && soundMode == SoundMode.ENABLED && !playing && isNotBlank(path)) {
			try {
				playing = true;
				getPlayer().start();
			} catch (Exception ex) {
				playing = false;
				log.error("Playing of sound failed. Sound file path=" + path + ", exists:" + new File(path).exists()
						+ ".", ex);
			}
		}
	}

	private boolean isNotBlank(String path) {
		return path != null && path.length() != 0;
	}

	private Player getPlayer() throws IOException, NoPlayerException {
		Player player = Manager.createPlayer(new File(path).toURI().toURL());
		player.addControllerListener(this);
		return player;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public void controllerUpdate(ControllerEvent controllerEvent) {
		if (controllerEvent instanceof StopEvent) {
			playing = false;
		}
	}
}
