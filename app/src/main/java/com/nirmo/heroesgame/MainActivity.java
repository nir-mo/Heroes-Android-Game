package com.nirmo.heroesgame;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentActivity;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_start_board);
        FrameLayout board = (FrameLayout) findViewById(R.id.game_start_board);

        Button button = (Button) findViewById(R.id.button_send);
        button.setOnClickListener(new View.OnClickListener() {
                                      public void onClick(View view) {
                                          // Do something in response to button click
                                          Intent intent = new Intent(view.getContext(), GameActivity.class);
                                          view.getContext().startActivity(intent);
                                      }
                                  }
        );
    }

    }

//
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        // Use the Builder class for convenient dialog construction
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setMessage(R.string.dialog_start_game)
//                .setPositiveButton(R.string.start, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // START THE GAME!
//                        GameActivity  game_activity = new GameActivity();
//                    }
//                });
//
//        // Create the AlertDialog object and return it
//        return builder.create();
//    }
//    public void confirmStartGame() {
//        DialogFragment newFragment = new DialogFragment();
//        newFragment.show(getSupportFragmentManager(), "game");
//    }
//
//
//}
