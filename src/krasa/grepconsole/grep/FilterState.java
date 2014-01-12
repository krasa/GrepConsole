package krasa.grepconsole.grep;

import krasa.grepconsole.filter.support.ConsoleMode;
import krasa.grepconsole.model.Operation;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.markup.TextAttributes;

public class FilterState {

	private String text;
	private Operation nextOperation = Operation.CONTINUE_MATCHING;
	private final ConsoleMode consoleMode;
	protected ConsoleViewContentType consoleViewContentType;
	private boolean exclude;

	public FilterState(String text, ConsoleMode consoleMode) {
		this.text = text;
		this.consoleMode = consoleMode;
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

	public ConsoleMode getConsoleMode() {
		return consoleMode;
	}

}
