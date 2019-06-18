package krasa.grepconsole.tail.remotecall;

import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import krasa.grepconsole.model.TailSettings;
import krasa.grepconsole.tail.remotecall.handler.OpenFileInConsoleMessageHandler;
import krasa.grepconsole.tail.remotecall.notifier.MessageNotifier;
import krasa.grepconsole.tail.remotecall.notifier.SocketMessageNotifier;
import krasa.grepconsole.utils.Notifier;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class RemoteCallService implements Disposable {
	private static final Logger log = Logger.getInstance(RemoteCallService.class);

	private ServerSocket serverSocket;
	private Thread listenerThread;


	public static RemoteCallService getInstance() {
		return ServiceManager.getService(RemoteCallService.class);
	}


	public boolean rebind(final TailSettings tailSettings) {
		dispose();
		if (!tailSettings.isEnabled()) {
			return false;
		}
		try {
			int port = Integer.parseInt(tailSettings.getPort());
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("localhost", port));
			log.info("Listening " + port);
		} catch (final Exception e) {
			SwingUtilities.invokeLater(() -> Notifications.Bus.notify(Notifier.NOTIFICATION.createNotification("GrepConsole plugin - Tail integration", "Can't bind port " + tailSettings.getPort()
					+ ". </br>Error: " + e.toString(), NotificationType.WARNING, null)));
			;
			log.info("GrepConsole Plugin Error", e);
			return false;
		}

		MessageNotifier messageNotifier = new SocketMessageNotifier(serverSocket);
		messageNotifier.addMessageHandler(new OpenFileInConsoleMessageHandler());
		listenerThread = new Thread(messageNotifier);
		listenerThread.start();
		return true;
	}

	@Override
	public void dispose() {
		try {
			if (listenerThread != null) {
				listenerThread.interrupt();
				listenerThread = null;
			}
			if (serverSocket != null) {
				serverSocket.close();
				serverSocket = null;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
}
