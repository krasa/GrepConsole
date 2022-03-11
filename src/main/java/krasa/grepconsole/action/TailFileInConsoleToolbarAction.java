package krasa.grepconsole.action;

import com.intellij.ide.DataManager;
import com.intellij.ide.dnd.FileCopyPasteUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.model.TailSettings;
import krasa.grepconsole.plugin.PluginState;
import krasa.grepconsole.plugin.TailHistory;
import krasa.grepconsole.tail.runConfiguration.TailRunConfigurationSettings;
import krasa.grepconsole.tail.runConfiguration.TailUtils;
import org.jetbrains.annotations.NotNull;
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
	public @NotNull
	JComponent createCustomComponent(@NotNull Presentation presentation, @NotNull String place) {
		final JPanel comp = new JPanel();
		comp.setTransferHandler(new MyTransferHandler());
		comp.add(new JLabel(getTemplatePresentation().getText()));
		comp.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				actionPerformed(AnActionEvent.createFromInputEvent(e,
						"GrepConsole-Tail-" + place,
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
				DataContext context = DataManager.getInstance().getDataContext(comp);
				final Project project = CommonDataKeys.PROJECT.getData(context);
				if (project == null) {
					return true;
				}

				DataFlavor[] transferFlavors = t.getTransferDataFlavors();
				if (transferFlavors != null && FileCopyPasteUtil.isFileListFlavorAvailable(transferFlavors)) {
					final List<File> fileList = FileCopyPasteUtil.getFileList(t);
					if (fileList != null) {
						int directories = 0;
						for (File file : fileList) {
							if (file.exists() && file.isDirectory()) {
								directories++;
							}
						}
						if (directories == 0) {
							TailHistory.getState(project).add2(fileList);
							for (File file : fileList) {
								openFileInConsole(project, file, resolveEncoding(file));
							}
						} else {
							TailSettings tailSettings = PluginState.getInstance().getTailSettings();
							TailRunConfigurationSettings lastTail = tailSettings.getLastTail();
							List<String> paths = lastTail.getPaths();
							paths.clear();
							for (File file : fileList) {
								paths.add(file.getAbsolutePath());
							}
							showAdvancedDialog(project, tailSettings, lastTail);
						}
					}
					return true;
				} else if (canHandlePlainText(transferFlavors)) {
					String path = (String) t.getTransferData(DataFlavor.stringFlavor);
					path = path.trim();
					TailHistory.getState(project).add(new File(path));

					TailUtils.openAllMatching(path, false, file -> openFileInConsole(project, file, resolveEncoding(file)));
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
			return transferFlavors != null && (canHandlePlainText(transferFlavors) || FileCopyPasteUtil.isFileListFlavorAvailable(transferFlavors));
		}

		public boolean canHandlePlainText(DataFlavor[] transferFlavors) {
			if (transferFlavors != null) {
				for (DataFlavor flavor : transferFlavors) {
					if (flavor != null && (flavor.equals(DataFlavor.stringFlavor))) {
						return true;
					}
				}

			}
			return false;
		}
	}
}
