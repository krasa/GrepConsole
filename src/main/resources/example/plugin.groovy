package example

import com.intellij.ide.plugins.IdeaPluginDescriptorImpl
import com.intellij.ide.plugins.PluginManager

//UNCOMMENT_THIS import static liveplugin.PluginUtil.*

import com.intellij.openapi.extensions.PluginId

import java.util.function.BiFunction
import java.util.regex.Matcher

import static com.intellij.openapi.util.text.StringUtil.newBombedCharSequence

/**
 https://github.com/dkandalov/live-plugin/blob/master/src/plugin-util-groovy/liveplugin/PluginUtil.groovy
 */


registerFunction("myScript", new BiFunction<String, Matcher, String>() {

    /** - The text will never be empty, it may or may not end with a newline - \n
     *  - It is possible that the stream will flush prematurely and the text will be incomplete: IDEA-70016
     *  - Return null to remove the line
     *  - Processing blocks application output stream, make sure to limit the length and processing time when needed using #limitAndCutNewline
     **/
    @Override
    String apply(String text, Matcher matcher) {
        try {
            CharSequence textForMatching = limitAndCutNewline(text, 150, 1000)

            if (textForMatching.contains("remove this line")) {
                return null
            }
            if (textForMatching.contains("notify me")) {
                show("Alert: " + text)
                return text
            }

            if (matcher.hasGroup()) {
                return "SHORTENED TO " + matcher.group(1)
            }

        } catch (com.intellij.openapi.progress.ProcessCanceledException ex) {
            show("Processing took too long for: " + text)
        }
        return text
    }
})


static Class<?> getExtensionManager() {
    IdeaPluginDescriptorImpl descriptor = PluginManager.getPlugin(PluginId.getId("GrepConsole"))
    return descriptor.getPluginClassLoader().loadClass("krasa.grepconsole.plugin.ExtensionManager");
}

static void registerFunction(String functionName, Object function) {
    Class<?> clazz = getExtensionManager()

    clazz.getMethod("register", String.class, Object.class)
            .invoke(null, functionName, function);

    liveplugin.PluginUtil.show("'" + functionName + "' registered")
}


static CharSequence limitAndCutNewline(String text, int maxLength, milliseconds) {
    int endIndex = text.length()
    if (text.endsWith("\n")) {
        --endIndex
    }
    if (maxLength >= 0) {
        endIndex = Math.min(endIndex, maxLength)
    }
    def substring = text.substring(0, endIndex)

    if (milliseconds > 0) {
        return newBombedCharSequence(substring, milliseconds)
    }
    return substring
}


