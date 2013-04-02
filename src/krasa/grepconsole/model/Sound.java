package krasa.grepconsole.model;

import java.io.File;
import java.net.URL;

import javax.media.Manager;
import javax.media.Player;

import com.intellij.openapi.diagnostic.Logger;

/**
 * @author Vojtech Krasa
 */
public class Sound extends DomainObject {
	private static final Logger log = Logger.getInstance(Sound.class.getName());

	private String path;
	private boolean enabled;

	public String getPath() {
		return path;
	}

	public void play() {
		if (enabled) {
			try {
				Player player = Manager.createPlayer(new URL("file:" + path));
				player.start();
			} catch (Exception ex) {
				log.error("Playing of sound failed. Sound file path=" + path + ", exists:" + new File(path).exists()
						+ ".", ex);
			}
		}
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

}
