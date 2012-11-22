package krasa.grepconsole.service;

import java.util.ArrayList;
import java.util.List;

import krasa.grepconsole.FilterState;
import krasa.grepconsole.GrepFilter;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;

public class GrepInputFilterService extends AbstractGrepFilterService implements InputFilter {

	public GrepInputFilterService(Project project) {
		super(project);
	}

	public GrepInputFilterService(Profile profile, List<GrepFilter> grepFilters) {
		super(profile, grepFilters);
	}

	@Override
	protected void initFilters() {
		grepFilters = new ArrayList<GrepFilter>();
		for (GrepExpressionItem grepExpressionItem : profile.getGrepExpressionItems()) {
			if (grepExpressionItem.isFilterOut()) {
				grepFilters.add(grepExpressionItem.createFilter());
			}
		}
	}

	@Override
	public Result applyFilter(String s, ConsoleViewContentType consoleViewContentType) {
		Result result = null;
		if (profile.isEnabledFiltering()) {
			FilterState state = super.filter(s);
			if (state != null) {
				result = prepareResult(s, state);
			}
		}
		return result;
	}

	private Result prepareResult(String s, FilterState state) {
		Result result = null;
		if (state != null) {
			result = new Result();
		}
		return result;
	}

}
