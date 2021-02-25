package krasa.grepconsole.action;

import com.intellij.ide.DataManager;
import com.intellij.ide.dnd.FileCopyPasteUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class TailFileInConsoleToolbarAction extends TailFileInConsoleAction implements CustomComponentAction {
	private static final Logger log = LoggerFactory.getLogger(TailFileInConsoleToolbarAction.class);

	@Override
	public JComponent createCustomComponent(Presentation presentation) {
		final JPanel comp = new JPanel();
		comp.setTransferHandler(new MyTransferHandler());
		comp.add(new JLabel(getTemplatePresentation().getText()));
		comp.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				actionPerformed(AnActionEvent.createFromInputEvent(e,
						ActionPlaces.UNKNOWN,
						presentation, DataManager.getInstance().getDataContext(e.getComponent())));
			}
		});
		comp.setToolTipText(getTemplatePresentation().getDescription());
		return comp;
	}

	private class MyTransferHandler extends TransferHandler {
		@Override
		public boolean importData(JComponent comp, Transferable t) {
			try {
				if (canHandleDrop(t.getTransferDataFlavors())) {
					final List<File> fileList = FileCopyPasteUtil.getFileList(t);
					if (fileList != null) {
						DataContext context = DataManager.getInstance().getDataContext(comp);
						final Project project = CommonDataKeys.PROJECT.getData(context);
						for (File file : fileList) {
							if (!file.isDirectory() && project != null) {
								openFileInConsole(project, file, resolveEncoding(file));
							}
						}
					}
					return true;
				}
				return false;
			} catch (Throwable e) {
				log.error(e.getMessage(), e);
				return false;
			}
		}

		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			return canHandleDrop(transferFlavors);
		}

		public boolean canHandleDrop(DataFlavor[] transferFlavors) {
			return transferFlavors != null && FileCopyPasteUtil.isFileListFlavorAvailable(transferFlavors);
		}
	}
}
