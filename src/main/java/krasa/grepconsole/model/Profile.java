package krasa.grepconsole.model;

import com.intellij.ide.ui.LafManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Transient;
import krasa.grepconsole.Cloner;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static krasa.grepconsole.plugin.DefaultState.getDefaultProfile;

public class Profile extends DomainObject implements Cloneable {
	public static final String DEFAULT = "200";
	public static final String DEFAULT_GREP = "1000";
	private static final String MAX_PROCESSING_TIME_DEFAULT = "1000";
	public static final String DARK = "@Dark Theme@";
	public static final String LIGHT = "@Light Theme@";

	private String maxLengthToMatch = DEFAULT;
	private long id;
	private boolean defaultProfile;
	/**
	 * for highlighting and folding
	 */
	private List<GrepExpressionGroup> grepExpressionGroups = new ArrayList<>();
	private List<GrepExpressionGroup> inputFilterGroups = new ArrayList<>();
	private boolean enabledHighlighting = true;
	private boolean enabledInputFiltering = true;
	private boolean enableMaxLengthLimit = true;
	@Transient
	private transient Integer maxLengthToMatchAsInt;
	private boolean multiLineOutput;
	private boolean multilineInputFilter;

	private boolean showStatsInConsoleByDefault;
	private boolean showStatsInStatusBarByDefault;
	private boolean enableFoldings;
	private String maxProcessingTime = MAX_PROCESSING_TIME_DEFAULT;
	@Transient
	private transient Integer maxProcessingTimeAsInt;
	private boolean filterOutBeforeGrep;
	private boolean alwaysPinGrepConsoles = true;
	private String maxLengthToGrep = DEFAULT_GREP;
	@Transient
	private transient Integer maxLengthToGrepAsInt;
	private boolean enableMaxLengthGrepLimit = true;
	private String name;
	private boolean testHighlightersInInputFilter;
	private boolean inputFilterBlankLineWorkaround = true;
	private boolean bufferStreams;

	// for higlighting, it always ends with \n, but for input filtering it does not
	@NotNull
	public String limitInputLength_andCutNewLine(@NotNull String text) {
		int endIndex = text.length();
		if (text.endsWith("\n")) {
			--endIndex;
		}
		if (this.isEnableMaxLengthLimit()) {
			endIndex = Math.min(endIndex, this.getMaxLengthToMatchAsInt());
		}
		return text.substring(0, endIndex);
	}

	@NotNull
	public String limitInputGrepLength_andCutNewLine(@NotNull String text) {
		int endIndex = text.length();
		if (text.endsWith("\n")) {
			--endIndex;
		}
		if (this.isEnableMaxLengthGrepLimit()) {
			endIndex = Math.min(endIndex, this.getMaxLengthToGrepAsInt());
		}
		return text.substring(0, endIndex);
	}

	@NotNull
	public CharSequence limitProcessingTime(String substring) {
		return StringUtil.newBombedCharSequence(substring, this.getMaxProcessingTimeAsInt());
	}


	public Profile() {
		id = System.currentTimeMillis();
	}

	public Profile(String name) {
		this.name = name;
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
		List<GrepExpressionItem> items = new ArrayList<>();
		for (GrepExpressionGroup group : grepExpressionGroups) {
			String name = group.getName();
			if (DARK.equals(group.getName())) {
				if (UIUtil.isUnderDarcula()) {
					items.addAll(group.getGrepExpressionItems());
				}
			} else if (LIGHT.equals(group.getName())) {
				if (!UIUtil.isUnderDarcula()) {
					items.addAll(group.getGrepExpressionItems());
				}
			} else if (name.startsWith("@") && name.endsWith("@")) {
				String themeName = LafManager.getInstance().getCurrentLookAndFeel().getName();
				if (themeName.equalsIgnoreCase(name.substring(1, name.length() - 1))) {
					items.addAll(group.getGrepExpressionItems());
				}
			} else {
				items.addAll(group.getGrepExpressionItems());
			}
		}
		return items;
	}

	public List<GrepExpressionItem> getAllInputFilterExpressionItems() {
		List<GrepExpressionItem> items = new ArrayList<>();
		for (GrepExpressionGroup group : inputFilterGroups) {
			items.addAll(group.getGrepExpressionItems());
		}
		return items;
	}


	public List<GrepExpressionGroup> getGrepExpressionGroups() {
		if (grepExpressionGroups.isEmpty()) {
			GrepExpressionGroup expressionGroup = new GrepExpressionGroup("default");
			grepExpressionGroups.add(expressionGroup);
		}
		return grepExpressionGroups;
	}

	public List<GrepExpressionGroup> getInputFilterGroups() {
		if (inputFilterGroups.isEmpty()) {
			GrepExpressionGroup expressionGroup = new GrepExpressionGroup("default");
			inputFilterGroups.add(expressionGroup);
		}
		return inputFilterGroups;
	}

	public void setGrepExpressionGroups(List<GrepExpressionGroup> grepExpressionGroups) {
		this.grepExpressionGroups = grepExpressionGroups;
	}

	public void setInputFilterGroups(List<GrepExpressionGroup> inputFilterGroups) {
		this.inputFilterGroups = inputFilterGroups;
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

	public Integer getMaxLengthToGrepAsInt() {
		if (maxLengthToGrepAsInt == null) {
			maxLengthToGrepAsInt = Integer.valueOf(maxLengthToGrep);
		}
		return maxLengthToGrepAsInt;
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
		maxLengthToMatch = normalizeInt(maxLengthToMatch, DEFAULT);
		this.maxLengthToMatch = maxLengthToMatch;
		maxLengthToMatchAsInt = Integer.valueOf(maxLengthToMatch);
	}

	@NotNull
	public String normalizeInt(String maxLengthToMatch, String aDefault) {
		if (maxLengthToMatch == null || maxLengthToMatch.length() == 0) {
			maxLengthToMatch = aDefault;
		}
		maxLengthToMatch = normalize(maxLengthToMatch);
		if (maxLengthToMatch.length() == 0 || !NumberUtils.isNumber(maxLengthToMatch)) {
			maxLengthToMatch = aDefault;
		}
		return maxLengthToMatch;
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

	public boolean isMultilineInputFilter() {
		return multilineInputFilter;
	}

	public void setMultilineInputFilter(boolean multilineInputFilter) {
		this.multilineInputFilter = multilineInputFilter;
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
		maxProcessingTime = normalizeInt(maxProcessingTime, MAX_PROCESSING_TIME_DEFAULT);
		this.maxProcessingTime = maxProcessingTime;
		maxProcessingTimeAsInt = Integer.valueOf(maxProcessingTime);
	}

	protected String normalize(String s) {
		return s.trim().replaceAll("[\u00A0 ,.]", "");
	}


	public boolean isFilterOutBeforeGrep() {
		return filterOutBeforeGrep;
	}

	public void setFilterOutBeforeGrep(final boolean filterOutBeforeGrep) {
		this.filterOutBeforeGrep = filterOutBeforeGrep;
	}

	public boolean isAlwaysPinGrepConsoles() {
		return alwaysPinGrepConsoles;
	}

	public void setAlwaysPinGrepConsoles(final boolean alwaysPinGrepConsoles) {
		this.alwaysPinGrepConsoles = alwaysPinGrepConsoles;
	}

	public String getMaxLengthToGrep() {
		return maxLengthToGrep;
	}

	public void setMaxLengthToGrep(String maxLengthToGrep) {
		maxLengthToGrep = normalizeInt(maxLengthToGrep, DEFAULT_GREP);
		this.maxLengthToGrep = maxLengthToGrep;
		maxLengthToGrepAsInt = Integer.valueOf(maxLengthToGrep);
	}

	public boolean isEnableMaxLengthGrepLimit() {
		return enableMaxLengthGrepLimit;
	}

	public void setEnableMaxLengthGrepLimit(final boolean enableMaxLengthGrepLimit) {
		this.enableMaxLengthGrepLimit = enableMaxLengthGrepLimit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPresentableName() {
		if (StringUtils.isBlank(name)) {
			if (defaultProfile) {
				name = "default";
			} else {
				name = "new";
			}
		}
		return name;
	}

	@Override
	public Profile clone() {
		return Cloner.deepClone(this);
	}

	@Override
	public String toString() {
		return "Profile{" +
				"id=" + id +
				", defaultProfile=" + defaultProfile +
				", name='" + name + '\'' +
				"}";
	}

	public void resetToDefault() {
		String name = this.name;
		long id = this.id;
		boolean defaultProfile = this.defaultProfile;

		XmlSerializerUtil.copyBean(getDefaultProfile(), this);

		this.setName(name);
		this.setId(id);
		this.setDefaultProfile(defaultProfile);

	}

	@Transient
	public GrepExpressionGroup getOrCreateInputFilterGroup(String name) {
		List<GrepExpressionGroup> inputFilterGroups = getInputFilterGroups();
		for (GrepExpressionGroup inputFilterGroup : inputFilterGroups) {
			if (inputFilterGroup == null) {
				continue;
			}
			if (Objects.equals(name, inputFilterGroup.getName())) {
				return inputFilterGroup;
			}
		}
		GrepExpressionGroup grepExpressionGroup = new GrepExpressionGroup(name);
		inputFilterGroups.add(grepExpressionGroup);
		return grepExpressionGroup;
	}

	public boolean isTestHighlightersInInputFilter() {
		return testHighlightersInInputFilter;
	}

	public void setTestHighlightersInInputFilter(final boolean testHighlightersInInputFilter) {
		this.testHighlightersInInputFilter = testHighlightersInInputFilter;
	}

	public boolean isInputFilterBlankLineWorkaround() {
		return inputFilterBlankLineWorkaround;
	}

	public void setInputFilterBlankLineWorkaround(final boolean inputFilterBlankLineWorkaround) {
		this.inputFilterBlankLineWorkaround = inputFilterBlankLineWorkaround;
	}

	public boolean isBufferStreams() {
		return bufferStreams;
	}

	public void setBufferStreams(final boolean bufferStreams) {
		this.bufferStreams = bufferStreams;
	}

	public String getPresentablename2() {
		String name = getPresentableName();
		if (!"default".equals(name) && isDefaultProfile()) {
			name += " (default)";
		}
		return name;
	}
}
