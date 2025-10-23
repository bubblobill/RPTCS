package net.rptools.extra.addon.wrappers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.rpTools.aoTool.addon.addon.StatSheet;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WStatSheets {
    List<StatSheet> statSheets = new ArrayList<>();

    public WStatSheets(){}

    public StatSheet[] getStatSheets() {
        return statSheets.toArray(StatSheet[]::new);
    }
    public void setStatSheets(StatSheet[] statSheets) {
        this.statSheets.clear();
        Collections.addAll(this.statSheets, statSheets);
    }
    public void addStatSheets(JsonObject statSheetsJson){
        JsonArray array = statSheetsJson.getAsJsonObject().get("statSheets").getAsJsonArray();
        array.forEach(e -> {
            try {
                addStatSheet(new StatSheet(e.getAsJsonObject()));
            } catch (MalformedURLException ignored) {
            }
        });
    }
    public void addStatSheet(StatSheet statSheet){
        statSheets.add(statSheet);
    }
    @Override
    public String toString() {
        return "WStatSheets{" +
                "statSheets=" + getStatSheets() +
                "}";
    }
}
