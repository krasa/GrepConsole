package krasa.grepconsole.utils;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

public class Notifier {

	public static void notify_InputAndHighlight(final Project project, String message) {
		final Notification notification = getNotificationGroup().createNotification(
				"Grep Console plugin: " + message
						+ "\n(More notifications will not be displayed for this console filter. Notification can be disabled at File | Settings | Appearance & Behavior | Notifications`)"
				, MessageType.WARNING);


		SwingUtilities.invokeLater(() -> Notifications.Bus.notify(notification, project));

	}

	public static void notify_GrepFilter(final Project project, String message) {
		final Notification notification = getNotificationGroup().createNotification(
				"Grep Console plugin: " + message
						+ "\n(More notifications will not be displayed for this console filter. Notification can be disabled at File | Settings | Appearance & Behavior | Notifications`)"
				, MessageType.WARNING);


		SwingUtilities.invokeLater(() -> Notifications.Bus.notify(notification, project));

	}

	public static Set<String> extensions = new HashSet<>();

	public static void notify_MissingExtension(String action, final Project project) {
		if (project != null) {
			if (extensions.add(action)) {
				final Notification notification = NotificationGroupManager.getInstance().getNotificationGroup("Grep Console - Extensions").createNotification(
						"Grep Console : missing script '" + action + "'"
						, MessageType.WARNING);


				SwingUtilities.invokeLater(() -> Notifications.Bus.notify(notification, project));
			}
		}

	}


	public static NotificationGroup getNotificationGroup() {
		return NotificationGroupManager.getInstance().getNotificationGroup("Grep Console");
	}

	public static void notify_BrokenExtension(String action, Project project) {
		if (project != null) {
			if (extensions.add(action)) {
				final Notification notification = NotificationGroupManager.getInstance().getNotificationGroup("Grep Console - Extensions").createNotification(
						"Grep Console : '" + action + "' has wrong class"
						, MessageType.WARNING);


				SwingUtilities.invokeLater(() -> Notifications.Bus.notify(notification, project));
			}
		}
	}
}
