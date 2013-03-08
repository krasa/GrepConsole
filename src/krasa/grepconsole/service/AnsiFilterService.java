package krasa.grepconsole.service;

import java.util.List;

import krasa.grepconsole.ansi.AnsiConsoleStyleFilter;
import krasa.grepconsole.model.Profile;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;

public class AnsiFilterService extends AbstractService implements InputFilter, ConsoleListener {
	protected AnsiConsoleStyleFilter ansiConsoleStyleFilter;
	private ConsoleView console;

	public AnsiFilterService(Project project) {
		super(project);
		ansiConsoleStyleFilter = new AnsiConsoleStyleFilter(profile);
		ansiConsoleStyleFilter.addListener(this);
	}

	public AnsiFilterService(Profile profile) {
		super(profile);
		ansiConsoleStyleFilter = new AnsiConsoleStyleFilter(profile);
		ansiConsoleStyleFilter.addListener(this);
	}

	@Override
	public List<Pair<String, ConsoleViewContentType>> applyFilter(String s,
			ConsoleViewContentType consoleViewContentType) {
		if (!profile.isEnableAnsiColoring() && !profile.isHideAnsiCommands()) {
			return null;
		}

		List<Pair<String, ConsoleViewContentType>> list = ansiConsoleStyleFilter.process(s, consoleViewContentType);
		if (list == null || list.isEmpty()) {
			return null;
		}
		return list;
	}

	public void setProfile(Profile profile) {
		ansiConsoleStyleFilter.setProfile(profile);
	}

	public void onChange() {
		profile = refreshProfile();
		ansiConsoleStyleFilter.setProfile(profile);
	}

	public void setConsole(ConsoleView console) {
		this.console = console;
	}

	@Override
	public void clearConsole() {
		console.clear();
	}
}
