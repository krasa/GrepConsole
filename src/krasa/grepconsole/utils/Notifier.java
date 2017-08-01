package krasa.grepconsole.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.grep.GrepModel;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

public class Notifier {
	public static void notify_InputAndHighlight(String substring, GrepProcessor grepProcessor, final Project project) {
		final Notification notification = GrepConsoleApplicationComponent.NOTIFICATION.createNotification(
				"Grep Console plugin: processing took too long, aborting to prevent GUI freezing.\n"
						+ "Consider changing following settings: 'Match only first N characters on each line' or 'Max processing time for a line'\n"
						+ "Last expression: [" + grepProcessor + "]\n" + "Line: " + Utils.toNiceLineForLog(substring)
						+ "\n(More notifications will not be displayed for this console filter. Notification can be disabled at File | Settings | Appearance & Behavior | Notifications`)"

				, MessageType.WARNING);
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			@Override
			public void run() {
				Notifications.Bus.notify(notification, project);
			}
		});
	}

	public static void notify_GrepCopyingFilter(String substring, GrepModel.Matcher matcher, final Project project) {
		final Notification notification = GrepConsoleApplicationComponent.NOTIFICATION.createNotification(
				"Grep Console plugin: Grep to a subconsole took too long, aborting to prevent input freezing.\n"
						+ "Consider changing following settings: 'Match only first N characters on each line' or 'Max processing time for a line'\n"
						+ "Matcher: " + matcher + "\n" + "Line: " + Utils.toNiceLineForLog(substring)
						+ "\n(More notifications will not be displayed for this console filter. Notification can be disabled at File | Settings | Appearance & Behavior | Notifications`)"

				, MessageType.WARNING);
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			@Override
			public void run() {
				Notifications.Bus.notify(notification, project);
			}
		});
	}
}
