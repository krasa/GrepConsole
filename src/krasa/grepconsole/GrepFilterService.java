package krasa.grepconsole;

import java.util.ArrayList;
import java.util.List;

import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

import org.apache.commons.lang.StringUtils;
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
		initFilters();
	}

	public GrepFilterService(Profile profile, List<GrepFilter> grepFilters) {
		this.profile = profile;
		this.grepFilters = grepFilters;
	}

	@Nullable
	@Override
	public Result applyFilter(String line, int entireLength) {
		Result result = null;
		// line can be empty sometimes under heavy load
		if (profile.isEnabled() && !StringUtils.isEmpty(line)) {
			FilterState state = new FilterState(getSubstring(line));
			FLOW: for (GrepFilter grepFilter : getGrepFilters()) {
				state = grepFilter.process(state);
				switch (state.getNextOperation()) {
				case PRINT_IMMEDIATELY:
					break FLOW;
				case CONTINUE_MATCHING:
					break;
				}
			}
			result = prepareResult(line, entireLength, state);
		}
		return result;
	}

	private Result prepareResult(String line, int entireLength, FilterState state) {
		Result result = null;
		TextAttributes textAttributes = state.getTextAttributes();
		if (textAttributes != null) {
			result = new Result(entireLength - line.length(), entireLength, null, textAttributes);
		}
		return result;
	}

	private String getSubstring(String line) {
		// todo i wonder if \n is used on all platforms
		int endIndex = line.length() - 1;
		if (profile.isEnableMaxLengthLimit()) {
			endIndex = Math.min(endIndex, profile.getMaxLengthToMatch());
		}
		return line.substring(0, endIndex);
	}

	private void initFilters() {
		grepFilters = new ArrayList<GrepFilter>();
		for (GrepExpressionItem grepExpressionItem : profile.getGrepExpressionItems()) {
			grepFilters.add(grepExpressionItem.createFilter());
		}
	}

	private List<GrepFilter> getGrepFilters() {
		return grepFilters;
	}

	public void onChange() {
		profile = refreshProfile();
		initFilters();
	}

	private Profile refreshProfile() {
		GrepConsoleApplicationComponent applicationComponent = GrepConsoleApplicationComponent.getInstance();
		return applicationComponent.getState().getProfile(profile);
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
		initFilters();
	}
}
