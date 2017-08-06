package krasa.grepconsole.action;

import com.intellij.execution.filters.Filter;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class HighlightManipulationAction extends DumbAwareAction {

	public static final Filter FILTER = new Filter() {
		@Nullable
		@Override
		public Result applyFilter(String s, int i) {
			return null;
		}
	};

	public HighlightManipulationAction() {
	}

	public HighlightManipulationAction(@Nullable String text) {
		super(text);
	}

	public HighlightManipulationAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	public abstract void applySettings();
}
