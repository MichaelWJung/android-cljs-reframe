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

    public interface EditTodoDialogListener {
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
        if (savedInstanceState != null) {
            todoId = savedInstanceState.getInt("id");
            title = savedInstanceState.getString("title");
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("id", todoId);
        if (input != null) {
            outState.putString("title", input.getText().toString());
        } else {
            outState.putString("title", title);
        }
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit todo");

        input = new EditText(getActivity());
        input.setText(title);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditTodoDialogListener listener = (EditTodoDialogListener) getTargetFragment();
                listener.onFinishEditTodoDialog(todoId, input.getText().toString());
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();
    }
}
