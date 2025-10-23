package net.rptools.extra.app.event;

import net.rpTools.aoTool.addonui.Navigator;
import net.rpTools.aoTool.files.PathNode;

import java.awt.*;
import java.awt.event.ActionEvent;

public class NavigatorOpenEvent extends AWTEvent {
    public Navigator navigator;
    public PathNode pathNode;
    public NavigatorOpenEvent(Navigator source, PathNode pathNode){
        super(source, ActionEvent.ACTION_FIRST);
        this.navigator = source;
        this.pathNode = pathNode;
    }
}
