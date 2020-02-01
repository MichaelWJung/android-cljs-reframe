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

import java.util.HashMap;

public class TodoFragment extends CljsFragment implements AddTodoDialog.AddTodoDialogListener, EditTodoDialog.EditTodoDialogListener, TodosAdapter.Callbacks {

    private static final String EDIT_TODO_TAG = "edit-todo";
    public static final String ADD_TODO_TAG = "add-todo";

    enum Tab {
        ALL, ACTIVE, DONE
    }

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

        setupTodosView(view);
        setupTabs(view);
        setupAddTodoButton(view);
    }

    @Override
    public void onFinishAddTodoDialog(String todoText) {
        final JSONArray event = new JSONArray();
        event.put("add-todo");
        event.put(todoText);
        dispatch(event);
    }

    @Override
    public void onFinishEditTodoDialog(int id, String todoText) {
        final JSONArray event = new JSONArray();
        event.put("save");
        event.put(id);
        event.put(todoText);
        dispatch(event);
    }

    @Override
    public void toggleChecked(int id) {
        final JSONArray event = new JSONArray();
        event.put("toggle-done");
        event.put(id);
        dispatch(event);
    }

    @Override
    public void deleteTodo(int id) {
        final JSONArray event = new JSONArray();
        event.put("delete-todo");
        event.put(id);
        TodoFragment.this.dispatch(event);
    }

    @Override
    public void openEditTodoDialog(int id, String title) {
        final EditTodoDialog dialog = new EditTodoDialog(id, title);
        dialog.setTargetFragment(TodoFragment.this, 0);
        assert getFragmentManager() != null;
        dialog.show(getFragmentManager(), EDIT_TODO_TAG);
    }

    private void openAddTodoDialog() {
        AddTodoDialog dialog = new AddTodoDialog();
        dialog.setTargetFragment(TodoFragment.this, 0);
        assert getFragmentManager() != null;
        dialog.show(getFragmentManager(), ADD_TODO_TAG);
    }

    private void setupTodosView(@NonNull View fragmentView) {
        final TodosAdapter todosAdapter = new TodosAdapter(getContext(), this);
        final RecyclerView todosView = fragmentView.findViewById(R.id.todos_view);
        todosView.setAdapter(todosAdapter);
        todosView.setLayoutManager(new LinearLayoutManager(getActivity()));
        subscribeToVisibleTodos(todosAdapter);
    }

    private void setupTabs(@NonNull View fragmentView) {
        final TabLayout tabLayout = fragmentView.findViewById(R.id.tab_layout);
        final HashMap<Tab, TabLayout.Tab> tabs = createTabs(tabLayout);
        tabLayout.addOnTabSelectedListener(new ShowingTabSelectedListener(tabs));
        subscribeToShowing(tabs);
    }

    private void setupAddTodoButton(@NonNull View fragmentView) {
        final FloatingActionButton addTodoButton = fragmentView.findViewById(R.id.button_add_todo);
        addTodoButton.setOnClickListener(v -> openAddTodoDialog());
    }

    private void subscribeToShowing(HashMap<Tab, TabLayout.Tab> tabs) {
        subscribe("showing", payload -> {
            try {
                String showing = payload.getString("value");
                switch (showing) {
                    case "all":
                        tabs.get(Tab.ALL).select();
                        break;
                    case "active":
                        tabs.get(Tab.ACTIVE).select();
                        break;
                    case "done":
                        tabs.get(Tab.DONE).select();
                        break;
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void subscribeToVisibleTodos(TodosAdapter todosAdapter) {
        subscribe("visible-todos", payload -> {
            try {
                todosAdapter.setTodos(payload.getJSONArray("value"));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private HashMap<Tab, TabLayout.Tab> createTabs(TabLayout tabLayout) {
        HashMap<Tab, TabLayout.Tab> tabs = new HashMap<>();
        for (Tab t : new Tab[]{Tab.ALL, Tab.ACTIVE, Tab.DONE}) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setText(getTabText(t));
            tabLayout.addTab(tab);
            tabs.put(t, tab);
        }
        return tabs;
    }

    private String getTabText(Tab tab) {
        switch (tab) {
            case ALL:
                return "All";
            case ACTIVE:
                return "Active";
            case DONE:
                return "Done";
            default:
                return null;
        }
    }

    private class ShowingTabSelectedListener implements TabLayout.OnTabSelectedListener {
        private final HashMap<Tab, TabLayout.Tab> tabs;

        ShowingTabSelectedListener(HashMap<Tab, TabLayout.Tab> tabs) {
            this.tabs = tabs;
        }

        @Override
        public void onTabSelected(TabLayout.Tab tab) {
            JSONArray query = new JSONArray();
            query.put("set-showing");
            int pos = tab.getPosition();
            if (pos == tabs.get(Tab.ALL).getPosition()) {
                query.put("all");
            } else if (pos == tabs.get(Tab.ACTIVE).getPosition()) {
                query.put("active");
            } else if (pos == tabs.get(Tab.DONE).getPosition()) {
                query.put("done");
            } else {
                return;
            }
            dispatch(query);
        }

        @Override
        public void onTabUnselected(TabLayout.Tab tab) {

        }

        @Override
        public void onTabReselected(TabLayout.Tab tab) {

        }
    }
}
