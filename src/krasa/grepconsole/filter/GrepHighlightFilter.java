package krasa.grepconsole.filter;

import java.util.Collections;
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

	private long executionId;
	private TextAttributes lastTextAttributes = null;

	public GrepHighlightFilter(Project project) {
		super(project);
	}

	public GrepHighlightFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile, grepProcessors);
	}

	@Nullable
	@Override
	// line can be empty sometimes under heavy load
	public Result applyFilter(@Nullable String s, int entireLength) {
		Result result = null;
		int offset = entireLength;
		if (s != null) {
			offset = entireLength - s.length();
		}
		FilterState state = super.filter(s, offset);
		if (state != null) {
			result = prepareResult(entireLength, state);
		}
		return result;
	}

	private Result prepareResult(int entireLength, FilterState state) {
		Result result = null;
		TextAttributes textAttributes = state.getTextAttributes();
		List<ResultItem> resultItemList = state.getResultItemList();
		if (textAttributes != null) {
			lastTextAttributes = textAttributes;
			if (resultItemList == null) {
				resultItemList = Collections.singletonList(getResultItem(entireLength, state, textAttributes));
			} else {
				resultItemList.add(getResultItem(entireLength, state, textAttributes));
			}
		} else if (lastTextAttributes != null && profile.isMultiLineOutput()) {
			if (resultItemList == null) {
				resultItemList = Collections.singletonList(getResultItem(entireLength, state, lastTextAttributes));
			} else {
				resultItemList.add(getResultItem(entireLength, state, lastTextAttributes));
			}
		}
		if (resultItemList != null) {
			result = new Result(resultItemList);
			result.setNextAction(NextAction.CONTINUE_FILTERING);
		}
		return result;
	}

	private ResultItem getResultItem(int entireLength, FilterState state, TextAttributes textAttributes) {
		return new ResultItem(state.getOffset(), entireLength, null, textAttributes);
	}

	@Override
	public void onChange() {
		super.onChange();
		lastTextAttributes = null;
	}

	@Override
	protected boolean shouldAdd(GrepExpressionItem grepExpressionItem) {
		return profile.isEnabledHighlighting()
				&& !(profile.isEnabledInputFiltering() && grepExpressionItem.isInputFilter());
	}

	public long getExecutionId() {
		return executionId;
	}

	public void setExecutionId(long executionId) {
		this.executionId = executionId;
	}

	public boolean hasGrepProcessorsForStatusBar() {
		final List<GrepProcessor> grepProcessors = getGrepProcessors();
		for (GrepProcessor grepProcessor : grepProcessors) {
			if (grepProcessor.getGrepExpressionItem().isShowCountInStatusBar()) {
				return true;
			}
		}
		return false;
	}

	public boolean hasGrepProcessorsForConsolePanel() {
		final List<GrepProcessor> grepProcessors = getGrepProcessors();
		for (GrepProcessor grepProcessor : grepProcessors) {
			if (grepProcessor.getGrepExpressionItem().isShowCountInConsole()) {
				return true;
			}
		}
		return false;
	}

}
