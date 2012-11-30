package krasa.grepconsole.model;

import java.util.ArrayList;
import java.util.List;


public class Profile extends DomainObject {
	private long id;
	private boolean defaultProfile;
	private List<GrepExpressionItem> grepExpressionItems = new ArrayList<GrepExpressionItem>();
	private boolean enabledHighlighting = true;
	private boolean enabledFiltering = true;
	private Integer maxLengthToMatch = 100;
	private boolean enableMaxLengthLimit = true;

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

	public Integer getMaxLengthToMatch() {
		return maxLengthToMatch;
	}

	public void setMaxLengthToMatch(final Integer maxLengthToMatch) {
		this.maxLengthToMatch = maxLengthToMatch;
	}

	public boolean isEnableMaxLengthLimit() {
		return enableMaxLengthLimit;
	}

	public void setEnableMaxLengthLimit(final boolean enableMaxLengthLimit) {
		this.enableMaxLengthLimit = enableMaxLengthLimit;
	}

	public boolean isEnabledFiltering() {
		return enabledFiltering;
	}

	public void setEnabledFiltering(boolean enabledFiltering) {
		this.enabledFiltering = enabledFiltering;
	}
}
