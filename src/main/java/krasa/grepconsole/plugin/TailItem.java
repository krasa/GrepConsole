package krasa.grepconsole.plugin;

import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import java.util.Objects;

public class TailItem {
	@SystemIndependent
	private String path;
	private boolean newestMatching = false;
	private boolean autodetectEncoding = false;
	private String encoding = null;

	public TailItem() {
	}

	public TailItem(String path, boolean newestMatching) {
		this.path = FileUtil.toSystemIndependentName(path);
		this.newestMatching = newestMatching;
	}

	public TailItem(@NotNull String path) {
		this(path, false);
	}

	public boolean isNewestMatching() {
		return newestMatching;
	}

	public void setNewestMatching(boolean newestMatching) {
		this.newestMatching = newestMatching;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isAutodetectEncoding() {
		return autodetectEncoding;
	}

	public void setAutodetectEncoding(boolean autodetectEncoding) {
		this.autodetectEncoding = autodetectEncoding;
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TailItem tailItem = (TailItem) o;
		return newestMatching == tailItem.newestMatching && Objects.equals(path, tailItem.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path, newestMatching);
	}

	@Override
	public String toString() {
		return "TailItem{" +
				"path='" + path + '\'' +
				", newestMatching=" + newestMatching +
				'}';
	}
}
