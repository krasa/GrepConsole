package krasa.grepconsole.grep;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.markup.TextAttributes;
import krasa.grepconsole.model.Operation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FilterState {

	private String text;
	private int offset;
	private Operation nextOperation = Operation.CONTINUE_MATCHING;
	protected ConsoleViewContentType consoleViewContentType;
	protected List<Filter.ResultItem> resultItemList;
	private boolean exclude;
	private boolean matchesSomething;

	public FilterState(String text, int offset) {
		this.text = text;
		this.offset = offset;
	}

	public Operation getNextOperation() {
		return nextOperation;
	}

	public void setNextOperation(Operation nextOperation) {
		this.nextOperation = nextOperation;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setConsoleViewContentType(ConsoleViewContentType consoleViewContentType) {
		this.consoleViewContentType = consoleViewContentType;
	}

	public ConsoleViewContentType getConsoleViewContentType() {
		return consoleViewContentType;
	}

	public TextAttributes getTextAttributes() {
		if (consoleViewContentType == null) {
			return null;
		}
		return consoleViewContentType.getAttributes();

	}

	public void setExclude(boolean exclude) {
		this.exclude = exclude;
	}

	public boolean isExclude() {
		return exclude;
	}


	public void setMatchesSomething(boolean matchesSomething) {
		this.matchesSomething = matchesSomething;
	}

	public boolean isMatchesSomething() {
		return matchesSomething;
	}

	public boolean add(Filter.ResultItem resultItem) {
		if (resultItemList == null) {
			resultItemList = new ArrayList<Filter.ResultItem>();
		}
		return resultItemList.add(resultItem);
	}

	@Nullable
	public List<Filter.ResultItem> getResultItemList() {
		return resultItemList;
	}

	public int getOffset() {
		return offset;
	}
}
