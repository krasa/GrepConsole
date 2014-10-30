package krasa.grepconsole.filter;

import java.util.*;

import com.intellij.openapi.project.*;
import krasa.grepconsole.grep.*;
import krasa.grepconsole.model.*;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
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
