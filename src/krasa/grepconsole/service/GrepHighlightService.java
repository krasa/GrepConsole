package krasa.grepconsole.service;

import java.util.List;

import krasa.grepconsole.FilterState;
import krasa.grepconsole.GrepFilter;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;

import org.jetbrains.annotations.Nullable;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;

public class GrepHighlightService extends AbstractGrepService implements Filter {

	public GrepHighlightService(Project project) {
		super(project);
	}

	public GrepHighlightService(Profile profile, List<GrepFilter> grepFilters) {
		super(profile, grepFilters);
	}

	@Nullable
	@Override
	public Result applyFilter(String s, int entireLength) {
		Result result = null;
		FilterState state = super.filter(s);
		if (state != null) {
			result = prepareResult(s, entireLength, state);
		}
		return result;
	}

	private Result prepareResult(String line, int entireLength, FilterState state) {
		Result result = null;
		TextAttributes textAttributes = state.getTextAttributes();
		if (textAttributes != null) {
			result = new Result(entireLength - line.length(), entireLength, null, textAttributes);
			result.setNextAction(NextAction.CONTINUE_FILTERING);
		}
		return result;
	}

	@Override
	protected boolean shouldAdd(GrepExpressionItem grepExpressionItem) {
		return profile.isEnabledHighlighting() && isDisabledInputFiltering(grepExpressionItem);
	}

	private boolean isDisabledInputFiltering(GrepExpressionItem grepExpressionItem) {
		boolean inputFilter = grepExpressionItem.isInputFilter();
		boolean enabledInputFiltering = profile.isEnabledInputFiltering();
		return !(inputFilter && enabledInputFiltering);
	}

}
