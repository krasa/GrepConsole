package krasa.grepconsole.filter;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import krasa.grepconsole.grep.FilterState;
import krasa.grepconsole.grep.GrepProcessor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;

import java.util.*;

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
	protected void initProcessors() {
		grepProcessors = new ArrayList<GrepProcessor>();
		if (profile.isEnabledInputFiltering()) {
			boolean inputFilterExists = false;
			for (GrepExpressionItem grepExpressionItem : profile.getGrepExpressionItems()) {
				grepProcessors.add(grepExpressionItem.createProcessor());
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
