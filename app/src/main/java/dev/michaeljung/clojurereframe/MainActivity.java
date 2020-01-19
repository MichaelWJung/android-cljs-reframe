package dev.michaeljung.clojurereframe;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.liquidplayer.service.MicroService;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private String updateListenerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final TextView textView = (TextView) findViewById(R.id.text);
        final Button button = (Button) findViewById(R.id.button);
        updateListenerId = UUID.randomUUID().toString();

        Application app = (Application) getApplicationContext();

        app.doWhenReady(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
            }
        });

        app.subscribe(updateListenerId, "count", new EventListener() {
            @Override
            public void onEvent(String event, JSONObject payload) {
                try {
                    textView.setText(Integer.toString(payload.getInt("value")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Application app = (Application) getApplicationContext();
        app.unsubscribe(updateListenerId);
    }

    public void dispatchIncrease(View view) {
        Application app = (Application) getApplicationContext();
        app.dispatch("increase");
    }
}
