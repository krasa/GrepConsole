package krasa.grepconsole.filter;

import java.util.Arrays;
import java.util.List;

import krasa.grepconsole.grep.FilterState;
import krasa.grepconsole.grep.GrepProcessor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;

public class GrepInputFilter extends AbstractGrepFilter implements InputFilter {

	public GrepInputFilter(Project project) {
		super(project);
	}

	public GrepInputFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile, grepProcessors);
	}

	@Override
	public List<Pair<String, ConsoleViewContentType>> applyFilter(String s,
			ConsoleViewContentType consoleViewContentType) {
		FilterState state = super.filter(s);
		return prepareResult(state);
	}

	private List<Pair<String, ConsoleViewContentType>> prepareResult(FilterState state) {
		Pair<String, ConsoleViewContentType> result = null;
		if (state != null) {
			if (state.isExclude()) {
				result = new Pair<String, ConsoleViewContentType>(null, null);
			}
		}
		if (result == null) {
			return null;// input is not changed
		} else {
			return Arrays.asList(result);
		}
	}

	@Override
	protected boolean shouldAdd(GrepExpressionItem grepExpressionItem) {
		return profile.isEnabledInputFiltering() && grepExpressionItem.isInputFilter();
	}

}
