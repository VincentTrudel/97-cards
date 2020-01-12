package com.example.tpfinal;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GameoverActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gameover);

        TextView scoreContainer = findViewById(R.id.score);

        DatabaseHelper instance = DatabaseHelper.getInstance(this);


        String scoreString = getIntent().getExtras().getString("SCORE");
        instance.ajouterScore(new Score(Integer.parseInt(scoreString) ), instance.getWritableDatabase());
        scoreContainer.setText("Your score: "+scoreString);



        Button playAgainButton = findViewById(R.id.playAgainButton);
        playAgainButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId()) {
            case R.id.playAgainButton: {
                Intent i = new Intent ( GameoverActivity.this,  GameActivity.class );
                startActivity(i);
            }
        }
    }


}
