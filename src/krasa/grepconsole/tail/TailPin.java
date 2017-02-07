package krasa.grepconsole.tail;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import krasa.grepconsole.action.OpenFileInConsoleAction;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Transient;

@State(name = "GrepConsoleTailPin", storages = { @Storage(value = "GrepConsoleTailPin.xml") })
public class TailPin implements PersistentStateComponent<TailPin> {
	@Transient
	private transient Project project;
	@NotNull
	private Set<String> pinnedFiles = new LinkedHashSet<String>();

	public TailPin() {
	}

	public TailPin(Project project) {
		this.project = project;
	}

	@Override
	@NotNull
	public TailPin getState() {
		return this;
	}

	@Override
	public void loadState(@NotNull TailPin state) {
		pinnedFiles = state.pinnedFiles;
	}

	public void openOldPins() {
		for (String pinnedFile : pinnedFiles.toArray(new String[pinnedFiles.size()])) {
			File file = new File(pinnedFile);
			if (file.exists()) {
				new OpenFileInConsoleAction().openFileInConsole(project, file);
			} else {
				pinnedFiles.remove(pinnedFile);
			}
		}
	}

	@NotNull
	public Set<String> getPinnedFiles() {
		return pinnedFiles;
	}

	public void setPinnedFiles(@NotNull Set<String> pinnedFiles) {
		this.pinnedFiles = pinnedFiles;
	}

	public void addPinned(@NotNull File pinnedFile) {
		this.pinnedFiles.add(pinnedFile.getAbsolutePath());
	}

	public void removePinned(File file) {
		this.pinnedFiles.remove(file.getAbsolutePath());
	}

	public boolean isPinned(File file) {
		return pinnedFiles.contains(file.getAbsolutePath());
	}
}
