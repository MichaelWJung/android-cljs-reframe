package dev.michaeljung.clojurereframe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class MainFragment extends MyFragment {

    public MainFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView textView = (TextView) view.findViewById(R.id.text);
        final Button button = (Button) view.findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatch("increase");
            }
        });

        doWhenReady(new Runnable() {
            @Override
            public void run() {
                button.setEnabled(true);
            }
        });

        subscribe("count", new EventListener() {
            @Override
            public void onEvent(JSONObject payload) {
                try {
                    textView.setText(Integer.toString(payload.getInt("value")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
