package krasa.grepconsole.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

public class Notifier {
	public static void notify_InputAndHighlight(final Project project, String message) {
		final Notification notification = GrepConsoleApplicationComponent.NOTIFICATION.createNotification(
				"GrepConsole plugin: " + message
						+ "\n(More notifications will not be displayed for this console filter. Notification can be disabled at File | Settings | Appearance & Behavior | Notifications`)"

				, MessageType.WARNING);
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			@Override
			public void run() {
				Notifications.Bus.notify(notification, project);
			}
		});
	}

	public static void notify_GrepFilter(final Project project, String message) {
		final Notification notification = GrepConsoleApplicationComponent.NOTIFICATION.createNotification(
				"GrepConsole plugin: " + message
						+ "\n(More notifications will not be displayed for this console filter. Notification can be disabled at File | Settings | Appearance & Behavior | Notifications`)"

				, MessageType.WARNING);
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			@Override
			public void run() {
				Notifications.Bus.notify(notification, project);
			}
		});
	}

	public static void notify_MissingExtension(String action, final Project project) {
		final Notification notification = GrepConsoleApplicationComponent.NOTIFICATION.createNotification(
				"GrepConsole : missing script '" + action + "'"

				, MessageType.WARNING);
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			@Override
			public void run() {
				Notifications.Bus.notify(notification, project);
			}
		});
	}
}
