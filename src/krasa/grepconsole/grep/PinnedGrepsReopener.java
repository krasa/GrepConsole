package krasa.grepconsole.grep;

import com.intellij.execution.ExecutionHelper;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.util.Alarm;
import com.intellij.util.SingleAlarm;

import javax.swing.*;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PinnedGrepsReopener {
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

				Collection<RunContentDescriptor> descriptors =
						ExecutionHelper.findRunningConsole(project, dom -> isSameConsole(dom, consoleView));

				if (!descriptors.isEmpty()) {
					if (descriptors.size() == 1) {
						RunContentDescriptor runContentDescriptor = (RunContentDescriptor) descriptors.toArray()[0];
						PinnedGrepConsolesState.RunConfigurationRef key = new PinnedGrepConsolesState.RunConfigurationRef(runContentDescriptor.getDisplayName(), runContentDescriptor.getIcon());
						PinnedGrepConsolesState.Pins state = PinnedGrepConsolesState.getInstance(project).getPins(key);
						if (state != null && !state.getPins().isEmpty() && consoleView instanceof ConsoleViewImpl) {
							try {
								SwingUtilities.invokeAndWait(() -> {
									if (project.isDisposed()) {
										return;
									}
									try {
										enabled = false;
										List<PinnedGrepConsolesState.Pin> list = state.getPins();
										for (PinnedGrepConsolesState.Pin pin : list) {
											if (pin.getParentConsoleUUID() == null) {
												initConsole(pin, (ConsoleViewImpl) consoleView, list);
											}
										}
									} finally {
										enabled = true;
									}
								});
							} catch (InterruptedException | InvocationTargetException ignored) {
							}
						}
					}
				} else if (atomicInteger.incrementAndGet() < 3) {
					myUpdateAlarm.cancelAndRequest();
				}
			}

			public void initConsole(PinnedGrepConsolesState.Pin pin, ConsoleViewImpl parent, List<PinnedGrepConsolesState.Pin> list) {
				ConsoleViewImpl foo = new OpenGrepConsoleAction().createGrepConsole(project, parent, pin.getGrepModel(), null, pin.getConsoleUUID());
				for (PinnedGrepConsolesState.Pin childPin : list) {
					if (pin.getConsoleUUID().equals(childPin.getParentConsoleUUID())) {
						initConsole(childPin, foo, list);
					}
				}
			}

			public boolean isSameConsole(RunContentDescriptor dom, ExecutionConsole consoleView) {
				ExecutionConsole executionConsole = dom.getExecutionConsole();
				if (executionConsole instanceof BaseTestsOutputConsoleView) {
					executionConsole = ((BaseTestsOutputConsoleView) executionConsole).getConsole();
				}
				return executionConsole == consoleView;
			}
		}, 100, Alarm.ThreadToUse.POOLED_THREAD, project);
		myUpdateAlarm.request();
	}


}