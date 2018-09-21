package krasa.grepconsole.grep;

import com.intellij.execution.ExecutionHelper;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AppUIUtil;
import com.intellij.util.Alarm;
import com.intellij.util.SingleAlarm;
import krasa.grepconsole.utils.FocusUtils;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
					
					Collection<RunContentDescriptor> descriptors = ExecutionHelper.findRunningConsole(project,
							dom -> FocusUtils.isSameConsole(dom, consoleView, false));

					if (!descriptors.isEmpty()) {
						if (descriptors.size() == 1) {
							RunContentDescriptor runContentDescriptor = (RunContentDescriptor) descriptors.toArray()[0];
							PinnedGrepConsolesState.RunConfigurationRef key = new PinnedGrepConsolesState.RunConfigurationRef(
									runContentDescriptor.getDisplayName(), runContentDescriptor.getIcon());
							PinnedGrepConsolesState.Pins state = PinnedGrepConsolesState.getInstance(project).getPins(key);

							if (state != null && !state.getPins().isEmpty() && consoleView instanceof ConsoleViewImpl) {
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

							}
						}
					} else if (atomicInteger.incrementAndGet() < 3) {
						myUpdateAlarm.cancelAndRequest();
					}
				});
			}

			public void initConsole(PinnedGrepConsolesState.Pin pin, ConsoleViewImpl parent, List<PinnedGrepConsolesState.Pin> list) {
				if (LOG.isDebugEnabled()) {
					LOG.debug(">initConsole " + "pin = [" + pin + "], parent = [" + parent.hashCode() + "], list = [" + list + "]");
				}
				ConsoleViewImpl foo = new OpenGrepConsoleAction().createGrepConsole(project, parent, pin.getGrepModel(), null, pin.getConsoleUUID());
				for (PinnedGrepConsolesState.Pin childPin : list) {
					if (pin.getConsoleUUID().equals(childPin.getParentConsoleUUID())) {
						initConsole(childPin, foo, list);
					}
				}
			}

		}, 100, Alarm.ThreadToUse.POOLED_THREAD, project);
		myUpdateAlarm.request();
	}

}