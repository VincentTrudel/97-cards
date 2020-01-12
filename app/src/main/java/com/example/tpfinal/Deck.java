package com.example.tpfinal;
import java.util.Stack;
import java.util.Collections;


public class Deck{
    private Stack<Integer> cardList;


    public Deck() {

    }

    public void createAndShuffle()
    {
        Stack<Integer> cardList = new Stack<>();
        for (int i=1; i<98; i++)
        {
            cardList.add(new Integer(i));
        }
        Collections.shuffle(cardList);
        this.cardList = cardList;
    }


    public int draw()
    {

        return cardList.pop();
    }

    public int size()
    {
        return getCardList().size();
    }

    public Stack<Integer> getCardList()
    {
        return cardList;
    }

    public void setCardList( Stack<Integer> cardList)
    {
        this.cardList = cardList;
    }
}
