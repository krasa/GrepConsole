package krasa.grepconsole.tail.remotecall.notifier;

import krasa.grepconsole.tail.remotecall.handler.MessageHandler;

public interface MessageNotifier extends Runnable {

	void addMessageHandler(MessageHandler handler);

}
