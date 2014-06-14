package krasa.grepconsole.filter;

import java.util.ArrayList;
import java.util.List;

import krasa.grepconsole.grep.FilterState;
import krasa.grepconsole.grep.GrepProcessor;
import krasa.grepconsole.model.*;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.project.Project;

public abstract class AbstractGrepFilter extends AbstractFilter {

	protected List<GrepProcessor> grepProcessors;

	public AbstractGrepFilter(Project project) {
		super(project);
		initProcessors();
	}

	public AbstractGrepFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile);
		this.grepProcessors = grepProcessors;
	}

	protected FilterState filter(@Nullable String text, int offset) {
		// line can be empty sometimes under heavy load
		if (!StringUtils.isEmpty(text) && !grepProcessors.isEmpty()) {
			FilterState state = new FilterState(getSubstring(text), offset);
			for (GrepProcessor grepProcessor : grepProcessors) {
				state = grepProcessor.process(state);
				if (!continueFiltering(state))
					return state;
			}
			return state;
		}
		return null;
	}

	protected boolean continueFiltering(FilterState state) {
		return state.getNextOperation() != Operation.EXIT;
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

	protected void initProcessors() {
		grepProcessors = new ArrayList<GrepProcessor>();
		for (GrepExpressionItem grepExpressionItem : profile.getGrepExpressionItems()) {
			if (shouldAdd(grepExpressionItem)) {
				grepProcessors.add(grepExpressionItem.createProcessor());
			}
		}
	}

	public List<GrepProcessor> getGrepProcessors() {
		return grepProcessors;
	}

	abstract protected boolean shouldAdd(GrepExpressionItem item);

	public void onChange() {
		super.onChange();
		initProcessors();
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
		initProcessors();
	}
}
