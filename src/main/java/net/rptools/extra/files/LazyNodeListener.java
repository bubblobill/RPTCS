package net.rptools.extra.files;

import javax.annotation.Nullable;

public interface LazyNodeListener {
     enum LazyNodeEventType {CHANGED,LOADED,LOADING,ERROR}
     record LazyNodeEvent(@Nullable LazyNode node, LazyNodeEventType eventType, @Nullable String message, @Nullable Exception exception){}

     void event(LazyNode.LazyNodeEvent e);
}
