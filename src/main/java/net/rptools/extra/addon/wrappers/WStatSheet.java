package net.rptools.extra.addon.wrappers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class WStatSheet {
    private static final Logger log = LogManager.getLogger(WStatSheet.class);
    String name;
    String description;
    String namespace = "namespace";
    Set<String> propertyTypes = new HashSet<>();
    URL entry;

    public WStatSheet() {
    }

    public WStatSheet(String namespace, JsonObject statSheetJson) {
        this.namespace = namespace;
        this.name = statSheetJson.get("name").getAsString();
        this.description = statSheetJson.get("description").getAsString();
        this.propertyTypes = statSheetJson.get("propertyTypes").getAsJsonArray().asList().stream().map(JsonElement::getAsString).collect(Collectors.toSet());
        setEntry(statSheetJson.get("entry").getAsString());
    }
    public WStatSheet(JsonObject statSheetJson) {
        this("namespace", statSheetJson);
    }

    public WStatSheet setDescription(String description) {
        this.description = description;
        return this;
    }

    public WStatSheet setNameSpace(String namespace){
        if(this.entry != null){
            try {
                entry = URI.create(this.entry.toString().replace(this.namespace, namespace)).toURL();
            } catch (MalformedURLException ignored) {
            }
        }
        this.namespace = namespace;
        return this;
    }
    public WStatSheet setEntry(String entry) {
        try {
            this.entry = new URI("lib://" + namespace + "/" + entry).toURL();
        } catch (MalformedURLException|URISyntaxException ignored) {
        }
        return this;
    }

    public WStatSheet setName(String name) {
        this.name = name;
        return this;
    }

    public String getEntryPath() {
        return this.entry.getPath();
    }

    public WStatSheet setPropertyTypes(Set<String> propertyTypes) {
        this.propertyTypes = propertyTypes;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public URL getEntry() {
        return entry;
    }

    public String getName() {
        return name;
    }

    public Set<String> getPropertyTypes() {
        return propertyTypes;
    }
}
