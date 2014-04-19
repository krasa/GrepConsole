package krasa.grepconsole.filter;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.grep.FilterState;
import krasa.grepconsole.grep.GrepProcessor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GrepHighlightFilter extends AbstractGrepFilter implements Filter {

	public GrepHighlightFilter(Project project) {
		super(project);
	}

	public GrepHighlightFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile, grepProcessors);
	}

	private TextAttributes lastTextAttributes = null;

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
			lastTextAttributes = textAttributes;
			result = new Result(entireLength - line.length(), entireLength, null, textAttributes);
			result.setNextAction(NextAction.CONTINUE_FILTERING);
		} else if (lastTextAttributes != null && profile.isMultiLineOutput()) {
			result = new Result(entireLength - line.length(), entireLength, null, lastTextAttributes);
			result.setNextAction(NextAction.CONTINUE_FILTERING);
		}
		return result;
	}

	@Override
	public void onChange() {
		super.onChange();
		lastTextAttributes = null;
	}

	@Override
	protected boolean shouldAdd(GrepExpressionItem grepExpressionItem) {
		return profile.isEnabledHighlighting() && !(profile.isEnabledInputFiltering() && grepExpressionItem.isInputFilter());
	}

}
