package dev.michaeljung.clojurereframe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;

public class TodoFragment extends CljsFragment implements AddTodoDialog.AddTodoDialogListener, EditTodoDialog.EditTodoDialogListener {

    private TodosAdapter todosAdapter;

    public TodoFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        todosAdapter = new TodosAdapter(this);
        RecyclerView todosView = view.findViewById(R.id.todos_view);
        todosView.setAdapter(todosAdapter);
        todosView.setLayoutManager(new LinearLayoutManager(getActivity()));

        final TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        final TabLayout.Tab tabAll = tabLayout.getTabAt(0);
        final TabLayout.Tab tabActive = tabLayout.getTabAt(1);
        final TabLayout.Tab tabDone = tabLayout.getTabAt(2);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                JSONArray query = new JSONArray();
                query.put("set-showing");
                if (tab == tabAll) {
                    query.put("all");
                } else if (tab == tabActive) {
                    query.put("active");
                } else if (tab == tabDone) {
                    query.put("done");
                } else {
                    return;
                }
                TodoFragment.this.dispatch(query);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        final FloatingActionButton addTodoButton = view.findViewById(R.id.button_add_todo);

        addTodoButton.setOnClickListener(v -> {
            AddTodoDialog dialog = new AddTodoDialog();
            dialog.setTargetFragment(TodoFragment.this, 0);
            dialog.show(getFragmentManager(), "add-todo");
        });

        subscribe("visible-todos", payload -> {
            try {
                todosAdapter.setTodos(payload.getJSONArray("value"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        subscribe("showing", payload -> {
            int id = 0;
            String showing = "";
            try {
                showing = payload.getString("value");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            switch (showing) {
                case "all":
                    tabAll.select();
                    break;
                case "active":
                    tabActive.select();
                    break;
                case "done":
                    tabDone.select();
                    break;
            }
        });
    }

    void toggleChecked(int id) {
        JSONArray event = new JSONArray();
        event.put("toggle-done");
        event.put(id);
        dispatch(event);
    }

    void openEditTodoDialog(int id, String title) {
        EditTodoDialog dialog = new EditTodoDialog(id, title);
        dialog.setTargetFragment(TodoFragment.this, 0);
        dialog.show(getFragmentManager(), "edit-todo");
    }

    @Override
    public void onFinishAddTodoDialog(String todoText) {
        JSONArray event = new JSONArray();
        event.put("add-todo");
        event.put(todoText);
        TodoFragment.this.dispatch(event);
    }

    @Override
    public void onFinishEditTodoDialog(int id, String todoText) {
        JSONArray event = new JSONArray();
        event.put("save");
        event.put(id);
        event.put(todoText);
        TodoFragment.this.dispatch(event);
    }

    public void deleteTodo(int id) {
        JSONArray event = new JSONArray();
        event.put("delete-todo");
        event.put(id);
        TodoFragment.this.dispatch(event);
    }
}
