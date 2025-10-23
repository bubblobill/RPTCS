package net.rptools.extra.addon.wrappers;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WMTScripts {
    final List<WMTScript> properties = new ArrayList<>();
    public WMTScripts(){}
    public WMTScript[] getProperties(){
        return properties.toArray(WMTScript[]::new);
    }
    public void setProperties(WMTScript[] properties){
        this.properties.clear();
        Collections.addAll(this.properties, properties);
    }
    public void addMTScript(WMTScript WMTScript){
        properties.add(WMTScript);
    }
    public void addMTScripts(JsonObject propertySet){
        if(propertySet.keySet().contains("properties")){
            propertySet.get("properties").getAsJsonArray().forEach(el -> addMTScript(new WMTScript(el.getAsJsonObject())));
        }

    }}
