package krasa.grepconsole.action;

import javax.swing.*;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;

public abstract class HighlightManipulationAction extends DumbAwareAction {
	private static final Logger log = LoggerFactory.getLogger(HighlightManipulationAction.class);
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

	protected void resetHighlightsInConsole(ConsoleView console) {
		console.rehighlightHyperlinksAndFoldings();
	}

	protected void removeAllHighlighters(Editor editor) {
		editor.getMarkupModel().removeAllHighlighters();
	}

	public abstract void applySettings();
}
