package krasa.grepconsole.tail.runConfiguration;

import java.util.ArrayList;
import java.util.List;

public class TailRunConfigurationSettings {
	private boolean autodetectEncoding = false;
	private String encoding = "UTF-8";
	private List<String> paths = new ArrayList<>();

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

	public List<String> getPaths() {
		return paths;
	}

	public void setPaths(List<String> paths) {
		this.paths = paths;
	}
}
