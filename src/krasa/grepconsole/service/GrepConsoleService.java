package krasa.grepconsole.service;

import java.util.ArrayList;
import java.util.List;

import krasa.grepconsole.console.GrepConsoleView;
import krasa.grepconsole.decorators.ConsoleTextDecorator;
import krasa.grepconsole.decorators.DecoratorState;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.plugin.PluginState;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.util.text.StringTokenizer;

public class GrepConsoleService {
	public static final String DELIMITER = "\n";
	private Project project;
	private ConsoleView consoleView;
	private List<ConsoleTextDecorator> consoleTextDecorators;
	private Profile profile;

	public GrepConsoleService(Project project, ConsoleView consoleView) {
		this.project = project;
		this.consoleView = consoleView;
		// not yet implemented filters by project
		GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
		applicationComponent.register(this);
		PluginState state = applicationComponent.getState();
		profile = state.getProfile(consoleView);
		initDecorators();
	}

	private void initDecorators() {
		consoleTextDecorators = new ArrayList<ConsoleTextDecorator>();
		for (GrepExpressionItem grepExpressionItem : profile.getGrepExpressionItems()) {
			consoleTextDecorators.add(grepExpressionItem.createDecorator());
		}
	}

	public GrepConsoleService(List<ConsoleTextDecorator> consoleTextDecorators) {
		this.consoleTextDecorators = consoleTextDecorators;
	}

	public void process(String text, ConsoleViewContentType contentType, GrepConsoleView grepConsoleView) {
		StringTokenizer stringTokenizer = new StringTokenizer(text, DELIMITER, true);
		while (stringTokenizer.hasMoreTokens()) {
			String line = stringTokenizer.nextToken();
			if (DELIMITER.equals(line)) { // optimalization
				grepConsoleView.printProcessedResult(line, contentType);
			} else {
				processLine(line, contentType, grepConsoleView);
			}
		}

	}

	private void processLine(String line, ConsoleViewContentType contentType, GrepConsoleView grepConsoleView) {
		DecoratorState state = new DecoratorState(line, contentType);
		FLOW: for (ConsoleTextDecorator consoleTextDecorator : getConsoleTextDecorators()) {
			state = consoleTextDecorator.process(state);
			switch (state.getNextOperation()) {
			case PRINT_IMMEDIATELY:
				break FLOW;
			case CONTINUE_MATCHING:
				break;
			case EXCLUDE:
				return;
			}
		}
		grepConsoleView.printProcessedResult(state.getLine(), state.getContentType());
	}

	private List<ConsoleTextDecorator> getConsoleTextDecorators() {
		return consoleTextDecorators;
	}

	public void onChange() {
		profile = getActualProfile();
		initDecorators();
	}

	private Profile getActualProfile() {
		GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
		return applicationComponent.getState().getProfile(profile);
	}

	public void dispose() {
		GrepConsoleApplicationComponent.getInstance().remove(this);
	}

	public ConsoleView getConsoleView() {
		return consoleView;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
		initDecorators();
	}
}
