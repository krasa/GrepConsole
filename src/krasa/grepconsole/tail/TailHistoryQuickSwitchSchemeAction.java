package krasa.grepconsole.tail;

import com.intellij.ide.actions.QuickSwitchSchemeAction;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsFileUtil;
import krasa.grepconsole.plugin.TailHistory;
import krasa.grepconsole.plugin.TailItem;
import krasa.grepconsole.tail.runConfiguration.TailUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import static krasa.grepconsole.action.TailFileInConsoleAction.openFileInConsole;
import static krasa.grepconsole.action.TailFileInConsoleAction.resolveEncoding;

public class TailHistoryQuickSwitchSchemeAction extends QuickSwitchSchemeAction implements DumbAware {
	@Override
	protected void fillActions(Project project, @NotNull DefaultActionGroup defaultActionGroup, @NotNull DataContext dataContext) {
		TailHistory state = TailHistory.getState(project);
		Set<TailItem> tailHistory = state.getItems();
		if (tailHistory.isEmpty()) {
			ApplicationManager.getApplication().invokeLater(() -> {
				Notification notification = NotificationGroupManager.getInstance().getNotificationGroup("Grep Console").createNotification("No tail in history", MessageType.INFO);
				Notifications.Bus.notify(notification);
			});
		}

		ArrayList<TailItem> list = new ArrayList<>(tailHistory);
		Collections.reverse(list);
		VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
		File root = null;
		if (projectDir != null) {
			root = new File(projectDir.getPath());
		}
		for (TailItem s : list) {
			File file = new File(s.getPath());
			if (!file.exists()) {
				state.removeFromHistory(s);
				continue;
			}

			String text = s.getPath();
			if (root != null) {
				text = VcsFileUtil.relativePath(root, file);
			}
			if (s.isNewestMatching()) {
				text += " (newest)";
			}
			defaultActionGroup.add(new DumbAwareAction(text) {
				@Override
				public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
					state.add(s);
					TailUtils.openAllMatching(file.getPath(), false, file -> openFileInConsole(project, file, resolveEncoding(file)));
				}
			});
		}
	}
}
