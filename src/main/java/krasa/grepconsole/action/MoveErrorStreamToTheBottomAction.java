package krasa.grepconsole.action;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.impl.DocumentImpl;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.CommonProcessors;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MoveErrorStreamToTheBottomAction extends DumbAwareAction {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(MoveErrorStreamToTheBottomAction.class);
	private final ConsoleViewImpl console;

	public MoveErrorStreamToTheBottomAction(ConsoleViewImpl console) {
		super("Move stderr to the bottom", "Provided by Grep Console plugin", AllIcons.ObjectBrowser.SortByType);
		this.console = console;
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		EditorEx myEditor = (EditorEx) console.getEditor();
		DocumentImpl document = (DocumentImpl) myEditor.getDocument();

		Key<ConsoleViewContentType> contentTypeKey = getConsoleViewContentTypeKey();

		try {
			document.setInBulkUpdate(true);
			myEditor.getScrollingModel().accumulateViewportChanges();

			MarkupModelEx model = (MarkupModelEx) DocumentMarkupModel.forDocument(myEditor.getDocument(), getEventProject(e), false);

			Key<ConsoleViewContentType> finalContentTypeKey = contentTypeKey;
			CommonProcessors.CollectProcessor<RangeHighlighter> processor = new CommonProcessors.CollectProcessor<RangeHighlighter>() {
				@Override
				protected boolean accept(RangeHighlighter rangeHighlighter) {
					ConsoleViewContentType contentType = rangeHighlighter.getUserData(finalContentTypeKey);
					return rangeHighlighter.isValid() && contentType == ConsoleViewContentType.ERROR_OUTPUT;
				}
			};
			model.processRangeHighlightersOverlappingWith(0, document.getTextLength(), processor);
			ArrayList<RangeHighlighter> highlighters = (ArrayList<RangeHighlighter>) processor.getResults();


			List<String> strings = new ArrayList<>();

			for (int i = highlighters.size() - 1; i >= 0; i--) {
				RangeHighlighter tokenMarker = highlighters.get(i);
				int startOffset = tokenMarker.getStartOffset();
				int endOffset = tokenMarker.getEndOffset();

				strings.add(document.getText(TextRange.create(startOffset, endOffset)));
				tokenMarker.dispose();
				document.deleteString(startOffset, endOffset);
			}

			int start = document.getTextLength();
			document.insertString(document.getTextLength(), join(strings));
			createTokenRangeHighlighter(ConsoleViewContentType.ERROR_OUTPUT, start, document.getTextLength(), model, contentTypeKey);
		} finally {
			document.setInBulkUpdate(false);
			myEditor.getScrollingModel().flushViewportChanges();
		}
	}

	public static Key<ConsoleViewContentType> getConsoleViewContentTypeKey() {
		Key<ConsoleViewContentType> contentTypeKey = null;
		try {
			Field content_type = ConsoleViewImpl.class.getDeclaredField("CONTENT_TYPE");
			content_type.setAccessible(true);
			contentTypeKey = (Key<ConsoleViewContentType>) content_type.get(null);


		} catch (Throwable ex) {
			//obfuscated class?
			Field[] declaredFields = ConsoleViewImpl.class.getDeclaredFields();
			for (Field declaredField : declaredFields) {
				Class<?> type = declaredField.getType();
				if (type == Key.class) {
					declaredField.setAccessible(true);
					try {
						Key key = (Key) declaredField.get(null);
						String x = key.toString();
						if (x.equals("ConsoleViewContentType")) {
							return key;
						}
					} catch (Throwable e) {
					}
				}
			}
			throw new RuntimeException(ex);
		}
		return contentTypeKey;
	}

	@NotNull
	protected String join(List<String> strings) {
		Collections.reverse(strings);

		StringBuilder sb = new StringBuilder();
		for (String string : strings) {
			sb.append(string);
		}
		return sb.toString();
	}


	private void createTokenRangeHighlighter(@NotNull ConsoleViewContentType contentType,
											 int startOffset,
											 int endOffset, MarkupModel model, Key<ConsoleViewContentType> CONTENT_TYPE) {
		TextAttributes attributes = contentType.getAttributes();
		int layer = HighlighterLayer.SYNTAX + 1; // make custom filters able to draw their text attributes over the default ones
		RangeHighlighter tokenMarker = model.addRangeHighlighter(startOffset, endOffset, layer,
				attributes, HighlighterTargetArea.EXACT_RANGE);
		tokenMarker.putUserData(CONTENT_TYPE, contentType);
	}


}
