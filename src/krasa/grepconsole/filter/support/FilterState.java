package krasa.grepconsole.filter.support;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.markup.TextAttributes;
import krasa.grepconsole.model.Operation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class FilterState {

	private int offset;
	private Operation nextOperation = Operation.CONTINUE_MATCHING;
	protected ConsoleViewContentType consoleViewContentType;
	protected List<MyResultItem> resultItemList;
	private boolean exclude;
	private boolean matchesSomething;
	private CharSequence charSequence;
	private boolean clearConsole;

	public FilterState(int offset, CharSequence charSequence) {
		this.offset = offset;
		this.charSequence = charSequence;
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
			resultItemList = new ArrayList<>();
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

	public void setClearConsole(boolean clearConsole) {
		this.clearConsole |= clearConsole;
	}

	public boolean isClearConsole() {
		return clearConsole;
	}
}
