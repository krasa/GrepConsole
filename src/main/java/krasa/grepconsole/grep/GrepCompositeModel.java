package krasa.grepconsole.grep;

import com.intellij.util.xmlb.annotations.Transient;
import krasa.grepconsole.Cloner;
import krasa.grepconsole.grep.actions.OpenGrepConsoleAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GrepCompositeModel {
	List<GrepModel> models = new ArrayList<GrepModel>();
	String customTitle;
	GrepBeforeAfterModel beforeAfterModel = new GrepBeforeAfterModel();

	public GrepCompositeModel() {
	}

	public GrepBeforeAfterModel getBeforeAfterModel() {
		if (beforeAfterModel == null) {
			beforeAfterModel = new GrepBeforeAfterModel();
		}
		return beforeAfterModel;
	}

	public void setBeforeAfterModel(GrepBeforeAfterModel beforeAfterModel) {
		this.beforeAfterModel = Cloner.deepClone(beforeAfterModel);
	}

	public GrepCompositeModel(GrepModel selectedItem) {
		add(selectedItem);
	}

	public String getCustomTitle() {
		return customTitle;
	}

	public void setCustomTitle(String customTitle) {
		this.customTitle = customTitle;
	}

	public List<GrepModel> getModels() {
		return models;
	}

	public void setModels(List<GrepModel> models) {
		this.models = models;
	}

	public void add(GrepModel grepModel) {
		models.add(grepModel);
	}

	@Transient
	public String getTitle() {
		if (customTitle != null) {
			return customTitle;
		}
		return OpenGrepConsoleAction.title(getFullTitle());
	}

	@Transient
	public String getFullTitle() {
		if (customTitle != null) {
			return customTitle;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < models.size(); i++) {
			GrepModel grepModel = models.get(i);
			if (grepModel.isExclude()) {
				sb.append("-");
			} else {
				if (i != 0) {
					sb.append("+");
				}
			}
			sb.append(grepModel.getExpression());
		}
		String expression = sb.toString();
		if (expression.equals("")) {
			expression = "---";
		}
		return expression;
	}


	public boolean matches(CharSequence charSequence) {
		boolean hasExcludingModel = false;
		boolean hasIncludingModel = false;

		for (GrepModel grepModel : models) {
			if (grepModel.isExclude()) {
				hasExcludingModel = true;
				if (grepModel.matches(charSequence)) {
					return false;
				}
			}
		}
		for (GrepModel grepModel : models) {
			if (!grepModel.isExclude()) {
				hasIncludingModel = true;
				if (grepModel.matches(charSequence)) {
					return true;
				}
			}
		}

		if (hasExcludingModel && !hasIncludingModel) {
			return true;
		}

		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GrepCompositeModel that = (GrepCompositeModel) o;
		return Objects.equals(models, that.models) && Objects.equals(customTitle, that.customTitle) && Objects.equals(beforeAfterModel, that.beforeAfterModel);
	}

	@Override
	public int hashCode() {
		return Objects.hash(models, customTitle, beforeAfterModel);
	}

	@Override
	public String toString() {
		return "GrepCompositeModel{" +
				"list=" + models +
				'}';
	}

}
