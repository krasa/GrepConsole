package krasa.grepconsole.utils;

import com.intellij.ide.CopyProvider;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public class Utils {
	public static int tryParseInteger(String text) {
		if ("".equals(text)) {
			return 0;
		}
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static boolean isSelectedText(AnActionEvent e) {
		DataContext dataContext = e.getDataContext();
		CopyProvider provider = PlatformDataKeys.COPY_PROVIDER.getData(dataContext);
		return provider != null && provider.isCopyEnabled(dataContext) && provider.isCopyVisible(dataContext);
	}

	public static String getString(AnActionEvent e) {
		DataContext dataContext = e.getDataContext();
		CopyProvider provider = PlatformDataKeys.COPY_PROVIDER.getData(dataContext);
		if (provider == null) {
			return null;
		}
		provider.performCopy(dataContext);
		Transferable contents = CopyPasteManager.getInstance().getContents();
		try {
			return contents == null ? null : (String) contents.getTransferData(DataFlavor.stringFlavor);
		} catch (Exception e1) {
			return null;
		}
	}

	public static String getSelectedString(AnActionEvent e) {
		DataContext dataContext = e.getDataContext();
		Editor editor = CommonDataKeys.EDITOR.getData(dataContext);
		if (editor == null) {
			return null;
		}
		CaretModel caretModel = editor.getCaretModel();
		Caret primaryCaret = caretModel.getPrimaryCaret();
		return primaryCaret.getSelectedText();
	}

	@NotNull
	public static String toNiceLineForLog(String substring) {
		int length = substring.length();
		int endIndex = substring.length();
		int min = Math.min(endIndex, 120);

		String result = substring.substring(0, min);
		if (min < endIndex) {
			result += "...";
		}
		result += " [length=" + length + "]";
		return result;
	}
}
