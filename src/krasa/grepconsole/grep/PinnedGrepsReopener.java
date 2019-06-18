package krasa.grepconsole.grep;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.impl.RunnerLayoutUiImpl;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AppUIUtil;
import com.intellij.ui.content.Content;
import com.intellij.util.Alarm;
import com.intellij.util.SingleAlarm;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static krasa.grepconsole.grep.PinnedGrepConsolesState.RunConfigurationRef.toKey;

public class PinnedGrepsReopener {
	private static final Logger LOG = Logger.getInstance(PinnedGrepsReopener.class);

	private final SingleAlarm myUpdateAlarm;
	public static volatile boolean enabled = true;

	public PinnedGrepsReopener(Project project, WeakReference<ConsoleView> consoleViewWeakReference) {
		AtomicInteger atomicInteger = new AtomicInteger();
		myUpdateAlarm = new SingleAlarm(new Runnable() {
			@Override
			public void run() {
				ConsoleView consoleView = consoleViewWeakReference.get();
				if (consoleView == null) {
					return;
				}
				if (project.isDisposed()) {
					return;
				}
				AppUIUtil.invokeOnEdt(() -> {
					if (project.isDisposed()) {
						return;
					}


					RunContentDescriptor runContentDescriptor = OpenGrepConsoleAction.getRunContentDescriptor(project, consoleView);

					if (runContentDescriptor != null) {
						PinnedGrepConsolesState.RunConfigurationRef key = toKey(runContentDescriptor);
						PinnedGrepConsolesState.Pins state = PinnedGrepConsolesState.getInstance(project).getPins(key);

						if (state != null && !state.getPins().isEmpty()) {
							if (project.isDisposed()) {
								return;
							}
							RunnerLayoutUi runnerLayoutUi = OpenGrepConsoleAction.getRunnerLayoutUi(project, runContentDescriptor, consoleView);
							if (runnerLayoutUi == null) {
								if (atomicInteger.incrementAndGet() < 6) {
									myUpdateAlarm.cancelAndRequest();
								} else {
									LOG.warn("runnerLayoutUi == null for " + key + ", aborting reopening of pins " + state);
								}
								return;
							}

							try {
								enabled = false;


								Content[] contents = runnerLayoutUi.getContents();
								for (Content content : contents) {
									if (OpenGrepConsoleAction.isSameConsole(content, consoleView)) {
										String contentType = RunnerLayoutUiImpl.CONTENT_TYPE.get(content);

										List<PinnedGrepConsolesState.Pin> list = state.getPins();
										for (PinnedGrepConsolesState.Pin pin : list) {
											if (pin.getParentConsoleUUID() == null && Objects.equals(contentType, pin.getContentType())) {
												initConsole(pin, key, consoleView, list);
											}
										}
									}
								}
							} finally {
								enabled = true;
							}
						}
					} else if (atomicInteger.incrementAndGet() < 3) {
						myUpdateAlarm.cancelAndRequest();
					}
				});
			}


			public void initConsole(PinnedGrepConsolesState.Pin pin, PinnedGrepConsolesState.RunConfigurationRef key, ConsoleView parent, List<PinnedGrepConsolesState.Pin> list) {
				if (LOG.isDebugEnabled()) {
					LOG.debug(">initConsole " + "pin = [" + pin + "], parent = [" + parent.hashCode() + "], list = [" + list + "]");
				}
				ConsoleViewImpl foo = new OpenGrepConsoleAction().createGrepConsole(project, key, parent, pin.getGrepModel(), null, pin.getConsoleUUID(), pin.getContentType());
				for (PinnedGrepConsolesState.Pin childPin : list) {
					if (pin.getConsoleUUID().equals(childPin.getParentConsoleUUID())) {
						initConsole(childPin, key, foo, list);
					}
				}
			}

		}, 100, Alarm.ThreadToUse.POOLED_THREAD, project);
		myUpdateAlarm.request();
	}

}