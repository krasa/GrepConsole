package krasa.grepconsole.model;

import com.intellij.openapi.diagnostic.Logger;
import krasa.grepconsole.filter.support.SoundMode;

import java.io.File;

/**
 * @author Vojtech Krasa
 */
public class Sound extends DomainObject {
	private static final Logger log = Logger.getInstance(Sound.class);

	public static SoundMode soundMode = SoundMode.ENABLED;

	private String path;

	private boolean enabled;

	protected volatile transient boolean playing;

	public String getPath() {
		return path;
	}

	public synchronized void play() {
		if (enabled && soundMode == SoundMode.ENABLED && !playing && isNotBlank(path)) {
			try {
				playing = true;
				PlayerUtil.play(path, this);
			} catch (Throwable ex) {
				playing = false;
				log.error("Playing of sound failed. Sound file path=" + path + ", exists:" + new File(path).exists()
						+ ".", ex);
			}
		}
	}

	private boolean isNotBlank(String path) {
		return path != null && path.length() != 0;
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
