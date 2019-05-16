package krasa.grepconsole.action;

import com.intellij.execution.impl.EditorHyperlinkSupport;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColorPicker;
import com.intellij.util.CommonProcessors;
import krasa.grepconsole.filter.GrepHighlightFilter;
import krasa.grepconsole.model.*;
import krasa.grepconsole.plugin.ServiceManager;
import krasa.grepconsole.utils.Rehighlighter;
import krasa.grepconsole.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collection;
import java.util.regex.Pattern;

@SuppressWarnings("UseJBColor")
public class ToggleEditorHighlightAction extends DumbAwareAction {
	public ToggleEditorHighlightAction() {
	}


	@Override
	public void actionPerformed(AnActionEvent e) {
		Editor editor = e.getData(PlatformDataKeys.EDITOR);
		Project project = e.getProject();
		if (project == null || editor == null) {
			return;
		}
		SelectionModel selectionModel = editor.getSelectionModel();
		MarkupModelEx markupModel = (MarkupModelEx) editor.getMarkupModel();
		if (!selectionModel.hasSelection()) {
			int offset = editor.getCaretModel().getOffset();
			Collection<RangeHighlighterEx> results = getHighlights(markupModel, offset, offset);
			for (RangeHighlighterEx result : results) {
				removeAll(markupModel, result);
			}
		} else {
			int selectionStart = selectionModel.getSelectionStart();
			int selectionEnd = selectionModel.getSelectionEnd();

			Collection<RangeHighlighterEx> highlights = getHighlights(markupModel, selectionStart, selectionEnd);
			for (RangeHighlighterEx highlight : highlights) {
				if (highlight.getStartOffset() == selectionStart && highlight.getEndOffset() == selectionEnd) {
					removeAll(markupModel, highlight);
					return;
				}
			}

			Color color = chooseColor(editor);

			if (color == null) {
				return;
			}

			EditorHyperlinkSupport myHyperlinks = new EditorHyperlinkSupport(editor, project);
			int lineCount = editor.getDocument().getLineCount();
			if (lineCount > 0) {
				Profile profile = new Profile();
				profile.setEnableMaxLengthLimit(false);
				profile.setMaxProcessingTime(String.valueOf(Integer.MAX_VALUE));
				addExpressionItem(Pattern.quote(selectionModel.getSelectedText()), color, profile);
				myHyperlinks.highlightHyperlinks(new GrepHighlightFilter(project, profile), 0, lineCount - 1);
			}
		}
	}

	protected void removeAll(MarkupModelEx markupModel, RangeHighlighterEx result) {
		TextAttributes textAttributes = result.getTextAttributes();

		RangeHighlighter[] allHighlighters = markupModel.getAllHighlighters();
		for (RangeHighlighter allHighlighter : allHighlighters) {
			if (allHighlighter.getTextAttributes() == textAttributes) {
				markupModel.removeHighlighter(allHighlighter);
			}
		}
	}

	protected Color chooseColor(Editor editor) {
		return ColorPicker.showDialog(editor.getComponent(), "Background color", Utils.nextColor(), true, null, true);
	}

	@NotNull
	protected Collection<RangeHighlighterEx> getHighlights(MarkupModelEx markupModel, int from, int to) {
		CommonProcessors.CollectProcessor<RangeHighlighterEx> processor = new CommonProcessors.CollectProcessor<RangeHighlighterEx>() {
			@Override
			protected boolean accept(RangeHighlighterEx o) {
				return o.getLayer() == HighlighterLayer.CONSOLE_FILTER;
			}
		};
		markupModel.processRangeHighlightersOverlappingWith(from, to, processor);
		return processor.getResults();
	}

	protected void add(ConsoleView consoleView, String string, Color color) {
		addExpressionItem(string, color, ServiceManager.getInstance().getProfile(consoleView));
		ServiceManager.getInstance().resetSettings();
		new Rehighlighter().resetHighlights(consoleView);
	}

	private void addExpressionItem(String string, Color color, final Profile profile) {
		GrepStyle style = new GrepStyle();
		style.setForegroundColor(new GrepColor(Color.BLACK));
		style.setBackgroundColor(new GrepColor(color));
		java.util.List<GrepExpressionGroup> grepExpressionGroups = profile.getGrepExpressionGroups();
		GrepExpressionGroup group = grepExpressionGroups.get(0);
		group.getGrepExpressionItems().add(0,
				new GrepExpressionItem().grepExpression(string).style(style).highlightOnlyMatchingText(
						true).operationOnMatch(Operation.CONTINUE_MATCHING));
	}


	@Override
	public void update(AnActionEvent e) {
		Editor editor = e.getData(PlatformDataKeys.EDITOR);
		Presentation presentation = e.getPresentation();
		if (editor != null) {
			String string = editor.getSelectionModel().getSelectedText();
			if (string == null) {
				MarkupModelEx markupModel = (MarkupModelEx) editor.getMarkupModel();
				CommonProcessors.FindFirstProcessor<RangeHighlighterEx> processor1 = new CommonProcessors.FindFirstProcessor<RangeHighlighterEx>() {
					@Override
					protected boolean accept(RangeHighlighterEx rangeHighlighterEx) {
						return rangeHighlighterEx.getLayer() == HighlighterLayer.CONSOLE_FILTER;
					}
				};
				int offset = editor.getCaretModel().getOffset();
				markupModel.processRangeHighlightersOverlappingWith(offset, offset, processor1);
				presentation.setEnabled(processor1.isFound());
			} else {
				presentation.setEnabled(true);
			}
		} else {
			presentation.setEnabled(false);
		}
	}

}
