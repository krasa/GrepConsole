package krasa.grepconsole.plugin;

import com.intellij.openapi.project.Project;
import krasa.grepconsole.action.TailFileInConsoleAction;
import krasa.grepconsole.grep.PinnedGrepConsolesState;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

public class GrepProjectState {
	@NotNull
	private Set<String> pinnedTailFiles = new LinkedHashSet<>();
	@NotNull
	private PinnedGrepConsolesState pinnedGrepConsolesState = new PinnedGrepConsolesState();

	public void openOldPins(@NotNull Project project) {
		for (String pinnedFile : pinnedTailFiles.toArray(new String[pinnedTailFiles.size()])) {
			File file = new File(pinnedFile);
			if (file.exists()) {
				new TailFileInConsoleAction().openFileInConsole(project, file, TailFileInConsoleAction.resolveEncoding(file));
			} else {
				pinnedTailFiles.remove(pinnedFile);
			}
		}
	}

	@NotNull
	public PinnedGrepConsolesState getPinnedGrepConsolesState() {
		return pinnedGrepConsolesState;
	}

	public void setPinnedGrepConsolesState(@NotNull PinnedGrepConsolesState pinnedGrepConsolesState) {
		this.pinnedGrepConsolesState = pinnedGrepConsolesState;
	}

	@NotNull
	public Set<String> getPinnedTailFiles() {
		return pinnedTailFiles;
	}

	public void setPinnedTailFiles(@NotNull Set<String> pinnedTailFiles) {
		this.pinnedTailFiles = pinnedTailFiles;
	}

	public void addPinned(@NotNull File pinnedFile) {
		this.pinnedTailFiles.add(pinnedFile.getAbsolutePath());
	}

	public void removePinned(File file) {
		this.pinnedTailFiles.remove(file.getAbsolutePath());
	}


	public boolean isPinned(File file) {
		return pinnedTailFiles.contains(file.getAbsolutePath());
	}
}
