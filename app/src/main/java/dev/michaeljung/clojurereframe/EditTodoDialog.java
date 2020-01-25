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

    private final int todoId;
    private final String title;

    public interface EditTodoDialogListener {
        void onFinishEditTodoDialog(int id, String todoText);
    }

    public EditTodoDialog(int todoId, String title) {
        this.todoId = todoId;
        this.title = title;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit todo");

        final EditText input = new EditText(getActivity());
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
