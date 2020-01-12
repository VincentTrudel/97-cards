package com.example.tpfinal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button playButton = findViewById(R.id.playButton);
        TextView highestScoreTextView = findViewById(R.id.highestScore);
        highestScoreTextView.setText("Highest score: "+String.valueOf(DatabaseHelper.getInstance(this).highestScore()));
        playButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId()) {
            case R.id.playButton: {
                Intent i = new Intent ( MainActivity.this,  GameActivity.class );
                startActivity(i);
            }
        }
    }
}
