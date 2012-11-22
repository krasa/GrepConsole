package krasa.grepconsole.service;

import java.util.ArrayList;
import java.util.List;

import krasa.grepconsole.FilterState;
import krasa.grepconsole.GrepFilter;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;

import org.jetbrains.annotations.Nullable;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;

public class GrepFilterService extends AbstractGrepFilterService implements Filter {

	public GrepFilterService(Project project) {
		super(project);
	}

	public GrepFilterService(Profile profile, List<GrepFilter> grepFilters) {
		super(profile, grepFilters);
	}

	@Nullable
	@Override
	public Result applyFilter(String line, int entireLength) {
		Result result = null;
		if (profile.isEnabledHighlighting()) {
			FilterState state = super.filter(line);
			if (state != null) {
				result = prepareResult(line, entireLength, state);
			}
		}
		return result;
	}

	private Result prepareResult(String line, int entireLength, FilterState state) {
		Result result = null;
		TextAttributes textAttributes = state.getTextAttributes();
		if (textAttributes != null) {
			result = new Result(entireLength - line.length(), entireLength, null, textAttributes);
		}
		return result;
	}

	protected void initFilters() {
		grepFilters = new ArrayList<GrepFilter>();
		for (GrepExpressionItem grepExpressionItem : profile.getGrepExpressionItems()) {
			if (shouldAdd(grepExpressionItem)) {
				grepFilters.add(grepExpressionItem.createFilter());
			}
		}
	}

	private boolean shouldAdd(GrepExpressionItem grepExpressionItem) {
		boolean exclude = grepExpressionItem.isFilterOut();
		boolean enabledFiltering = profile.isEnabledFiltering();
		return (enabledFiltering && !exclude) || !enabledFiltering;
	}

}
