package krasa.grepconsole.model;

import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class GrepExpressionGroup {
    private  boolean enabled;
    private  String name;
    private  List<GrepExpressionItem> grepExpressionItems;

    public GrepExpressionGroup() {
    }

    public GrepExpressionGroup(String name, List<GrepExpressionItem> grepExpressionItems) {
        this.name = name;
        this.grepExpressionItems = grepExpressionItems;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public List<GrepExpressionItem> getGrepExpressionItems() {
        return grepExpressionItems;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGrepExpressionItems(List<GrepExpressionItem> grepExpressionItems) {
        this.grepExpressionItems = grepExpressionItems;
    }
}
