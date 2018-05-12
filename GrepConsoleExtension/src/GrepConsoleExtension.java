import com.intellij.ide.plugins.IdeaPluginDescriptorImpl;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.ui.MessageType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.intellij.openapi.util.text.StringUtil.newBombedCharSequence;

public class GrepConsoleExtension implements ApplicationComponent {

	private static final Logger LOG = Logger.getInstance(GrepConsoleExtension.class);

	public static final NotificationGroup NOTIFICATION = new NotificationGroup("Grep Console Extension",
			NotificationDisplayType.BALLOON, true);

	@Override
	public void initComponent() {
		try {
			registerFunction("extension", new Function<String, String>() {
				Pattern pattern = Pattern.compile(".*ugly slow regexp.*");

				/** - The text will never be empty, it may or may not end with a newline - \n
				 *  - It is possible that the stream will flush prematurely and the text will be incomplete: IDEA-70016
				 *  - Return null to remove the line
				 *  - Processing blocks application output stream, make sure to limit the length and processing time when needed using #limitAndCutNewline
				 **/
				@Override
				public String apply(String text) {
					try {
						CharSequence textForMatching = limitAndCutNewline(text, 150, 1000);

						Matcher matcher = pattern.matcher(textForMatching);
						if (matcher.matches()) {
							return text.toUpperCase();
						}

					} catch (com.intellij.openapi.progress.ProcessCanceledException ex) {
						ApplicationManager.getApplication().invokeLater(() -> {
							Notification notification = NOTIFICATION.createNotification("Extension processing took too long for: " + text, MessageType.WARNING);
							Notifications.Bus.notify(notification);
						});
					}
					return text;
				}
			});
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	@Override
	public void disposeComponent() {

	}

	@NotNull
	@Override
	public String getComponentName() {
		return "GrepConsoleExtension";
	}

	/**
	 * reflection for easier setup
	 */
	static void registerFunction(String functionName, Function<String, String> function) {
		try {
			IdeaPluginDescriptorImpl descriptor = (IdeaPluginDescriptorImpl) PluginManager.getPlugin(PluginId.getId("GrepConsole"));
			Class<?> clazz = descriptor.getPluginClassLoader().loadClass("krasa.grepconsole.plugin.ExtensionManager");

			clazz.getMethod("registerFunction", String.class, Function.class)
					.invoke(null, functionName, function);

			LOG.info("'" + functionName + "' registered");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}


	static CharSequence limitAndCutNewline(String text, int maxLength, int milliseconds) {
		int endIndex = text.length();
		if (text.endsWith("\n")) {
			--endIndex;
		}
		if (maxLength >= 0) {
			endIndex = Math.min(endIndex, maxLength);
		}
		String substring = text.substring(0, endIndex);

		if (milliseconds > 0) {
			return newBombedCharSequence(substring, milliseconds);
		}
		return substring;
	}


}
