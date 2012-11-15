package krasa.grepconsole;

import com.intellij.openapi.editor.markup.TextAttributes;

public class DecoratorState {

	private String line;
	private Operation nextOperation = Operation.CONTINUE_MATCHING;
	protected TextAttributes textAttributes;

	public DecoratorState(String line) {
		this.line = line;
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

	public void setTextAttributes(TextAttributes textAttributes) {
		this.textAttributes = textAttributes;
	}

	public TextAttributes getTextAttributes() {
		return textAttributes;
	}

}
