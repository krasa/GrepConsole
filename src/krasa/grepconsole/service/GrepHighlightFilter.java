package krasa.grepconsole.service;

import java.util.List;

import krasa.grepconsole.grep.FilterState;
import krasa.grepconsole.grep.GrepProcessor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;

import org.jetbrains.annotations.Nullable;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;

public class GrepHighlightFilter extends AbstractGrepFilter implements Filter {

	public GrepHighlightFilter(Project project) {
		super(project);
	}

	public GrepHighlightFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile, grepProcessors);
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
		return profile.isEnabledHighlighting();
	}

}
