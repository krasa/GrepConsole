package krasa.grepconsole.filter;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import krasa.grepconsole.grep.listener.GrepCopyingFilterListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class GrepCopyingFilter extends AbstractFilter implements InputFilter {

	private List<GrepCopyingFilterListener> copyingListeners = new CopyOnWriteArrayList<>();

	public GrepCopyingFilter(Project project) {
		super(project);
	}

	@Override
	public List<Pair<String, ConsoleViewContentType>> applyFilter(String s,
			ConsoleViewContentType consoleViewContentType) {
		for (int i = 0; i < copyingListeners.size(); i++) {
			GrepCopyingFilterListener copyingListener = copyingListeners.get(i);
			copyingListener.process(s, consoleViewContentType);
		}
		return null;
	}

	@Override
	protected void refreshProfile() {
		super.refreshProfile();

		for (GrepCopyingFilterListener copyingListener : copyingListeners) {
			copyingListener.profileUpdated(profile);
		}
	}

	public void addListener(GrepCopyingFilterListener copyingListener) {
		copyingListeners.add(copyingListener);
	}

	public void removeListener(GrepCopyingFilterListener copyingListener) {
		copyingListeners.remove(copyingListener);
	}

}
