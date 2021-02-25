package krasa.grepconsole.tail.runConfiguration;

public class TailRunConfigurationSettings {
	private String path = "";
	private boolean autodetectEncoding = false;
	private String encoding = "UTF-8";

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isAutodetectEncoding() {
		return autodetectEncoding;
	}

	public void setAutodetectEncoding(final boolean autodetectEncoding) {
		this.autodetectEncoding = autodetectEncoding;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(final String encoding) {
		this.encoding = encoding;
	}
}
