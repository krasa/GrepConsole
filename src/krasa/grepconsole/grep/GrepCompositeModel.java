package krasa.grepconsole.grep;

import com.intellij.util.xmlb.annotations.Transient;
import krasa.grepconsole.grep.actions.OpenGrepConsoleAction;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GrepCompositeModel {
	List<GrepModel> models = new ArrayList<GrepModel>();
	String customTitle;

	public GrepCompositeModel() {
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
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < models.size(); i++) {
			GrepModel grepModel = models.get(i);
			if (i != 0) {
				if (grepModel.isExclude()) {
					sb.append("-");
				} else {
					sb.append("+");
				}
			}
			sb.append(grepModel.getExpression());
		}
		String expression = sb.toString();
		if (expression.equals("")) {
			expression = "---";
		}
		return OpenGrepConsoleAction.title(expression);
	}


	public boolean matches(CharSequence charSequence) {
		for (GrepModel grepModel : models) {
			if (grepModel.isExclude()) {
				if (grepModel.matches(charSequence)) {
					return false;
				}
			}
		}
		for (GrepModel grepModel : models) {
			if (!grepModel.isExclude()) {
				if (grepModel.matches(charSequence)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GrepCompositeModel that = (GrepCompositeModel) o;
		return Objects.equals(models, that.models) && Objects.equals(customTitle, that.customTitle);
	}

	@Override
	public int hashCode() {
		return Objects.hash(models, customTitle);
	}

	@Override
	public String toString() {
		return "GrepCompositeModel{" +
				"list=" + models +
				'}';
	}
}
