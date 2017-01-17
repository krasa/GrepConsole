package krasa.grepconsole.filter;

import java.util.ArrayList;
import java.util.List;

import krasa.grepconsole.filter.support.FilterState;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;

import org.jetbrains.annotations.NotNull;

import com.intellij.execution.filters.InputFilterEx;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;

public class GrepInputFilter extends AbstractGrepFilter implements InputFilterEx {

	public GrepInputFilter(Project project) {
		super(project);
	}

	public GrepInputFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile, grepProcessors);
	}

	@Override
	public String applyFilter(@NotNull String s, @NotNull ConsoleViewContentType consoleViewContentType) {
		FilterState state = super.filter(s, -1);
		if (state != null && state.isExclude()) {
			return null;
		}
		return s;// input is not changed
	}

	@Override
	protected boolean continueFiltering(FilterState state) {
		return !state.isMatchesSomething();
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
