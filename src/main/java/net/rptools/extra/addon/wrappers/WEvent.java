package net.rptools.extra.addon.wrappers;

public class WEvent {
    public static final String[] EVENT_NAMES = new String[]{
            "onFirstInit","onInit","onInitiativeChangeRequest","onTokenMove","onMultipleTokensMove"
    };

    String name;
    WMTScript mts;
    public WEvent(){}
    public WEvent(String name, WMTScript mts){
        this.name = name;
        this.mts = mts;
    }
    public WEvent setMts(WMTScript mts) {
        this.mts = mts;
        return this;
    }
    public WEvent setName(String name) {
        this.name = name;
        return this;
    }
    public WMTScript getMts() {
        return mts;
    }
    public String getName() {
        return name;
    }
}
