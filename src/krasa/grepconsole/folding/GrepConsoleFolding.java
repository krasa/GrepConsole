package krasa.grepconsole.folding;

import java.util.List;
import java.util.regex.Pattern;

import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

import org.jetbrains.annotations.Nullable;

import com.intellij.execution.ConsoleFolding;

/**
 * @author Vojtech Krasa
 */
public class GrepConsoleFolding extends ConsoleFolding {
	private GrepConsoleApplicationComponent grepConsoleApplicationComponent;

	public GrepConsoleFolding(GrepConsoleApplicationComponent grepConsoleApplicationComponent) {
		this.grepConsoleApplicationComponent = grepConsoleApplicationComponent;
	}

	@Override
	public boolean shouldFoldLine(String line) {
		List<GrepExpressionItem> foldings = grepConsoleApplicationComponent.getFoldingItems();
		for (int i = 0; i < foldings.size(); i++) {
			GrepExpressionItem grepExpressionItem = foldings.get(i);
			Pattern unlessPattern = grepExpressionItem.getUnlessPattern();
			if (unlessPattern != null && unlessPattern.matcher(line).matches()) {
				return false;
			}
			Pattern pattern = grepExpressionItem.getPattern();
			if (pattern != null && pattern.matcher(line).matches()) {
				return true;
			}
		}
		return false;
	}

	@Nullable
	@Override
	public String getPlaceholderText(List<String> lines) {
		String s = lines.size() > 1 ? "s" : "";
		return " <" + lines.size() + " folded line" + s + ">";
	}
}
