package krasa.grepconsole.service;

import java.util.List;

import krasa.grepconsole.FilterState;
import krasa.grepconsole.GrepFilter;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

import org.apache.commons.lang.StringUtils;

import com.intellij.openapi.project.Project;

public abstract class AbstractGrepFilterService<T> {

	protected Project project;
	protected Profile profile;
	protected List<GrepFilter> grepFilters;

	public AbstractGrepFilterService(Project project) {
		this.project = project;
		profile = GrepConsoleApplicationComponent.getInstance().getProfile(project);
		initFilters();
	}

	public AbstractGrepFilterService(Profile profile, List<GrepFilter> grepFilters) {
		this.profile = profile;
		this.grepFilters = grepFilters;
	}

	public FilterState filter(String text) {
		// line can be empty sometimes under heavy load
		if (!StringUtils.isEmpty(text)) {
			FilterState state = null;
			state = new FilterState(getSubstring(text));
			for (GrepFilter grepFilter : getGrepFilters()) {
				state = grepFilter.process(state);
				switch (state.getNextOperation()) {
				case PRINT_IMMEDIATELY:
					return state;
				case CONTINUE_MATCHING:
					break;
				}
			}
		}
		return null;
	}

	// for higlighting, it always ends with \n, but for input filtering it does not
	private String getSubstring(String text) {
		int endIndex = text.length();
		if (text.endsWith("\n")) {
			--endIndex;
		}
		if (profile.isEnableMaxLengthLimit()) {
			endIndex = Math.min(endIndex, profile.getMaxLengthToMatch());
		}
		return text.substring(0, endIndex);
	}

	protected abstract void initFilters();

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
