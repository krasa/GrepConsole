package krasa.grepconsole.service;

import com.intellij.execution.impl.ConsoleState;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import junit.framework.Assert;
import krasa.grepconsole.console.GrepConsoleView;
import krasa.grepconsole.console.GrepConsoleViewImpl;
import krasa.grepconsole.decorators.ConsoleTextDecorator;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.ArrayList;

import static org.easymock.EasyMock.*;

public class GrepConsoleServiceTest {

    @Test
    public void testProcess() throws Exception {
        testOutputEqualsInput("kuk\nfoo\nbar");
        testOutputEqualsInput("\nkuk\nfoo\nbar\n");
        testOutputEqualsInput("\n\nkuk\nfoo\nbar\n\n");
        testOutputEqualsInput("\n");
        testOutputEqualsInput("\n\n");
        testOutputEqualsInput("\n\n");
    }

    private void testOutputEqualsInput(String input) {
        GrepConsoleService grepConsoleService = getGrepConsoleService();
        
        IMocksControl strictControl = EasyMock.createNiceControl();
        Capture<String> captured = new Capture<String>(CaptureType.ALL);

        GrepConsoleView consoleView = strictControl.createMock(GrepConsoleView.class);
        consoleView.printProcessedResult(capture(captured), anyObject(ConsoleViewContentType.class));
        EasyMock.expectLastCall().anyTimes();

        ConsoleViewContentType contentType = strictControl.createMock(ConsoleViewContentType.class);
        expect(contentType.getAttributes()).andReturn(new TextAttributes()).anyTimes();
        
        strictControl.replay();


        grepConsoleService.process(input, contentType, consoleView);
        strictControl.verify();

        StringBuilder sb = new StringBuilder();
        for (String o : captured.getValues()) {
            sb.append(o);
        }
        Assert.assertEquals(input, sb.toString());
    }

    private GrepConsoleService getGrepConsoleService() {
        ArrayList<ConsoleTextDecorator> consoleTextDecorators = new ArrayList<ConsoleTextDecorator>();
        consoleTextDecorators.add(new DummyConsoleTextDecorator(null));

        return new GrepConsoleService(consoleTextDecorators);
    }

}
