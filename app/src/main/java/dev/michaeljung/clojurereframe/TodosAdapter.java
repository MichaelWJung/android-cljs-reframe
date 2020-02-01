package dev.michaeljung.clojurereframe;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

class TodosAdapter extends RecyclerView.Adapter {

    private JSONArray todos;
    private Context context;
    private Callbacks callbacks;
    private LayoutInflater inflater;

    interface Callbacks {
        void toggleChecked(int id);
        void openEditTodoDialog(int id, String title);
        void deleteTodo(int id);
    }

    TodosAdapter(Context context, Callbacks callbacks) {
        this.todos = new JSONArray();
        this.context = context;
        this.callbacks = callbacks;
        this.inflater = LayoutInflater.from(context);
    }

    void setTodos(JSONArray todos) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TodoDiffCallback(this.todos, todos));
        diffResult.dispatchUpdatesTo(this);
        this.todos = todos;
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
            final JSONObject todo = todos.getJSONObject(position);
            final TodoViewHolder todoViewHolder = (TodoViewHolder) holder;
            todoViewHolder.todo_checkbox.setText(todo.getString("title"));
            todoViewHolder.todo_checkbox.setChecked(todo.getBoolean("done"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            final TodoViewHolder todoViewHolder = (TodoViewHolder) holder;
            final Bundle diff = (Bundle) payloads.get(0);
            for (String key : diff.keySet()) {
                switch (key) {
                    case "title":
                        final String title = diff.getString("title");
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
        final CheckBox todo_checkbox;
        final ImageView menu_button;

        TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            todo_checkbox = itemView.findViewById(R.id.todo_checkbox);
            todo_checkbox.setOnClickListener(new CheckBoxOnClickListener());
            menu_button = itemView.findViewById(R.id.todo_menu);
            menu_button.setOnClickListener(new MenuOnClickListener());
        }

        class CheckBoxOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) {
                callbacks.toggleChecked(getId());
            }
        }

        class MenuOnClickListener implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.todo_item_popup_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(this);
                popup.show();
            }

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_edit:
                        callbacks.openEditTodoDialog(getId(), getTitle());
                        break;
                    case R.id.menu_delete:
                        callbacks.deleteTodo(getId());
                }
                return true;
            }
        }

        private int getId() {
            try {
                return getTodo().getInt("id");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        private String getTitle() {
            try {
                return getTodo().getString("title");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        private JSONObject getTodo() throws JSONException {
            return todos.getJSONObject(getAdapterPosition());
        }
    }
}
