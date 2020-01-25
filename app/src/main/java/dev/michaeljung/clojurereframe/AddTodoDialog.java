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
    private String text = null;

    public interface AddTodoDialogListener {
        void onFinishAddTodoDialog(String todoText);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            if (savedInstanceState.keySet().contains("title")) {
                text = savedInstanceState.getString("title");
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (input != null) {
            outState.putString("title", input.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add todo");

        input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        if (text != null) {
            input.setText(text);
        }
        builder.setView(input);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AddTodoDialogListener listener = (AddTodoDialogListener) getTargetFragment();
                listener.onFinishAddTodoDialog(input.getText().toString());
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
