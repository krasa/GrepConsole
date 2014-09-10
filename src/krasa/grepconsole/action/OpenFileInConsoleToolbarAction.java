package krasa.grepconsole.action;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

import javax.swing.*;

import com.intellij.ide.DataManager;
import com.intellij.ide.dnd.FileCopyPasteUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.Project;

/**
 * @author Vojtech Krasa
 */
public class OpenFileInConsoleToolbarAction extends OpenFileInConsoleAction implements CustomComponentAction {

	@Override
	public JComponent createCustomComponent(Presentation presentation) {
		final JPanel comp = new JPanel();
		comp.setTransferHandler(new MyTransferHandler());
		comp.add(new JLabel(getTemplatePresentation().getText()));
		comp.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				actionPerformed(AnActionEvent.createFromInputEvent(OpenFileInConsoleToolbarAction.this, e,
						ActionPlaces.UNKNOWN));
			}
		});
		comp.setToolTipText(getTemplatePresentation().getDescription());
		return comp;
	}

	private class MyTransferHandler extends TransferHandler {
		@Override
		public boolean importData(JComponent comp, Transferable t) {
			if (canHandleDrop(t.getTransferDataFlavors())) {
				final List<File> fileList = FileCopyPasteUtil.getFileList(t);
				if (fileList != null) {
					DataContext context = DataManager.getInstance().getDataContext(comp);
					final Project data = PlatformDataKeys.PROJECT.getData(context);
					for (File file : fileList) {
						if (!file.isDirectory()) {
							openFileInConsole(data, file);
						}
					}
				}
				return true;
			}
			return false;
		}

		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			return canHandleDrop(transferFlavors);
		}

		public boolean canHandleDrop(DataFlavor[] transferFlavors) {
			return transferFlavors != null && FileCopyPasteUtil.isFileListFlavorSupported(transferFlavors);
		}
	}
}
