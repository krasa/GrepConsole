package krasa.grepconsole.filter;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.filter.support.FilterState;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Operation;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.utils.Notifier;
import krasa.grepconsole.utils.Utils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMatchingFilter extends AbstractFilter {
	private static final Logger log = Logger.getInstance(AbstractMatchingFilter.class);
	                                
	protected volatile List<GrepProcessor> grepProcessors;
	protected boolean showLimitNotification = true;

	public AbstractMatchingFilter(@NotNull Project project, @NotNull Profile profile) {
		super(project, profile);
		initProcessors();
	}

	public AbstractMatchingFilter(@NotNull Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile);
		this.grepProcessors = grepProcessors;
	}

	protected final FilterState filter(@Nullable String text, int offset) {
		// line can be empty sometimes under heavy load
		if (!StringUtils.isEmpty(text) && !grepProcessors.isEmpty()) {
			String substring = profile.limitInputLength_andCutNewLine(text);
			CharSequence charSequence = profile.limitProcessingTime(substring);

			FilterState state = new FilterState(offset, text, profile, charSequence, project);
			for (GrepProcessor grepProcessor : grepProcessors) {
				try {
					state = grepProcessor.process(state);
					if (!continueFiltering(state))
						return state;
				} catch (ProcessCanceledException e) {
					String message = "processing took too long, aborting to prevent GUI freezing.\n"
							+ "Consider changing following settings: 'Match only first N characters on each line' or 'Max processing time for a line'\n"
							+ "Last expression: [" + grepProcessor + "]\n" + "Line: " + Utils.toNiceLineForLog(substring);
					if (showLimitNotification) {
						showLimitNotification = false;
						Notifier.notify_InputAndHighlight(project, message);
					} else {
						log.warn(message);
					}
					break;
				} catch (Exception e) {
					throw new RuntimeException(grepProcessor.getGrepExpressionItem().toString(), e);
				}
			}

			return state;
		}
		return null;
	}


	protected boolean continueFiltering(FilterState state) {
		return state.getNextOperation() != Operation.EXIT;
	}


	protected void initProcessors() {
		grepProcessors = new ArrayList<>();
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

	public void setProfile(@NotNull Profile profile) {
		this.profile = profile;
		onChange();
	}
}
