package krasa.grepconsole.service;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import krasa.grepconsole.filter.FilterState;
import krasa.grepconsole.filter.GrepFilter;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;

import java.util.Arrays;
import java.util.List;

public class GrepInputFilterService extends AbstractGrepService implements InputFilter {

	public GrepInputFilterService(Project project) {
		super(project);
	}

	public GrepInputFilterService(Profile profile, List<GrepFilter> grepFilters) {
		super(profile, grepFilters);
	}

	@Override
	public List<Pair<String, ConsoleViewContentType>> applyFilter(String s,
																  ConsoleViewContentType consoleViewContentType) {
		Pair<String, ConsoleViewContentType> result = null;
		FilterState state = super.filter(s);
		if (state != null) {
			result = prepareResult(state);
		}
		if (result == null) {
			return null;//input is not changed
		} else {
			return Arrays.asList(result);
		}
	}

	private Pair<String, ConsoleViewContentType> prepareResult(FilterState state) {
		Pair<String, ConsoleViewContentType> result = null;
		if (state.isExclude()) {
			result = new Pair<String, ConsoleViewContentType>(null, null);
		}
		return result;
	}

	@Override
	protected boolean shouldAdd(GrepExpressionItem grepExpressionItem) {
		return profile.isEnabledInputFiltering() && grepExpressionItem.isInputFilter();
	}

}
