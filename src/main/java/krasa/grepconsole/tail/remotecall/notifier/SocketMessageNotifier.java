package krasa.grepconsole.tail.remotecall.notifier;

import com.google.common.io.Closeables;
import com.intellij.openapi.diagnostic.Logger;
import krasa.grepconsole.tail.remotecall.handler.MessageHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static java.net.URLDecoder.decode;

public class SocketMessageNotifier implements MessageNotifier {

	private static final Logger log = Logger.getInstance(SocketMessageNotifier.class);
	private static final String CRLF = "\r\n";
	private static final String NL = "\n";
	private final Collection<MessageHandler> messageHandlers = new HashSet<>();
	private final ServerSocket serverSocket;

	public SocketMessageNotifier(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	private static Map<String, String> getParametersFromUrl(String url) {
		String parametersString = url.substring(url.indexOf('?') + 1);
		Map<String, String> parameters = new HashMap<>();
		StringTokenizer tokenizer = new StringTokenizer(parametersString, "&");
		while (tokenizer.hasMoreElements()) {
			String[] parametersPair = tokenizer.nextToken().split("=", 2);
			if (parametersPair.length > 1) {
				parameters.put(parametersPair[0], parametersPair[1]);
			}
		}

		return parameters;
	}

	@Override
	public void addMessageHandler(MessageHandler handler) {
		messageHandlers.add(handler);
	}

	@Override
	public void run() {
		while (true) {
			Socket clientSocket;
			try {
				// noinspection SocketOpenedButNotSafelyClosed
				clientSocket = serverSocket.accept();
			} catch (IOException e) {
				if (serverSocket.isClosed()) {
					break;
				} else {
					log.error("Error while accepting", e);
					continue;
				}
			}

			InputStream inputStream = null;
			try {
				inputStream = clientSocket.getInputStream();
			} catch (IOException e) {
				log.error(e);
			}
			if (inputStream != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
				try {
					String inputLine, requestString = "";

					while ((inputLine = in.readLine()) != null && !inputLine.equals(CRLF) && !inputLine.equals(NL)
							&& !inputLine.isEmpty()) {
						requestString += inputLine;
					}
					clientSocket.getOutputStream().write(("HTTP/1.1 200 OK" + CRLF + CRLF).getBytes());
					clientSocket.close();

					StringTokenizer tokenizer = new StringTokenizer(requestString);
					String method = tokenizer.hasMoreElements() ? tokenizer.nextToken() : "";
					if (!method.equals("GET")) {
						log.warn("Only GET requests allowed");
						continue;
					}

					log.info("Received request " + requestString);
					Map<String, String> parameters = getParametersFromUrl(tokenizer.nextToken());

					String message = parameters.get("message") != null ? decode(parameters.get("message").trim(),
							"UTF-8") : "";

					log.info("Received message " + message);
					handleMessage(message);
				} catch (IOException e) {
					log.error("Error", e);
				} finally {
					// noinspection deprecation
					Closeables.closeQuietly(in);
				}
			}
		}
	}

	private void handleMessage(String message) {
		for (MessageHandler handler : messageHandlers) {
			handler.handleMessage(message);
		}
	}
}
