package krasa.grepconsole;

import java.util.ArrayList;
import java.util.List;

import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

import org.jetbrains.annotations.Nullable;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;

public class GrepFilter implements Filter {

	protected Project project;
	private Profile profile;
	private List<ConsoleTextDecorator> consoleTextDecorators;

	public GrepFilter(Project project) {
		this.project = project;
		profile = GrepConsoleApplicationComponent.getInstance().getProfile(project);
		initDecorators();
	}

	public GrepFilter(Profile profile, List<ConsoleTextDecorator> consoleTextDecorators) {
		this.profile = profile;
		this.consoleTextDecorators = consoleTextDecorators;
	}

	@Nullable
	@Override
	public Result applyFilter(String line, int entireLength) {
		Result result = null;
		if (profile.isEnabled()) {
			DecoratorState state = new DecoratorState(line.substring(0, line.length() - 1));
			FLOW: for (ConsoleTextDecorator consoleTextDecorator : getConsoleTextDecorators()) {
				state = consoleTextDecorator.process(state);
				switch (state.getNextOperation()) {
				case PRINT_IMMEDIATELY:
					break FLOW;
				case CONTINUE_MATCHING:
					break;
				}
			}
			TextAttributes textAttributes = state.getTextAttributes();
			if (textAttributes != null) {
				result = new Result(entireLength - line.length(), entireLength, null, textAttributes);
			}
		}
		return result;
	}

	private void initDecorators() {
		consoleTextDecorators = new ArrayList<ConsoleTextDecorator>();
		for (GrepExpressionItem grepExpressionItem : profile.getGrepExpressionItems()) {
			consoleTextDecorators.add(grepExpressionItem.createDecorator());
		}
	}

	private List<ConsoleTextDecorator> getConsoleTextDecorators() {
		return consoleTextDecorators;
	}

	public void onChange() {
		profile = refreshProfile();
		initDecorators();
	}

	private Profile refreshProfile() {
		GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
		return applicationComponent.getState().getProfile(profile);
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
		initDecorators();
	}
}
