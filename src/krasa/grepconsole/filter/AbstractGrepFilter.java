package krasa.grepconsole.filter;

import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import krasa.grepconsole.filter.support.FilterState;
import krasa.grepconsole.filter.support.GrepProcessor;
import krasa.grepconsole.model.GrepExpressionItem;
import krasa.grepconsole.model.Operation;
import krasa.grepconsole.model.Profile;
import krasa.grepconsole.utils.Notifier;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractGrepFilter extends AbstractFilter {

	protected volatile List<GrepProcessor> grepProcessors;
	private boolean showLimitNotification = true;

	public AbstractGrepFilter(Project project) {
		super(project);
		initProcessors();
	}

	public AbstractGrepFilter(Profile profile, List<GrepProcessor> grepProcessors) {
		super(profile);
		this.grepProcessors = grepProcessors;
	}

	protected final FilterState filter(@Nullable String text, int offset) {
		// line can be empty sometimes under heavy load
		if (!StringUtils.isEmpty(text) && !grepProcessors.isEmpty()) {
			String substring = profile.limitInputLength_andCutNewLine(text);
			CharSequence charSequence = profile.limitProcessingTime(substring);

			FilterState state = new FilterState(offset, charSequence);
			for (GrepProcessor grepProcessor : grepProcessors) {
				try {
					state = grepProcessor.process(state);
					if (!continueFiltering(state))
						return state;
				} catch (ProcessCanceledException e) {
					if (showLimitNotification) {
						showLimitNotification = false;
						Notifier.notify_InputAndHighlight(substring, grepProcessor, project);
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

	public void setProfile(Profile profile) {
		this.profile = profile;
		initProcessors();
	}
}
