package dev.michaeljung.clojurereframe;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TodoDiffCallback extends DiffUtil.Callback {

    private JSONArray newTodos;
    private JSONArray oldTodos;

    TodoDiffCallback(JSONArray oldTodos, JSONArray newTodos) {
        this.newTodos = newTodos;
        this.oldTodos = oldTodos;
    }

    @Override
    public int getOldListSize() {
        return oldTodos.length();
    }

    @Override
    public int getNewListSize() {
        return newTodos.length();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        try {
            JSONObject oldTodo = oldTodos.getJSONObject(oldItemPosition);
            JSONObject newTodo = newTodos.getJSONObject(newItemPosition);
            return oldTodo.getInt("id") == newTodo.getInt("id");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        try {
            JSONObject oldTodo = oldTodos.getJSONObject(oldItemPosition);
            JSONObject newTodo = newTodos.getJSONObject(newItemPosition);
            return oldTodo.getString("title").equals(newTodo.getString("title")) &&
                   oldTodo.getBoolean("done") == newTodo.getBoolean("done");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        try {
            JSONObject oldTodo = oldTodos.getJSONObject(oldItemPosition);
            JSONObject newTodo = newTodos.getJSONObject(newItemPosition);

            Bundle diff = new Bundle();
            if (!oldTodo.getString("title").equals(newTodo.getString("title"))) {
                diff.putString("title", newTodo.getString("title"));
            }
            if (oldTodo.getBoolean("done") != newTodo.getBoolean("done")) {
                diff.putBoolean("done", newTodo.getBoolean("done"));
            }
            if (diff.size() == 0) {
                return null;
            }
            return diff;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
