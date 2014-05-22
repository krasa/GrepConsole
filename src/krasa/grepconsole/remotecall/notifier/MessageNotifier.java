package krasa.grepconsole.remotecall.notifier;

import krasa.grepconsole.remotecall.handler.MessageHandler;

public interface MessageNotifier extends Runnable {

	void addMessageHandler(MessageHandler handler);

}
