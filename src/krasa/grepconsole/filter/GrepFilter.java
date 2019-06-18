package krasa.grepconsole.filter;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import krasa.grepconsole.grep.listener.GrepFilterListener;
import krasa.grepconsole.model.Profile;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GrepFilter extends AbstractFilter implements InputFilter {

	private List<GrepFilterListener> grepListeners = new CopyOnWriteArrayList<>();

	public GrepFilter(Project project, Profile profile) {
		super(project, profile);
	}

	@Override
	public List<Pair<String, ConsoleViewContentType>> applyFilter(String s,
			ConsoleViewContentType consoleViewContentType) {
		for (int i = 0; i < grepListeners.size(); i++) {
			GrepFilterListener grepListener = grepListeners.get(i);
			grepListener.process(s, consoleViewContentType);
		}
		return null;
	}

	@Override
	protected void refreshProfile() {
		super.refreshProfile();

		for (GrepFilterListener grepListener : grepListeners) {
			grepListener.profileUpdated(profile);
		}
	}

	public void addListener(GrepFilterListener grepListener) {
		grepListeners.add(grepListener);
	}

	public void removeListener(GrepFilterListener grepListener) {
		grepListeners.remove(grepListener);
	}

	@Override
	public String toString() {
		return "GrepFilter{" +
				"grepListeners=" + grepListeners +
				'}';
	}
}
