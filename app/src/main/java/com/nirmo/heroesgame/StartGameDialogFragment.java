package com.nirmo.heroesgame;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;


public class StartGameDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialog_start_game)
                .setPositiveButton(R.string.start, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // START THE GAME!
                        GameActivity  game_activity = new GameActivity();
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }
}

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        return new AlertDialog.Builder(requireContext())
//                .setMessage(getString(R.string.order_confirmation))
//                .setPositiveButton(getString(R.string.ok), (dialog, which) -> {} )
//                .create();
//    }
//
//    public static String TAG = "PurchaseConfirmationDialog";
//}