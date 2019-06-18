package krasa.grepconsole.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;

import javax.swing.*;

public class Notifier {
	public static final NotificationGroup NOTIFICATION = new NotificationGroup("Grep Console",
			NotificationDisplayType.BALLOON, true);

	public static void notify_InputAndHighlight(final Project project, String message) {
		final Notification notification = NOTIFICATION.createNotification(
				"GrepConsole plugin: " + message
						+ "\n(More notifications will not be displayed for this console filter. Notification can be disabled at File | Settings | Appearance & Behavior | Notifications`)"
				, MessageType.WARNING);


		SwingUtilities.invokeLater(() -> Notifications.Bus.notify(notification, project));

	}

	public static void notify_GrepFilter(final Project project, String message) {
		final Notification notification = NOTIFICATION.createNotification(
				"GrepConsole plugin: " + message
						+ "\n(More notifications will not be displayed for this console filter. Notification can be disabled at File | Settings | Appearance & Behavior | Notifications`)"
				, MessageType.WARNING);


		SwingUtilities.invokeLater(() -> Notifications.Bus.notify(notification, project));

	}

	public static void notify_MissingExtension(String action, final Project project) {
		final Notification notification = NOTIFICATION.createNotification(
				"GrepConsole : missing script '" + action + "'"
				, MessageType.WARNING);


		SwingUtilities.invokeLater(() -> Notifications.Bus.notify(notification, project));

	}

}
