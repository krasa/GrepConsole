package krasa.grepconsole.model;

import java.util.ArrayList;
import java.util.List;

public class Profile extends DomainObject {
	private long id;
	private boolean defaultProfile;
	private List<GrepExpressionItem> grepExpressionItems = new ArrayList<GrepExpressionItem>();
	private boolean enabledHighlighting = true;
	private boolean enabledInputFiltering = true;
	private String maxLengthToMatch = "40";
	private boolean enableMaxLengthLimit = true;
	private boolean enableAnsi;
	private boolean printAnsi;

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
		return Integer.valueOf(maxLengthToMatch);
	}

	public String getMaxLengthToMatch() {
		return maxLengthToMatch;
	}

	public void setMaxLengthToMatch(String maxLengthToMatch) {
		this.maxLengthToMatch = maxLengthToMatch;
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

	public boolean isEnableAnsi() {
		return enableAnsi;
	}

	public void setEnableAnsi(final boolean enableAnsi) {
		this.enableAnsi = enableAnsi;
	}

	public boolean isPrintAnsi() {
		return printAnsi;
	}

	public void setPrintAnsi(final boolean printAnsi) {
		this.printAnsi = printAnsi;
	}
}
