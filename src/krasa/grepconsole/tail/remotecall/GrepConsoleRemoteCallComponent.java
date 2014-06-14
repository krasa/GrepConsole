package krasa.grepconsole.tail.remotecall;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import krasa.grepconsole.model.TailSettings;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;
import krasa.grepconsole.tail.remotecall.handler.OpenFileInConsoleMessageHandler;
import krasa.grepconsole.tail.remotecall.notifier.MessageNotifier;
import krasa.grepconsole.tail.remotecall.notifier.SocketMessageNotifier;

import org.jetbrains.annotations.NotNull;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;

public class GrepConsoleRemoteCallComponent implements ApplicationComponent {
	private static final Logger log = Logger.getInstance(GrepConsoleRemoteCallComponent.class);

	private ServerSocket serverSocket;
	private Thread listenerThread;

	public void initComponent() {
		final TailSettings tailSettings = GrepConsoleApplicationComponent.getInstance().getState().getTailSettings();
		rebind(tailSettings);
	}

	public boolean rebind(final TailSettings tailSettings) {
		disposeComponent();
		if (!tailSettings.isEnabled()) {
			return false;
		}
		try {
			int port = Integer.parseInt(tailSettings.getPort());
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress("localhost", port));
			log.info("Listening " + port);
		} catch (final Exception e) {
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				public void run() {
					Messages.showMessageDialog(
							"Can't bind port " + tailSettings.getPort()
									+ ". GrepConsole plugin external integration for Tail File won't work. error: "
									+ e.toString(), "GrepConsole Plugin Error", Messages.getErrorIcon());
				}
			});
			log.info("GrepConsole Plugin Error", e);
			return false;
		}

		MessageNotifier messageNotifier = new SocketMessageNotifier(serverSocket);
		messageNotifier.addMessageHandler(new OpenFileInConsoleMessageHandler());
		listenerThread = new Thread(messageNotifier);
		listenerThread.start();
		return true;
	}

	public void disposeComponent() {
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

	@NotNull
	public String getComponentName() {
		return "GrepConsoleRemoteCallComponent";
	}

	public static GrepConsoleRemoteCallComponent getInstance() {
		return ApplicationManager.getApplication().getComponent(GrepConsoleRemoteCallComponent.class);
	}

}
