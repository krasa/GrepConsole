package krasa.grepconsole.filter;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import krasa.grepconsole.filter.support.FilterState;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		FilterState state = super.filter(s, -1);
		return prepareResult(state);
	}

	@Override
	protected boolean continueFiltering(FilterState state) {
		return !state.isMatchesSomething();
	}

	private List<Pair<String, ConsoleViewContentType>> prepareResult(FilterState state) {
		Pair<String, ConsoleViewContentType> result = null;
		if (state != null) {
			if (state.isExclude()) {
				result = new Pair<>(null, null);
			}
		}
		if (result == null) {
			return null;// input is not changed
		} else {
			return Arrays.asList(result);
		}
	}

	@Override
	protected void initProcessors() {
		grepProcessors = new ArrayList<>();
		if (profile.isEnabledInputFiltering()) {
			boolean inputFilterExists = false;
			for (GrepExpressionItem grepExpressionItem : profile.getAllGrepExpressionItems()) {
				grepProcessors.add(createProcessor(grepExpressionItem));
				if (grepExpressionItem.isInputFilter()) {
					inputFilterExists = true;
				}
			}
			if (!inputFilterExists) {
				grepProcessors.clear();
			}
		}
	}

	@Override
	protected boolean shouldAdd(GrepExpressionItem item) {
		throw new UnsupportedOperationException();
	}

}
