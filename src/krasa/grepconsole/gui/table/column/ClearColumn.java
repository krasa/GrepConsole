package krasa.grepconsole.gui.table.column;

import com.intellij.openapi.util.IconLoader;
import krasa.grepconsole.model.GrepExpressionItem;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ClearColumn extends IconColumnInfo {
	public static final Icon DISABLED = IconLoader.getIcon("/krasa/grepconsole/icons/gc.png");
	public static final Icon ENABLED = IconLoader.getIcon("/krasa/grepconsole/icons/clearEnabled.png");

	public ClearColumn(String title) {
		super(title);
	}

	@Override

	protected Icon getIcon(@NotNull GrepExpressionItem value) {
		boolean clearConsole = value.isClearConsole();
		if (clearConsole) {
			return ENABLED;
		} else {
			return DISABLED;
		}
	}

	@Override
	protected void execute(GrepExpressionItem value) {
		value.setClearConsole(!value.isClearConsole());
	}
}
