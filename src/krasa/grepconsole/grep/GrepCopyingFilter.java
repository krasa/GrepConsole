package krasa.grepconsole.grep;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import krasa.grepconsole.filter.AbstractFilter;
import krasa.grepconsole.grep.listener.GrepCopyingFilterListener;

import com.intellij.execution.filters.TextInputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;

public class GrepCopyingFilter extends AbstractFilter implements TextInputFilter {

	private List<GrepCopyingFilterListener> copyingListeners = new CopyOnWriteArrayList<GrepCopyingFilterListener>();

	public GrepCopyingFilter(Project project) {
		super(project);
	}

	@Override
	public String applyFilter(String s,
			ConsoleViewContentType consoleViewContentType) {
		for (GrepCopyingFilterListener copyingListener : copyingListeners) {
			copyingListener.process(s, consoleViewContentType);
		}
		return s;
	}

	public void addListener(GrepCopyingFilterListener copyingListener) {
		copyingListeners.add(copyingListener);
	}

	public void removeListener(GrepCopyingFilterListener copyingListener) {
		copyingListeners.remove(copyingListener);
	}

}
