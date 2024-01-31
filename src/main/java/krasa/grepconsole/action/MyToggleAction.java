package krasa.grepconsole.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Supplier;

public abstract class MyToggleAction extends ToggleAction implements DumbAware {
	public MyToggleAction(@NotNull Supplier<@NlsActions.ActionText String> text, @NotNull Supplier<@NlsActions.ActionDescription String> description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	public MyToggleAction(@Nullable @NlsActions.ActionText String text, @Nullable @NlsActions.ActionDescription String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	public MyToggleAction() {
	}

	public MyToggleAction(@Nullable @NlsActions.ActionText String text) {
		super(text);
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}
}
