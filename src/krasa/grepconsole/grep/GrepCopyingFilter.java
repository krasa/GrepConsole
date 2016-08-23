package krasa.grepconsole.grep;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;

import krasa.grepconsole.filter.AbstractFilter;

public class GrepCopyingFilter extends AbstractFilter implements InputFilter {

	private List<GrepCopyingListener> copyingListeners = new CopyOnWriteArrayList<GrepCopyingListener>();

	public GrepCopyingFilter(Project project) {
		super(project);
	}

	@Override
	public List<Pair<String, ConsoleViewContentType>> applyFilter(String s,
			ConsoleViewContentType consoleViewContentType) {
		for (GrepCopyingListener copyingListener : copyingListeners) {
			copyingListener.process(s, consoleViewContentType);
		}
		return null;
	}

	public void addListener(GrepCopyingListener copyingListener) {
		copyingListeners.add(copyingListener);
	}

	public void removeListener(GrepCopyingListener copyingListener) {
		copyingListeners.remove(copyingListener);
	}

}
