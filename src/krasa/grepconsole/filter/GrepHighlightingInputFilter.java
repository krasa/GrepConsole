package krasa.grepconsole.filter;

import java.util.*;

import kotlin.ranges.IntRange;
import krasa.grepconsole.filter.support.FilterState;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.filter.support.MyResultItem;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;
import com.intellij.execution.filters.HighlightingInputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;

public class GrepHighlightingInputFilter extends GrepHighlightFilter implements HighlightingInputFilter {

	public GrepHighlightingInputFilter(Project project) {
		super(project);
	}

	public GrepHighlightingInputFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile, grepProcessors);
	}

	@Override
	public List<Pair<IntRange, ConsoleViewContentType>> applyFilter(String s,
			ConsoleViewContentType consoleViewContentType) {
		if (s != null) {
			FilterState state = super.filter(s, 0);

			if (state != null) {
				return prepareResult(s, s.length(), state, consoleViewContentType);
			}
		}
		return null;
	}

	private List<Pair<IntRange, ConsoleViewContentType>> prepareResult(String s, int entireLength, FilterState state,
			ConsoleViewContentType originalConsoleViewContentType) {

		List<MyResultItem> resultItemList = adjustWholeLineMatch(entireLength, state);

		List<Pair<IntRange, ConsoleViewContentType>> pairs = null;
		if (resultItemList != null) {
			pairs = new ArrayList<Pair<IntRange, ConsoleViewContentType>>();
			for (MyResultItem myResultItem : resultItemList) {
				IntRange integers = new IntRange(myResultItem.getHighlightStartOffset(),
						myResultItem.getHighlightEndOffset());
				ConsoleViewContentType consoleViewContentType = myResultItem.getConsoleViewContentType();
				if (consoleViewContentType == null) {
					consoleViewContentType = originalConsoleViewContentType;
				}
				pairs.add(Pair.create(integers, consoleViewContentType));
			}
		}

		return pairs;
	}

	@NotNull
	private Set<Map.Entry<Range<Integer>, MyResultItem>> toRanges(List<MyResultItem> resultItemList) {
		TreeRangeMap<Integer, MyResultItem> treeRangeMap = TreeRangeMap.create();
		Collections.reverse(resultItemList);
		for (MyResultItem resultItem : resultItemList) {
			int start = resultItem.getHighlightStartOffset();
			int end = resultItem.getHighlightEndOffset();
			treeRangeMap.put(Range.closed(start, end), resultItem);
		}
		return treeRangeMap.asMapOfRanges().entrySet();
	}

	@Override
	protected GrepProcessor createProcessor(GrepExpressionItem grepExpressionItem) {
		return grepExpressionItem.createProcessor();
	}
}
