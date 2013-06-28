package krasa.grepconsole.service;

import com.intellij.openapi.project.Project;
import krasa.grepconsole.filter.FilterState;
import krasa.grepconsole.filter.GrepFilter;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGrepService extends AbstractService {

	protected List<GrepFilter> grepFilters;

	public AbstractGrepService(Project project) {
		super(project);
		initFilters();
	}

	public AbstractGrepService(Profile profile, List<GrepFilter> grepFilters) {
		super(profile);
		this.grepFilters = grepFilters;
	}

	public FilterState filter(String text) {
		// line can be empty sometimes under heavy load
		if (!StringUtils.isEmpty(text)) {
			FilterState state = null;
			state = new FilterState(getSubstring(text), mode);
			for (GrepFilter grepFilter : grepFilters) {
				state = grepFilter.process(state);
				switch (state.getNextOperation()) {
					case EXIT:
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
			endIndex = Math.min(endIndex, profile.getMaxLengthToMatchAsInt());
		}
		return text.substring(0, endIndex);
	}

	protected void initFilters() {
		grepFilters = new ArrayList<GrepFilter>();
		for (GrepExpressionItem grepExpressionItem : profile.getGrepExpressionItems()) {
			if (shouldAdd(grepExpressionItem)) {
				grepFilters.add(grepExpressionItem.createFilter());
			}
		}
	}

	abstract protected boolean shouldAdd(GrepExpressionItem item);

	public void onChange() {
		super.onChange();
		initFilters();
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
		initFilters();
	}
}
