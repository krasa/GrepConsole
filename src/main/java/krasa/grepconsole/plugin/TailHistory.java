package krasa.grepconsole.plugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TailHistory {
	static int MAX_SIZE = 20;

	@NotNull
	private Set<TailItem> items = new LinkedHashSet<>();

	public static TailHistory getState(Project project) {
		return GrepProjectComponent.getInstance(project).getState().getTailHistory();
	}

	@NotNull
	public Set<TailItem> getItems() {
		return items;
	}

	public void setItems(@NotNull Set<TailItem> items) {
		this.items = items;
	}


	public void add(List<String> paths, boolean selectNewestMatchingFile, String encoding, boolean autodetectEncoding) {
		for (String path : paths) {
			TailItem e = new TailItem(path, selectNewestMatchingFile);
			e.setEncoding(encoding);
			e.setAutodetectEncoding(autodetectEncoding);
			add(e);
		}
		limitSize();
	}

	public void add(@Nullable VirtualFile[] choose) {
		if (choose != null) {
			for (VirtualFile virtualFile : choose) {
				add(new TailItem(virtualFile.getPath()));
			}
			limitSize();
		}
	}

	public void add2(List<File> list) {
		for (File file : list) {
			add(new TailItem(file.getAbsolutePath()));
		}
		limitSize();
	}

	public void add(TailItem e) {
		items.remove(e);
		items.add(e);
	}

	public void addAndLimitSize(File file) {
		add(new TailItem(file.getAbsolutePath()));
		limitSize();
	}

	private void limitSize() {
		if (items.size() > MAX_SIZE) {
			int removeN = items.size() - MAX_SIZE;
			Iterator<TailItem> iterator = items.iterator();
			for (int i = 0; i < removeN; i++) {
				if (iterator.hasNext()) {
					iterator.next();
					iterator.remove();
				}
			}
		}
	}


	public void removeFromHistory(TailItem s) {
		items.remove(s);
	}

}
