package krasa.grepconsole.integration;

import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import krasa.grepconsole.filter.support.Cache;
import krasa.grepconsole.plugin.ServiceManager;
import org.jetbrains.annotations.NotNull;

public class LookAndFeelListener implements LafManagerListener {

  @Override
  public void lookAndFeelChanged(@NotNull LafManager lafManager) {
    Cache.reset();
    ServiceManager.getInstance().rehighlight();
  }
}
