package dev.michaeljung.clojurereframe;

import org.json.JSONObject;

interface EventListener {
    public void onEvent(String event, JSONObject payload);
}
