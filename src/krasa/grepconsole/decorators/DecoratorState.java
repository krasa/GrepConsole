package krasa.grepconsole.decorators;

import krasa.grepconsole.model.ModifiableConsoleViewContentType;

import com.intellij.execution.ui.ConsoleViewContentType;

public class DecoratorState {

	private String line;
	private ConsoleViewContentType contentType;
	private Operation nextOperation = Operation.CONTINUE_MATCHING;

	public DecoratorState(String line, ConsoleViewContentType contentType) {
		this.line = line;
		this.contentType = contentType;
	}

	public Operation getNextOperation() {
		return nextOperation;
	}

	public void setNextOperation(Operation nextOperation) {
		this.nextOperation = nextOperation;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public ConsoleViewContentType getContentType() {
		return contentType;
	}

	public void setContentType(ModifiableConsoleViewContentType contentType) {
		this.contentType = contentType;
	}

}
