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

public class AddTodoDialog extends DialogFragment {

    private EditText input = null;

    interface AddTodoDialogListener {
        void onFinishAddTodoDialog(String todoText);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String text = "";
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains("title")) {
                text = savedInstanceState.getString("title");
            }
        }
        input = createEditText(text);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("title", input.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add todo");
        builder.setView(input);
        builder.setPositiveButton("Add", new AddButtonListener());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        return builder.create();
    }

    private EditText createEditText(@NonNull String text) {
        EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(text);
        return input;
    }

    private class AddButtonListener implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            AddTodoDialogListener listener = (AddTodoDialogListener) getTargetFragment();
            assert listener != null;
            listener.onFinishAddTodoDialog(input.getText().toString());
            dialog.dismiss();
        }
    }
}
