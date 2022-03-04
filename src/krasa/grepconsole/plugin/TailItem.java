package krasa.grepconsole.plugin;

import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.Objects;

public class TailItem {
	private String path;
	private boolean newestMatching;

	public TailItem() {
	}

	public TailItem(String path, boolean newestMatching) {
		this.path = path;
		this.newestMatching = newestMatching;
	}

	public TailItem(File file) {
		this(file.getAbsolutePath(), false);
	}

	public TailItem(VirtualFile virtualFile) {
		this(virtualFile.getPath(), false);
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
}
