package net.rptools.extra.addon.addon;

import javax.script.ScriptException;

public class JSScriptEngine {
    public static JSScriptEngine getJSScriptEngine() {
        return new JSScriptEngine();
    }
public void evalScript(String jsContextName, String script, boolean b) throws ParserException, ScriptException {}
    public static boolean hasAddOnContext(String jsContextName) {
        return false;
    }

    public static void registerAddOnContext(String jsContextName) {

    }

    public static void removeAddOnContext(String jsContextName) {

    }
}
