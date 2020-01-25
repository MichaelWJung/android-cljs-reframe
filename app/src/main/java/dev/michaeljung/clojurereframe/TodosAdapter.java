package dev.michaeljung.clojurereframe;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedStateListDrawable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

class TodosAdapter extends RecyclerView.Adapter {

    private LayoutInflater inflater;
    private JSONArray todos;
    private MainFragment fragment;

    TodosAdapter(MainFragment fragment) {
        this.inflater = LayoutInflater.from(fragment.getContext());
        this.fragment = fragment;
        todos = new JSONArray();
    }

    void setTodos(JSONArray todos) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TodoDiffCallback(todos, this.todos));
        diffResult.dispatchUpdatesTo(this);
        try {
            this.todos = new JSONArray(todos.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.todo_list_item, parent, false);
        return new TodoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            JSONObject todo = todos.getJSONObject(position);
            TodoViewHolder todoViewHolder = (TodoViewHolder) holder;

            final int id = todo.getInt("id");
            final String title = todo.getString("title");
            final boolean done = todo.getBoolean("done");

            todoViewHolder.id = id;
            todoViewHolder.title = title;
            todoViewHolder.todo_checkbox.setText(title);
            todoViewHolder.todo_checkbox.setChecked(done);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            TodoViewHolder todoViewHolder = (TodoViewHolder) holder;
            Bundle diff = (Bundle) payloads.get(0);
            for (String key : diff.keySet()) {
                switch (key) {
                    case "title":
                        final String title = diff.getString("title");
                        todoViewHolder.title = title;
                        todoViewHolder.todo_checkbox.setText(title);
                        break;
                    case "done":
                        final boolean done = diff.getBoolean("done");
                        todoViewHolder.todo_checkbox.setChecked(done);
                        break;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return todos.length();
    }

    class TodoViewHolder extends RecyclerView.ViewHolder {
        public final CheckBox todo_checkbox;
        public final ImageView menu_button;
        public int id;
        public String title;

        public TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            todo_checkbox = (CheckBox) itemView.findViewById(R.id.todo_checkbox);
            todo_checkbox.setOnClickListener(new CheckBoxOnClickListener());
            menu_button = (ImageView) itemView.findViewById(R.id.todo_menu);
            menu_button.setOnClickListener(new MenuOnClickListener());
            id = -1;
            title = "";
        }

        class CheckBoxOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                fragment.toggleChecked(id);
            }
        }

        class MenuOnClickListener implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_edit:
                        fragment.openEditTodoDialog(id, title);
                        break;
                    case R.id.menu_delete:
                        fragment.deleteTodo(id);
                }
                return true;
            }

            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(fragment.getContext(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.todo_item_popup_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(this);
                popup.show();
            }
        }
    }
}
