package krasa.grepconsole.tail.runConfiguration;

import krasa.grepconsole.model.DomainObject;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class TailRunConfigurationSettings extends DomainObject {
	private boolean autodetectEncoding = false;
	private String encoding = "UTF-8";
	private List<String> paths = new ArrayList<>();
	private boolean selectNewestMatchingFile;

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

	public boolean isSelectNewestMatchingFile() {
		return selectNewestMatchingFile;
	}

	public void setSelectNewestMatchingFile(final boolean selectNewestMatchingFile) {
		this.selectNewestMatchingFile = selectNewestMatchingFile;
	}

	public Charset resolveEncoding(File file) {
		return TailRunProfileState.resolveEncoding(file, this);
	}

	public TailRunConfigurationSettings clearPaths() {
		paths.clear();
		return this;
	}
}
