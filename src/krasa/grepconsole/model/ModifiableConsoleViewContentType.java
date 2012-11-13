package krasa.grepconsole.model;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.TextAttributes;

public class ModifiableConsoleViewContentType extends ConsoleViewContentType {
    private static final Logger logger = Logger.getInstance("krasa.grepconsole.model.ModifiableConsoleViewContentType");

	protected final TextAttributes attributes;

	public static ModifiableConsoleViewContentType create(String id, ConsoleViewContentType contentType) {
        logger.debug("New instance creation from " + contentType);
        TextAttributes clone = contentType.getAttributes().clone();
        return new ModifiableConsoleViewContentType(id, clone);
    }

	private ModifiableConsoleViewContentType(String id, TextAttributes clone) {
		super(id, clone);
		attributes = clone;
	}
}
