package com.nirmo.heroesgame;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_start_board);

        Button button = (Button) findViewById(R.id.button_send);
        button.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), GameActivity.class);
            view.getContext().startActivity(intent);
        });
    }
}
