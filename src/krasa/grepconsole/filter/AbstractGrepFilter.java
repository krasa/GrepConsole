package krasa.grepconsole.filter;

import java.util.ArrayList;
import java.util.List;

import krasa.grepconsole.filter.support.FilterState;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Operation;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.plugin.GrepConsoleApplicationComponent;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;

public abstract class AbstractGrepFilter extends AbstractFilter {

	protected List<GrepProcessor> grepProcessors;
	boolean showLimitNotification = true;

	public AbstractGrepFilter(Project project) {
		super(project);
		initProcessors();
	}

	public AbstractGrepFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile);
		this.grepProcessors = grepProcessors;
	}

	protected FilterState filter(@Nullable String text, int offset) {
		// line can be empty sometimes under heavy load
		if (!StringUtils.isEmpty(text) && !grepProcessors.isEmpty()) {
			String substring = getSubstring(text);
			FilterState state = new FilterState(substring, offset, profile.getMaxProcessingTimeAsInt());
			for (GrepProcessor grepProcessor : grepProcessors) {
				try {
					state = grepProcessor.process(state);
					if (!continueFiltering(state))
						return state;
				} catch (ProcessCanceledException e) {
					if (showLimitNotification) {
						showLimitNotification = false;
						int length = substring.length();
						int endIndex = substring.length();
						substring = substring.substring(0, Math.min(endIndex, 120)) + " [length=" + length + "]";
						final Notification notification = GrepConsoleApplicationComponent.NOTIFICATION.createNotification(
								"Grep Console plugin: processing took too long, aborting to prevent GUI freezing.\n"
										+ "Consider changing following settings: 'Match only first N characters on each line' or 'Max processing time for a line'\n"
										+ "Last expression: [" + grepProcessor + "]\n" + "Line: " + substring
										+ "\n(More notifications will not be displayed for this console. Notification can be disabled at File | Settings | Appearance & Behavior | Notifications`)"

								, MessageType.WARNING);
						ApplicationManager.getApplication().invokeLater(new Runnable() {
							@Override
							public void run() {
								Notifications.Bus.notify(notification, project);
							}
						});
					}
					break;
				}
			}

			return state;
		}
		return null;
	}

	protected boolean continueFiltering(FilterState state) {
		return state.getNextOperation() != Operation.EXIT;
	}

	// for higlighting, it always ends with \n, but for input filtering it does not
	private String getSubstring(String text) {
		int endIndex = text.length();
		if (text.endsWith("\n")) {
			--endIndex;
		}
		if (profile.isEnableMaxLengthLimit()) {
			endIndex = Math.min(endIndex, profile.getMaxLengthToMatchAsInt());
		}
		return text.substring(0, endIndex);
	}

	protected void initProcessors() {
		grepProcessors = new ArrayList<GrepProcessor>();
		for (GrepExpressionItem grepExpressionItem : profile.getAllGrepExpressionItems()) {
			if (shouldAdd(grepExpressionItem)) {
				grepProcessors.add(createProcessor(grepExpressionItem));
			}
		}
	}

	protected GrepProcessor createProcessor(GrepExpressionItem grepExpressionItem) {
		return grepExpressionItem.createProcessor();
	}

	public List<GrepProcessor> getGrepProcessors() {
		return grepProcessors;
	}

	abstract protected boolean shouldAdd(GrepExpressionItem item);

	@Override
	public void onChange() {
		super.onChange();
		initProcessors();
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
		initProcessors();
	}
}
