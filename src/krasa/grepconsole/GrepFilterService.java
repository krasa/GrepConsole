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

public class GrepFilterService implements Filter {

	protected Project project;
	private Profile profile;
	private List<GrepFilter> grepFilters;

	public GrepFilterService(Project project) {
		this.project = project;
		profile = GrepConsoleApplicationComponent.getInstance().getProfile(project);
		initDecorators();
	}

	public GrepFilterService(Profile profile, List<GrepFilter> grepFilters) {
		this.profile = profile;
		this.grepFilters = grepFilters;
	}

	@Nullable
	@Override
	public Result applyFilter(String line, int entireLength) {
		Result result = null;
		if (profile.isEnabled()) {
			FilterState state = new FilterState(line.substring(0, line.length() - 1));
			FLOW: for (GrepFilter grepFilter : getGrepFilters()) {
				state = grepFilter.process(state);
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
		grepFilters = new ArrayList<GrepFilter>();
		for (GrepExpressionItem grepExpressionItem : profile.getGrepExpressionItems()) {
			grepFilters.add(grepExpressionItem.createDecorator());
		}
	}

	private List<GrepFilter> getGrepFilters() {
		return grepFilters;
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
