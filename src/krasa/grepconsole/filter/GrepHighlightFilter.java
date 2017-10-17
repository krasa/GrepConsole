package krasa.grepconsole.filter;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.filter.support.FilterState;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.filter.support.MyResultItem;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/** must be executed in single thread, see #createProcessor */
public class GrepHighlightFilter extends AbstractGrepFilter implements Filter {

	protected ConsoleViewContentType lastTextAttributes = null;

	public GrepHighlightFilter(Project project, Profile profile) {
		super(project, profile);
	}

	protected GrepHighlightFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile, grepProcessors);
	}

	@Nullable
	@Override
	// line can be empty sometimes under heavy load
	public Result applyFilter(@Nullable String s, int entireLength) {
		int offset = entireLength;
		if (s != null) {
			offset = entireLength - s.length();
		}
		FilterState state = super.filter(s, offset);

		Result result = null;
		if (state != null) {
			result = prepareResult(entireLength, state);
		}
		return result;
	}

	private Result prepareResult(int entireLength, FilterState state) {
		Result result = null;
		List<MyResultItem> resultItemList = adjustWholeLineMatch(entireLength, state);
		if (resultItemList != null) {
			result = new Result(MyResultItem.toIJ(resultItemList));
			result.setNextAction(NextAction.CONTINUE_FILTERING);
		}
		return result;
	}

	protected List<MyResultItem> adjustWholeLineMatch(int entireLength, FilterState state) {
		ConsoleViewContentType textAttributes = state.getConsoleViewContentType();
		List<MyResultItem> resultItemList = state.getResultItemList();
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
		return resultItemList;
	}

	private MyResultItem getResultItem(int entireLength, FilterState state, ConsoleViewContentType textAttributes) {
		return new MyResultItem(state.getOffset(), entireLength, null, textAttributes);
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

	@Override
	protected GrepProcessor createProcessor(GrepExpressionItem grepExpressionItem) {
		return grepExpressionItem.createThreadUnsafeProcessor();
	}
}
