package krasa.grepconsole.model;

import krasa.grepconsole.tail.runConfiguration.TailRunConfigurationSettings;

/**
 * @author Vojtech Krasa
 */
public class TailSettings extends DomainObject {

	private boolean enabled;
	private String port = String.valueOf(8093);
	private String defaultEncoding = "UTF-8";
	private boolean autodetectEncoding = true;
	private boolean advancedTailDialog = true;
	private TailRunConfigurationSettings lastTail = new TailRunConfigurationSettings();

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

	public String getDefaultEncoding() {
		return defaultEncoding;
	}

	public void setDefaultEncoding(final String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
	}

	public boolean isAutodetectEncoding() {
		return autodetectEncoding;
	}

	public void setAutodetectEncoding(final boolean autodetectEncoding) {
		this.autodetectEncoding = autodetectEncoding;
	}

	public boolean isAdvancedTailDialog() {
		return advancedTailDialog;
	}

	public void setAdvancedTailDialog(final boolean advancedTailDialog) {
		this.advancedTailDialog = advancedTailDialog;
	}

	public void setLastTail(TailRunConfigurationSettings lastTail) {
		this.lastTail = lastTail;
	}

	public TailRunConfigurationSettings getLastTail() {
		return lastTail;

	}
}
