package krasa.grepconsole.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;

import com.intellij.util.xmlb.annotations.Transient;

public class Profile extends DomainObject {
	public static final String DEFAULT = "120";
	private static final String MAX_PROCESSING_TIME_DEFAULT = "1000";
	private String maxLengthToMatch = DEFAULT;
	private long id;
	private boolean defaultProfile;
	private List<GrepExpressionGroup> grepExpressionGroups = new ArrayList<GrepExpressionGroup>();
	@Deprecated
	private List<GrepExpressionItem> grepExpressionItems = new ArrayList<GrepExpressionItem>();
	private boolean enabledHighlighting = true;
	private boolean enabledInputFiltering = true;
	private boolean enableMaxLengthLimit = true;
	@Transient
	private transient Integer maxLengthToMatchAsInt;
	private boolean multiLineOutput;

	private boolean showStatsInConsoleByDefault;
	private boolean showStatsInStatusBarByDefault;
	private boolean enableFoldings;
	private String maxProcessingTime = MAX_PROCESSING_TIME_DEFAULT;
	@Transient
	private transient Integer maxProcessingTimeAsInt;

	public Profile() {
		id = System.currentTimeMillis();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isDefaultProfile() {
		return defaultProfile;
	}

	public void setDefaultProfile(boolean defaultProfile) {
		this.defaultProfile = defaultProfile;
	}

	public List<GrepExpressionItem> getAllGrepExpressionItems() {
		List<GrepExpressionItem> items = new ArrayList<GrepExpressionItem>();
		for (GrepExpressionGroup group : grepExpressionGroups) {
			items.addAll(group.getGrepExpressionItems());
		}
		return items;
	}

	@Deprecated
	public List<GrepExpressionItem> getGrepExpressionItems() {
		return grepExpressionItems;
	}

	@Deprecated
	public void setGrepExpressionItems(List<GrepExpressionItem> grepExpressionItems) {
		this.grepExpressionItems = grepExpressionItems;
	}

	public List<GrepExpressionGroup> getGrepExpressionGroups() {
		if (grepExpressionGroups.isEmpty() && grepExpressionItems != null && !grepExpressionItems.isEmpty()) {
			GrepExpressionGroup expressionGroup = new GrepExpressionGroup("default");
			expressionGroup.getGrepExpressionItems().addAll(grepExpressionItems);
			grepExpressionItems.clear();
			grepExpressionGroups.add(expressionGroup);
		}
		return grepExpressionGroups;
	}

	public void setGrepExpressionGroups(List<GrepExpressionGroup> grepExpressionGroups) {
		this.grepExpressionGroups = grepExpressionGroups;
	}

	public boolean isEnabledHighlighting() {
		return enabledHighlighting;
	}

	public void setEnabledHighlighting(final boolean enabledHighlighting) {
		this.enabledHighlighting = enabledHighlighting;
	}

	public Integer getMaxLengthToMatchAsInt() {
		if (maxLengthToMatchAsInt == null) {
			maxLengthToMatchAsInt = Integer.valueOf(maxLengthToMatch);
		}
		return maxLengthToMatchAsInt;
	}

	public Integer getMaxProcessingTimeAsInt() {
		if (maxProcessingTimeAsInt == null) {
			maxProcessingTimeAsInt = Integer.valueOf(maxProcessingTime);
		}
		return maxProcessingTimeAsInt;
	}

	public String getMaxLengthToMatch() {
		return maxLengthToMatch;
	}

	public void setMaxLengthToMatch(String maxLengthToMatch) {
		if (maxLengthToMatch == null || maxLengthToMatch.length() == 0) {
			maxLengthToMatch = DEFAULT;
		}
		maxLengthToMatch = maxLengthToMatch.replace("\u00A0", "").replace(" ", "");
		if (maxLengthToMatch.length() == 0 || !NumberUtils.isNumber(maxLengthToMatch)) {
			maxLengthToMatch = DEFAULT;
		}
		this.maxLengthToMatch = maxLengthToMatch;
		maxLengthToMatchAsInt = Integer.valueOf(maxLengthToMatch);
	}

	public boolean isEnableMaxLengthLimit() {
		return enableMaxLengthLimit;
	}

	public void setEnableMaxLengthLimit(final boolean enableMaxLengthLimit) {
		this.enableMaxLengthLimit = enableMaxLengthLimit;
	}

	public boolean isEnabledInputFiltering() {
		return enabledInputFiltering;
	}

	public void setEnabledInputFiltering(boolean enabledInputFiltering) {
		this.enabledInputFiltering = enabledInputFiltering;
	}

	public boolean isMultiLineOutput() {
		return multiLineOutput;
	}

	public void setMultiLineOutput(boolean multiLineOutput) {
		this.multiLineOutput = multiLineOutput;
	}

	public boolean isShowStatsInStatusBarByDefault() {
		return showStatsInStatusBarByDefault;
	}

	public void setShowStatsInStatusBarByDefault(boolean showStatsInStatusBarByDefault) {
		this.showStatsInStatusBarByDefault = showStatsInStatusBarByDefault;
	}

	public boolean isShowStatsInConsoleByDefault() {
		return showStatsInConsoleByDefault;
	}

	public void setShowStatsInConsoleByDefault(boolean showStatsInConsoleByDefault) {
		this.showStatsInConsoleByDefault = showStatsInConsoleByDefault;
	}

	public boolean isEnableFoldings() {
		return enableFoldings;
	}

	public void setEnableFoldings(final boolean enableFoldings) {
		this.enableFoldings = enableFoldings;
	}

	public String getMaxProcessingTime() {
		return maxProcessingTime;
	}

	public void setMaxProcessingTime(String maxProcessingTime) {
		if (maxProcessingTime == null || maxProcessingTime.length() == 0) {
			maxProcessingTime = MAX_PROCESSING_TIME_DEFAULT;
		}
		maxProcessingTime = maxProcessingTime.trim().replace("\u00A0", "").replace(" ", "");
		if (maxProcessingTime.length() == 0 || !NumberUtils.isNumber(maxProcessingTime)) {
			maxProcessingTime = MAX_PROCESSING_TIME_DEFAULT;
		}
		this.maxProcessingTime = maxProcessingTime;
		maxProcessingTimeAsInt = Integer.valueOf(maxProcessingTime);
	}
}
