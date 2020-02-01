package dev.michaeljung.clojurereframe;

import org.json.JSONObject;

interface EventListener {
    void onEvent(JSONObject payload);
}
