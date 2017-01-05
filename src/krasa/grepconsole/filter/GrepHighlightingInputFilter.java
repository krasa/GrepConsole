package krasa.grepconsole.filter;

import com.intellij.execution.filters.InputFilter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Profile;

import java.util.ArrayList;
import java.util.List;

public class GrepHighlightingInputFilter extends GrepHighlightFilter implements InputFilter {

	public GrepHighlightingInputFilter(Project project) {
		super(project);
	}

	public GrepHighlightingInputFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile, grepProcessors);
	}

	@Override
	protected GrepProcessor createProcessor(GrepExpressionItem grepExpressionItem) {
		return grepExpressionItem.createProcessor();
	}

	@Override
	public List<Pair<String, ConsoleViewContentType>> applyFilter(String s,
																  ConsoleViewContentType consoleViewContentType) {
		if (!s.endsWith("\n")) {

			System.err.println("");
		}
		Result result = applyFilter(s, s.length());
		if (result == null) {
			return null;
		}
		List<ResultItem> resultItems = result.getResultItems();
		if (resultItems == null) {
			return null;
		}
		int lastIndex = 0;
		ArrayList<Pair<String, ConsoleViewContentType>> pairs = new ArrayList<Pair<String, ConsoleViewContentType>>();
		for (int i = 0; i < result.getResultItems().size(); i++) {
			ResultItem resultItem = result.getResultItems().get(i);
			int highlightStartOffset = resultItem.getHighlightStartOffset();
			int highlightEndOffset = resultItem.getHighlightEndOffset();

			if (lastIndex < highlightStartOffset) {
				pairs.add(Pair.create(s.substring(lastIndex, highlightStartOffset), consoleViewContentType));
			}

			ConsoleViewContentType second = new ConsoleViewContentType(resultItem.getHighlightAttributes().toString(), resultItem.getHighlightAttributes());
			String substring = s.substring(highlightStartOffset, highlightEndOffset);
			pairs.add(Pair.create(substring, second));
			lastIndex = highlightEndOffset;

			if (i == result.getResultItems().size() - 1) {
				if (s.length() > lastIndex) {
					pairs.add(Pair.create(s.substring(lastIndex, s.length()), consoleViewContentType));
				}
			}
		}


		return pairs;

	}

}
