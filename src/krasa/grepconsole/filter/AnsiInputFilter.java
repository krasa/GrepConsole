package krasa.grepconsole.filter;

import java.lang.ref.WeakReference;
import java.util.List;

import krasa.grepconsole.ansi.AnsiConsoleStyleProcessor;
import krasa.grepconsole.filter.support.ConsoleListener;
import krasa.grepconsole.model.Profile;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;

public class AnsiInputFilter extends AbstractFilter implements InputFilter, ConsoleListener {
	protected AnsiConsoleStyleProcessor ansiConsoleStyleProcessor;
	private WeakReference<ConsoleView> console;

	public AnsiInputFilter(Project project) {
		super(project);
		ansiConsoleStyleProcessor = new AnsiConsoleStyleProcessor(profile);
		ansiConsoleStyleProcessor.addListener(this);
	}

	public AnsiInputFilter(Profile profile) {
		super(profile);
		ansiConsoleStyleProcessor = new AnsiConsoleStyleProcessor(profile);
		ansiConsoleStyleProcessor.addListener(this);
	}

	@Override
	public List<Pair<String, ConsoleViewContentType>> applyFilter(String s,
			ConsoleViewContentType consoleViewContentType) {
		List<Pair<String, ConsoleViewContentType>> list = null;

		if (profile.isEnableAnsiColoring() || profile.isHideAnsiCommands()) {
			list = ansiConsoleStyleProcessor.process(s, consoleViewContentType);
		}

		if (list == null || list.isEmpty()) {
			return null;
		}
		return list;
	}

	public void setProfile(Profile profile) {
		ansiConsoleStyleProcessor.setProfile(profile);
	}

	@Override
	public void onChange() {
		super.onChange();

		ansiConsoleStyleProcessor.setProfile(profile);
	}

	public void setConsole(ConsoleView console) {
		this.console = new WeakReference<ConsoleView>(console);
	}

	@Override
	public void clearConsole() {
		final ConsoleView consoleView = console.get();
		if (consoleView != null) {
			console.clear();
		}
	}

	public boolean isRegistered() {
		return console != null;
	}
}
