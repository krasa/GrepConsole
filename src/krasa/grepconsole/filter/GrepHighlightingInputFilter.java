package krasa.grepconsole.filter;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;
import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import krasa.grepconsole.filter.support.FilterState;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.filter.support.MyResultItem;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GrepHighlightingInputFilter extends GrepHighlightFilter implements InputFilter {

	public GrepHighlightingInputFilter(Project project) {
		super(project);
	}

	public GrepHighlightingInputFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile, grepProcessors);
	}

	@Override
	public List<Pair<String, ConsoleViewContentType>> applyFilter(String s,
			ConsoleViewContentType consoleViewContentType) {
		if (s != null) {
			FilterState state = super.filter(s, 0);

			if (state != null) {
				return prepareResult(s, s.length(), state, consoleViewContentType);
			}
		}
		return null;
	}

	private List<Pair<String, ConsoleViewContentType>> prepareResult(String s, int entireLength, FilterState state,
			ConsoleViewContentType originalConsoleViewContentType) {

		List<MyResultItem> resultItemList = adjustWholeLineMatch(entireLength, state);

		int lastIndex = 0;
		List<Pair<String, ConsoleViewContentType>> pairs = null;
		if (resultItemList != null) {
			Set<Map.Entry<Range<Integer>, MyResultItem>> entries = toRanges(resultItemList);

			pairs = new ArrayList<>(entries.size());

			for (Map.Entry<Range<Integer>, MyResultItem> entry : entries) {
				Range<Integer> key = entry.getKey();
				MyResultItem value = entry.getValue();
				Integer start = key.lowerEndpoint();
				Integer end = key.upperEndpoint();

				if (lastIndex < start) {
					pairs.add(Pair.create(s.substring(lastIndex, start), originalConsoleViewContentType));
				}

				pairs.add(Pair.create(s.substring(start, end), value.getConsoleViewContentType()));
				lastIndex = end;
			}
			if (s.length() > lastIndex) {
				pairs.add(Pair.create(s.substring(lastIndex, s.length()), originalConsoleViewContentType));
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
