package com.example.tpfinal;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.gson.Gson;
import java.util.Stack;


public class GameActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView cardsLeft;
    private TextView timeContainer;
    private String selectedCardValueString;
    private TextView selectedCardTextView;
    private Stack<String> removedCardsValueStr = new Stack<>();
    private Stack<Integer> removedCardsLocation = new Stack<>();
    private Deck deck = new Deck();
    private Thread thread = null;
    private long startTime;
    private long timeSinceStartMs;
    private Integer[] ascendingCardIds = {R.id.ascendingCardLeft, R.id.ascendingCardRight};
    private Integer[] descendingCardIds = {R.id.descendingCardLeft, R.id.descendingCardRight};
    private int minutes;
    private int seconds;
    private long timeSinceLastPlay = -1;
    private long timeDiffWithLastPlay;
    private String minutesStr = null;
    private String secondsStr = null;
    private LinearLayout descendingCards = null;
    private LinearLayout ascendingCards =  null;
    private android.support.v7.widget.GridLayout cards = null;
    private TextView scoreContainer = null;
    private Integer lastCardOnPileBeforePlayContainerId = null;
    private String lastCardOnPileBeforePlayValue = null;
    private long timeAlreadyPlayed;
    private int score = 0;
    private int scoreToAdd = 0;
    private ImageView replayButton = null;
    private String[] cardsInHand = null;
    private boolean newGame;
    private Stack<Integer> retrievedCardList;

    private void changeCardColor(TextView cardTextView, int cardValue)
    {
        if (cardValue == 98 || cardValue == 0)
        {
            cardTextView.setBackgroundResource(R.drawable.card_in_play);
        }
        else if (cardValue < 21)
        {
            cardTextView.setBackgroundResource(R.drawable.card_container_0_20);
        }
        else if ( cardValue >20 && cardValue < 41)
        {
            cardTextView.setBackgroundResource(R.drawable.card_container_21_40);
        }
        else if ( cardValue >40 && cardValue < 61)
        {
            cardTextView.setBackgroundResource(R.drawable.card_container_41_60);
        }
        else if ( cardValue >60 && cardValue < 80 )
        {
            cardTextView.setBackgroundResource(R.drawable.card_container_61_80);
        }
        else{
            cardTextView.setBackgroundResource(R.drawable.card_container_81_plus);
        }
    }

    private String timeInMsToString(long timeInMs)
    {
        minutes = (int)timeSinceStartMs/60000;
        seconds =  (int)(timeSinceStartMs%60000)/1000;
        // update TextView here!
        secondsStr = String.valueOf(seconds);
        minutesStr = String.valueOf(minutes);
        if (minutes < 10)
        {
            minutesStr = "0"+minutesStr;
        }
        if (seconds < 10)
        {
            secondsStr = "0"+secondsStr;
        }
       return minutesStr+":"+secondsStr;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = this.getSharedPreferences(
                "97CARDS", Context.MODE_PRIVATE);

        newGame = prefs.getString( "cardsInHand" , null) == null;
        setContentView(R.layout.activity_game);

        timeAlreadyPlayed = 0;
        startTime = new java.util.Date().getTime();
        timeContainer = findViewById(R.id.timeContainer);
        descendingCards =  findViewById(R.id.descendingCards);
        ascendingCards =  findViewById(R.id.ascendingCards);
        scoreContainer = findViewById(R.id.scoreContainer);
        replayButton = findViewById(R.id.replayButton);
        cardsLeft = findViewById(R.id.cardsLeftCount);
        replayButton.setOnClickListener(this);
        //newDate = dateFormat.parse(dateTime);
        thread = new Thread() {

            @Override
            public void run() {
                try {
                    while (!thread.isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                timeSinceStartMs = (new java.util.Date().getTime() - startTime) +timeAlreadyPlayed;
                                timeContainer.setText(timeInMsToString(timeSinceStartMs));

                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        Ecouteur ec = new Ecouteur();
        DropListener dropListener = new DropListener();
        cards = findViewById(R.id.Cards);

        if (newGame)
        {
            deck.createAndShuffle();
            cardsLeft.setText(String.valueOf(deck.size()));
        }


        for (int i = 0; i < cards.getChildCount(); i++)
        {

            TextView card = (TextView) cards.getChildAt(i);
            if (newGame)
            {
                int cardValue = deck.draw();
                changeCardColor(card, cardValue);
                card.setText(String.valueOf(cardValue));
            }
            card.setOnDragListener(ec);
            card.setOnTouchListener(ec);
        }

        for(int i=0; i<descendingCards.getChildCount(); i++)
        {
            LinearLayout descendingCardContainer = (LinearLayout)descendingCards.getChildAt(i);
            descendingCardContainer.setOnDragListener(dropListener);
        }
        for(int i=0; i<ascendingCards.getChildCount(); i++)
        {
            LinearLayout ascendingCardContainer = (LinearLayout)ascendingCards.getChildAt(i);
            ascendingCardContainer.setOnDragListener(dropListener);
        }
        thread.start();

    }

    @Override
    protected void onPause(){
        super.onPause();

        SharedPreferences prefs = this.getSharedPreferences(
                "97CARDS", Context.MODE_PRIVATE);


        //Sauver les cartes dans la main
        Gson gson = new Gson();
        cardsInHand = new String[8];
        for(int i=0; i<8; i++)
        {
            cardsInHand[i]= (String)((TextView)cards.getChildAt(i)).getText();
        }
        prefs.edit().putString("cardsInHand", gson.toJson(cardsInHand) ).apply();
        //Sauver les cartes sur les piles descendantes
        for (Integer descendingCardId : descendingCardIds)
        {
            prefs.edit().putString(String.valueOf(descendingCardId), (String)((TextView)findViewById(descendingCardId)).getText()).apply();
        }
        //Sauver les cartes sur les piles ascendantes
        for (Integer ascendingCardId : ascendingCardIds)
        {
            prefs.edit().putString(String.valueOf(ascendingCardId), (String)((TextView)findViewById(ascendingCardId)).getText()).apply();
        }
        //Sauver le nombre de cartes restantes dans le paquet
        prefs.edit().putString("cardsLeft", (String)cardsLeft.getText()).apply();
        //Sauver le score
        prefs.edit().putInt("score", score).apply();
        //Sauver le temps déjà joué en millisecondes
        prefs.edit().putLong("timeAlreadyPlayed", timeSinceStartMs ).apply();
        //Sauver le temps depuis le dernier play
        prefs.edit().putLong("timeSinceLastPlay", timeSinceLastPlay ).apply();
        //sauver les cartes enlevées
        prefs.edit().putInt("removedCardsSize", removedCardsValueStr.size() ).apply();

        for (int i=0; i<removedCardsValueStr.size(); i++)
        {
            prefs.edit().putString("removedCardsValueStr"+String.valueOf(i), removedCardsValueStr.pop()).apply();
            prefs.edit().putInt("removedCardsLocation"+String.valueOf(i), removedCardsLocation.pop()).apply();
        }
        //sauver le deck

        prefs.edit().putInt("deckSize", deck.size() ).apply();
        for (int i=0; i<deck.size(); i++)
        {
            prefs.edit().putInt("deckList"+String.valueOf(i), deck.draw()).apply();
        }


        //sauver l'état du bouton replay
        prefs.edit().putInt("replayButtonTag", (Integer)replayButton.getTag() ).apply();

        //sauver la carte sur la pile qui doit être affichée si on appuie sur replay
        prefs.edit().putInt("lastCardOnPileBeforePlayContainerId", lastCardOnPileBeforePlayContainerId ).apply();
        //sauver son emplacement
        prefs.edit().putString("lastCardOnPileBeforePlayValue", lastCardOnPileBeforePlayValue ).apply();


    }
    @Override
    protected void onResume(){
        super.onResume();

        if (!newGame)
        {
            SharedPreferences prefs = this.getSharedPreferences(
                    "97CARDS", Context.MODE_PRIVATE);
            //Récupérer les cartes dans la main
            Gson gson = new Gson();
            String cardsInHandStr = prefs.getString("cardsInHand", null);

            if (cardsInHandStr != null)
            {
                cardsInHand = gson.fromJson(cardsInHandStr, String[].class);
                for (int i = 0; i < 8; i++)
                {
                    TextView currentCardInHand = (TextView)cards.getChildAt(i);
                    if (!cardsInHand[i].contentEquals(""))
                    {
                        currentCardInHand.setText(cardsInHand[i]);
                        changeCardColor(currentCardInHand, Integer.parseInt(cardsInHand[i]));
                    }
                    else
                    {
                        currentCardInHand.setText("");
                        currentCardInHand.setBackgroundResource(R.color.background_color);
                    }
                }
            }
            //Récupérer les cartes sur les piles descendantes
            for (Integer descendingCardId : descendingCardIds)
            {
                TextView currentCardInPile = findViewById(descendingCardId);
                String currentCardInPileValue = prefs.getString(String.valueOf(descendingCardId), "98");
                currentCardInPile.setText(currentCardInPileValue);
                changeCardColor(currentCardInPile, Integer.valueOf(currentCardInPileValue));
            }
            //Récupérer les cartes sur les piles ascendantes
            for (Integer ascendingCardId : ascendingCardIds)
            {
                TextView currentCardInPile = findViewById(ascendingCardId);
                String currentCardInPileValue = prefs.getString(String.valueOf(ascendingCardId), "0");
                currentCardInPile.setText(currentCardInPileValue);
                changeCardColor(currentCardInPile, Integer.valueOf(currentCardInPileValue));
            }
            //Récupérer le nombre de cartes restantes dans le paquet
            cardsLeft.setText(prefs.getString("cardsLeft", "89"));
            //Récupérer le score
            score = prefs.getInt("score", 0);
            scoreContainer.setText(String.valueOf(score));

            //Récupérer le temps en millisecondes
            timeAlreadyPlayed = prefs.getLong("timeAlreadyPlayed", 0 );

            //Récupérer le temps depuis le dernier play
            timeSinceLastPlay = prefs.getLong("timeSinceLastPlay", 0 );
            //Récupérer les cartes enlevées en JSON

            removedCardsValueStr = (Stack<String>)gson.fromJson(prefs.getString("removedCardsValueStr", "[]" ), Stack.class);
            removedCardsLocation =  (Stack<Integer>)gson.fromJson(prefs.getString("removedCardsLocation", "[]" ), Stack.class);


            retrievedCardList = new Stack<>();
            //Récupérer le deck
            for (int i=0; i<prefs.getInt("deckSize", 0); i++)
            {
                retrievedCardList.push(prefs.getInt("deckList"+String.valueOf(i), -1));
            }
            deck.setCardList(retrievedCardList);
            //Ici, le deck est récupéré à l'envers, mais c'est pas grave parce qu'il est random anyway

            for (int i=0; i<deck.size(); i++)
            {
                prefs.edit().putInt("deckList"+String.valueOf(i), deck.draw()).apply();
            }

            for (int i=0; i<prefs.getInt("removedCardsSize", 0); i++)
            {
                removedCardsLocation.push(prefs.getInt("removedCardsLocation"+String.valueOf(i), -1));
                removedCardsValueStr.push(prefs.getString("removedCardsValueStr"+String.valueOf(i), null));
            }

            //Récupérer l'état du bouton replay
            Integer replayButtonImage = prefs.getInt("replayButtonTag", R.drawable.replay_button );
            replayButton.setImageResource(replayButtonImage);
            replayButton.setTag(replayButtonImage);

            //Récupérer la carte sur la pile qui doit être affichée si on appuie sur replay
            lastCardOnPileBeforePlayContainerId =  prefs.getInt("lastCardOnPileBeforePlayContainerId", -1 );
            //Récupérer son emplacement
            lastCardOnPileBeforePlayValue =  prefs.getString("lastCardOnPileBeforePlayValue", "" );
        }


    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        this.getSharedPreferences("97CARDS", 0).edit().clear().commit();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.replayButton: {
                if (replayButton.getTag() != null)
                {
                    if (replayButton.getTag().equals(R.drawable.replay_button_black)) {

                        String lastCardPlayedValueStr = removedCardsValueStr.pop();
                        System.out.println(removedCardsLocation.toString());
                        TextView lastCardPlayedLocation = findViewById(removedCardsLocation.pop());
                        lastCardPlayedLocation.setText(lastCardPlayedValueStr);
                        changeCardColor(lastCardPlayedLocation, Integer.parseInt(lastCardPlayedValueStr));
                        TextView lastCardOnPileBeforePlayContainer = findViewById(lastCardOnPileBeforePlayContainerId);
                        lastCardOnPileBeforePlayContainer.setText(lastCardOnPileBeforePlayValue);
                        changeCardColor(lastCardOnPileBeforePlayContainer, Integer.parseInt(lastCardOnPileBeforePlayValue));

                        score-=scoreToAdd;
                        score *= .95;
                        scoreContainer.setText(String.valueOf(score));

                        replayButton.setImageResource(R.drawable.replay_button);
                        replayButton.setTag(R.drawable.replay_button);
                    }
                }
            }
        }
    }

    private class DropListener implements View.OnDragListener
    {

        @Override
        public boolean onDrag(View v, DragEvent event) {


            switch (event.getAction())
            {
                case DragEvent.ACTION_DROP:
                    LinearLayout container = (LinearLayout)v;
                    String parentContainerName =  getResources().getResourceEntryName(( (LinearLayout) container.getParent()).getId());
                    String containerName = getResources().getResourceEntryName(container.getId());
                    TextView cardDroppedOn = null;
                    TextView targetContainer;
                    boolean success = false;
                    int selectedCardValueInt = Integer.parseInt(selectedCardValueString);
                    int currentCardInContainerValue;
                    if (containerName.indexOf("1") !=-1) //Le conteneur est à gauche donc le 2e item (index 1) est le textview contenant le numéro de la carte
                    {
                        cardDroppedOn = (TextView)container.getChildAt(1);
                    }
                    else if (containerName.indexOf("2") !=-1) //Le conteneur est à droite donc le 1er item (index 0) est le textview contenant le numéro de la carte
                    {
                        cardDroppedOn = (TextView)container.getChildAt(0);
                    }
                    currentCardInContainerValue = Integer.parseInt( (String)cardDroppedOn.getText() );

                    if (parentContainerName.contentEquals("ascendingCards"))
                    {
                        if (selectedCardValueInt > currentCardInContainerValue || currentCardInContainerValue - selectedCardValueInt == 10)
                            success = true;
                    }
                    else // parentContainerName == "descendingCards"
                    {
                        if (selectedCardValueInt < currentCardInContainerValue ||  selectedCardValueInt - currentCardInContainerValue == 10)
                            success = true;
                    }

                    if (success) //mettre la carte dans la pile cardDroppedOn
                    {

                        lastCardOnPileBeforePlayContainerId = cardDroppedOn.getId();
                        lastCardOnPileBeforePlayValue = (String)cardDroppedOn.getText();
                        targetContainer = cardDroppedOn;
                        removedCardsValueStr.push(selectedCardValueString);
                        removedCardsLocation.push(selectedCardTextView.getId());
                        cardsLeft.setText(String.valueOf(deck.size()- removedCardsValueStr.size()));

                        if (timeSinceLastPlay == -1)
                        {
                            timeSinceLastPlay = startTime;

                        }
                        else
                        {
                            timeSinceLastPlay = new java.util.Date().getTime();
                        }

                        timeDiffWithLastPlay = new java.util.Date().getTime() - timeSinceLastPlay;

                        scoreToAdd = Math.abs(selectedCardValueInt - currentCardInContainerValue) * 100 - (int)(timeDiffWithLastPlay*.01);
                        if (scoreToAdd < 0 )
                        {
                            scoreToAdd = 0;
                        }


                        score+= scoreToAdd;
                        scoreContainer.setText(String.valueOf(score));

                        if (replayButton.getTag() != null && replayButton.getTag().equals(R.drawable.replay_button_black))
                        {
                            replayButton.setImageResource(R.drawable.replay_button);
                            replayButton.setTag(R.drawable.replay_button);
                        }


                        if (removedCardsValueStr.size() ==2)
                        {
                            if (deck.size() > 0)
                            {
                                for (int i = 0; i < 2; i++) {
                                    TextView newCard = findViewById(removedCardsLocation.pop());
                                    removedCardsValueStr.pop();

                                    int newCardValueInt = deck.draw();
                                    newCard.setText(String.valueOf(newCardValueInt));
                                    changeCardColor(newCard, newCardValueInt);
                                }
                            }
                        }
                        else if (removedCardsValueStr.size() ==1)
                        {
                            replayButton.setImageResource(R.drawable.replay_button_black);
                            replayButton.setTag(R.drawable.replay_button_black);
                        }
                    }
                    else //remettre la carte dans son textView d'origine
                    {
                        targetContainer = selectedCardTextView;
                    }
                    targetContainer.setText(selectedCardValueString);
                    changeCardColor(targetContainer, selectedCardValueInt);

                    if (success)
                    {
                        if (gameOver() || deck.size() ==0 && handIsEmpty())
                        {
                            finish();
                            getSharedPreferences("97CARDS", 0).edit().clear().commit();
                            Intent i = new Intent ( GameActivity.this,  GameoverActivity.class );
                            i.putExtra("SCORE", scoreContainer.getText());
                            startActivity(i);

                        }
                    }

                    selectedCardValueString = null;
                    selectedCardTextView = null;
                    break;
                default:
                    break;

            }
            return true;
        }

    }

    private class Ecouteur implements View.OnDragListener, View.OnTouchListener
    {

        @Override
        public boolean onDrag(View v, DragEvent event) {

            switch (event.getAction())
            {

                case DragEvent.ACTION_DRAG_STARTED:
                    if (((TextView)v).getText() == selectedCardValueString)
                    {
                        v.setBackgroundResource(R.color.background_color);
                        ((TextView) v).setText("");
                    }
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    if (selectedCardValueString != null)
                    {
                        selectedCardTextView.setText(selectedCardValueString);
                        changeCardColor(selectedCardTextView, Integer.parseInt(selectedCardValueString));
                        selectedCardValueString = null;
                        break;
                    }
                    break;
                case DragEvent.ACTION_DROP:
                    if (selectedCardValueString != null)
                    {
                        selectedCardTextView.setText(selectedCardValueString);
                        changeCardColor(selectedCardTextView, Integer.parseInt(selectedCardValueString));
                        selectedCardValueString = null;
                        break;
                    }
                default:
                    break;

            }
            return true;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
            v.startDragAndDrop(null, shadowBuilder, v, 0);
            selectedCardTextView = (TextView)v;
            selectedCardValueString = (String)selectedCardTextView.getText();

            return true;
        }
    }

    private boolean handIsEmpty()
    {
        boolean handIsEmpty = true;

        for(int i=0; i<8; i++)
        {
            if (!((String)((TextView)cards.getChildAt(i)).getText()).contentEquals(""))
            {
                handIsEmpty = false;
                break;

            }
        }
        return handIsEmpty;
    }

    private boolean gameOver()
    {
        //Comme ici il y a 2x deux boucles imbriquées avec beaucoup de if, pour la performance j'ai 3 return statement
        //Je me suis dit que c'était approprié dans ce cas-ci
        int cardOnPileValue;
        int cardInHandValueInt;
        String cardInHandValueString;
        for (Integer ascendingCardId : ascendingCardIds)
        {
            cardOnPileValue = Integer.parseInt((String) (((TextView)findViewById(ascendingCardId))).getText());
            for(int j=0; j<8; j++)
            {
                cardInHandValueString = (String)((TextView)cards.getChildAt(j)).getText();
                if (! cardInHandValueString.contentEquals(""))
                {
                    cardInHandValueInt = Integer.parseInt(cardInHandValueString);

                    if (cardInHandValueInt > cardOnPileValue ||  cardOnPileValue - cardInHandValueInt == 10)
                    {
                        return false;
                    }
                }
            }
        }
        for (Integer descendingCardId : descendingCardIds)
        {
            cardOnPileValue = Integer.parseInt((String) (((TextView)findViewById(descendingCardId))).getText());
            for(int j=0; j<8; j++)
            {
                cardInHandValueString = (String)((TextView)cards.getChildAt(j)).getText();
                if (! cardInHandValueString.contentEquals(""))
                {
                    cardInHandValueInt = Integer.parseInt(cardInHandValueString);
                    if (cardInHandValueInt < cardOnPileValue ||  cardInHandValueInt - cardOnPileValue == 10)
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
