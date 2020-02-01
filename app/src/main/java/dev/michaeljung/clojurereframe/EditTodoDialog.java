package dev.michaeljung.clojurereframe;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class EditTodoDialog extends DialogFragment {

    private int todoId;
    private String title;
    private EditText input;

    interface EditTodoDialogListener {
        void onFinishEditTodoDialog(int id, String todoText);
    }

    public EditTodoDialog() {
    }

    EditTodoDialog(int todoId, String title) {
        this.todoId = todoId;
        this.title = title;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            todoId = savedInstanceState.getInt("id");
            title = savedInstanceState.getString("title");
        }
        if (title == null) {
            title = "";
        }
        input = createEditText(title);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("id", todoId);
        outState.putString("title", input.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit todo");
        builder.setView(input);
        builder.setPositiveButton("Save", new SaveButtonListener());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        return builder.create();
    }

    private EditText createEditText(@NonNull String text) {
        EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(text);
        return input;
    }

    private class SaveButtonListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            EditTodoDialogListener listener = (EditTodoDialogListener) getTargetFragment();
            assert listener != null;
            listener.onFinishEditTodoDialog(todoId, input.getText().toString());
            dialog.dismiss();
        }
    }
}
