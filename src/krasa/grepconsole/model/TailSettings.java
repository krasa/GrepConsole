package krasa.grepconsole.model;

/**
 * @author Vojtech Krasa
 */
public class TailSettings extends DomainObject {

	private boolean enabled;
	private String port = String.valueOf(8093);

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public int getPortAsInt() {
		return Integer.parseInt(port);
	}
}
