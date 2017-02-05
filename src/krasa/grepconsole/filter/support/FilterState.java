package krasa.grepconsole.filter.support;

import java.util.ArrayList;
import java.util.List;

import krasa.grepconsole.model.Operation;

import org.jetbrains.annotations.NotNull;
import krasa.grepconsole.model.Operation;

import org.jetbrains.annotations.Nullable;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.text.StringUtil;


public class FilterState {

	private int offset;
	private Operation nextOperation = Operation.CONTINUE_MATCHING;
	protected ConsoleViewContentType consoleViewContentType;
	protected List<MyResultItem> resultItemList;
	private boolean exclude;
	private boolean matchesSomething;
	private CharSequence charSequence;

	public FilterState(String text, int offset, Integer maxProcessingTimeAsInt) {
		this.offset = offset;
		charSequence = StringUtil.newBombedCharSequence(text, maxProcessingTimeAsInt);
	}

	@NotNull
	public CharSequence getCharSequence() {
		return charSequence;
	}
	public Operation getNextOperation() {
		return nextOperation;
	}

	public void setNextOperation(Operation nextOperation) {
		this.nextOperation = nextOperation;
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

	public boolean add(MyResultItem resultItem) {
		if (resultItemList == null) {
			resultItemList = new ArrayList<MyResultItem>();
		}
		return resultItemList.add(resultItem);
	}

	@Nullable
	public List<MyResultItem> getResultItemList() {
		return resultItemList;
	}

	public int getOffset() {
		return offset;
	}

}
