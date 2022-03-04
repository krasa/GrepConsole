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
	static int MAX_SIZE = 50;

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


	public void add(List<String> paths, boolean selectNewestMatchingFile) {
		for (String path : paths) {
			TailItem e = new TailItem(path, selectNewestMatchingFile);
			items.remove(e);
			items.add(e);
		}
		limitSize();
	}

	public void add(@Nullable VirtualFile[] choose) {
		if (choose != null) {
			for (VirtualFile virtualFile : choose) {
				TailItem e = new TailItem(virtualFile);
				items.remove(e);
				items.add(e);
			}
			limitSize();
		}
	}

	public void add2(List<File> list) {
		for (File file : list) {
			TailItem e = new TailItem(file);
			items.remove(e);
			items.add(e);
		}
		limitSize();
	}

	public void add(TailItem e) {
		items.remove(e);
		items.add(e);
	}

	public void add(File file) {
		TailItem e = new TailItem(file);
		items.remove(e);
		items.add(e);
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
