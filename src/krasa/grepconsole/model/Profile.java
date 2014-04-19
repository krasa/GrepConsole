package krasa.grepconsole.model;

import com.intellij.util.xmlb.annotations.Transient;
import org.apache.commons.lang.math.NumberUtils;

import com.intellij.util.xmlb.annotations.Transient;
import org.apache.commons.lang.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class Profile extends DomainObject {
	public static final String DEFAULT = "60";

	private long id;
	private boolean defaultProfile;
	private List<GrepExpressionItem> grepExpressionItems = new ArrayList<GrepExpressionItem>();
	private boolean enabledHighlighting = true;
	private boolean enabledInputFiltering = true;
	private String maxLengthToMatch = DEFAULT;
	private boolean enableMaxLengthLimit = true;
	private boolean enableAnsiColoring;
	private boolean hideAnsiCommands;
	private boolean encodeText;
	@Transient
	private transient Integer maxLengthToMatchAsInt;
	private boolean multiLineOutput;

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

	public List<GrepExpressionItem> getGrepExpressionItems() {
		return grepExpressionItems;
	}

	public void setGrepExpressionItems(List<GrepExpressionItem> grepExpressionItems) {
		this.grepExpressionItems = grepExpressionItems;
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

	public boolean isEnableAnsiColoring() {
		return enableAnsiColoring;
	}

	public void setEnableAnsiColoring(final boolean enableAnsiColoring) {
		this.enableAnsiColoring = enableAnsiColoring;
	}

	public boolean isHideAnsiCommands() {
		return hideAnsiCommands;
	}

	public void setHideAnsiCommands(final boolean hideAnsiCommands) {
		this.hideAnsiCommands = hideAnsiCommands;
	}

	public boolean isEncodeText() {
		return encodeText;
	}

	public void setEncodeText(boolean encodeText) {
		this.encodeText = encodeText;
	}

	public boolean isMultiLineOutput() {
		return multiLineOutput;
	}

	public void setMultiLineOutput(boolean multiLineOutput) {
		this.multiLineOutput = multiLineOutput;
	}
}
