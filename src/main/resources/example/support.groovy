package example

import com.intellij.ide.plugins.IdeaPluginDescriptorImpl
import com.intellij.ide.plugins.PluginManager
import com.intellij.openapi.extensions.PluginId

import java.util.function.Function

import static com.intellij.openapi.util.text.StringUtil.newBombedCharSequence

static Class<?> getExtensionManager() {
    IdeaPluginDescriptorImpl descriptor = PluginManager.getPlugin(PluginId.getId("GrepConsole"))
    return descriptor.getPluginClassLoader().loadClass("krasa.grepconsole.plugin.ExtensionManager");
}

static void registerFunction(String functionName, Function<String, String> function) {
    Class<?> clazz = getExtensionManager()

    clazz.getMethod("registerFunction", String.class, Function.class)
            .invoke(null, functionName, function);

    liveplugin.PluginUtil.show("'" + functionName + "' registered")
}

static Object unregisterFunction(Class<?> clazz, String functionName) {
    clazz.getMethod("unregisterFunction", String.class).invoke(null, functionName)
    liveplugin.PluginUtil.show("'" + functionName + "' unregistered")
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


