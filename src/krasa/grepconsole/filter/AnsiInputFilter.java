package krasa.grepconsole.filter;

import java.util.ArrayList;
import java.util.List;

import krasa.grepconsole.ansi.AnsiConsoleStyleProcessor;
import krasa.grepconsole.filter.support.ConsoleListener;
import krasa.grepconsole.model.Profile;

import org.apache.commons.net.util.Base64;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;

public class AnsiInputFilter extends AbstractFilter implements InputFilter, ConsoleListener {
	protected AnsiConsoleStyleProcessor ansiConsoleStyleProcessor;
	private ConsoleView console;

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
		if (profile.isEncodeText()) {
			if (list == null) {
				list = new ArrayList<Pair<String, ConsoleViewContentType>>(2);
			}
			if (list.isEmpty()) {
				list.add(new Pair<String, ConsoleViewContentType>(s, consoleViewContentType));
			}
			StringBuilder stringBuilder = new StringBuilder();
			for (Pair<String, ConsoleViewContentType> stringConsoleViewContentTypePair : list) {
				stringBuilder.append(stringConsoleViewContentTypePair.first);
			}
			list.add(new Pair<String, ConsoleViewContentType>(
					"input:" + Base64.encodeBase64URLSafeString(s.getBytes()), consoleViewContentType));
			list.add(new Pair<String, ConsoleViewContentType>("\nresult:"
					+ Base64.encodeBase64URLSafeString(stringBuilder.toString().getBytes()) + "\n",
					consoleViewContentType));
		}

		if (list == null || list.isEmpty()) {
			return null;
		}
		return list;
	}

	public void setProfile(Profile profile) {
		ansiConsoleStyleProcessor.setProfile(profile);
	}

	public void onChange() {
		super.onChange();

		ansiConsoleStyleProcessor.setProfile(profile);
	}

	public void setConsole(ConsoleView console) {
		this.console = console;
	}

	@Override
	public void clearConsole() {
		console.clear();
	}
}
