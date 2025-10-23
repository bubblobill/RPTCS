package net.rptools.extra.addon.wrappers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WEvents {
    private final List<WEvent> WEvents = new ArrayList<>();
    private final List<WEvent> legacyWEvents = new ArrayList<>();
    public WEvents(){}

//    public WEvents(JsonObject jo) {
//        JsonArray array = jo.get("WEvents").getAsJsonArray();
//        array.forEach(jsonElement -> {
//            JsonObject job = jsonElement.getAsJsonObject();
//            WEvents.add(new WEvent(job.get())
//        });
//    }
    public void addEvents(JsonObject eventsJson){
        if(eventsJson.keySet().contains("WEvents")) {
            JsonArray eventsArray = eventsJson.getAsJsonObject().get("WEvents").getAsJsonArray();
            eventsArray.forEach(e -> addEvent(new WEvent()
                            .setName(e.getAsJsonObject().get("name").getAsString())
                            .setMts(new WMTScript().setFilename(Path.of(e.getAsJsonObject().get("mts").getAsString())))
                )
            );
        }
        if(eventsJson.keySet().contains("legacyWEvents")) {
            JsonArray legacyEventsArray = eventsJson.getAsJsonObject().get("legacyWEvents").getAsJsonArray();
            legacyEventsArray.forEach(e -> addLegacyEvent(new WEvent()
                            .setName(e.getAsJsonObject().get("name").getAsString())
                            .setMts(new WMTScript().setFilename(Path.of(e.getAsJsonObject().get("mts").getAsString())))
                    )
            );
        }
    }
    public void addEvent(WEvent WEvent){ WEvents.add(WEvent); }
    public void addLegacyEvent(WEvent WEvent){ legacyWEvents.add(WEvent); }
    public WEvent[] getEvents() { return WEvents.toArray(WEvent[]::new); }
    public WEvent[] getLegacyEvents() { return legacyWEvents.toArray(WEvent[]::new); }
    public void setEvents(WEvent[] WEvents) { this.WEvents.clear(); Collections.addAll(this.WEvents, WEvents); }
    public void setLegacyEvents(WEvent[] WEvents) { this.legacyWEvents.clear(); Collections.addAll(this.legacyWEvents, WEvents); }

}
