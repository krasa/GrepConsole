package krasa.grepconsole.decorators;

import com.intellij.execution.ui.ConsoleViewContentType;
import krasa.grepconsole.model.ModifiableConsoleViewContentType;

public class DecoratorState {

	private String line;
	private ConsoleViewContentType contentType;
	private NextOperation operation = NextOperation.CONTINUE_MATCHING;

	public DecoratorState(String line, ConsoleViewContentType contentType) {
		this.line = line;
		this.contentType = contentType;
	}

	public NextOperation getOperation() {
		return operation;
	}

	public void setOperation(NextOperation operation) {
		this.operation = operation;
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
