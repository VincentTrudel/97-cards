package com.example.tpfinal;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Vector;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance;
    private SQLiteDatabase database;

    public static DatabaseHelper getInstance(Context contexte) {

        if (instance == null){
            instance = new DatabaseHelper(contexte.getApplicationContext());
        }

        return instance;
    }

    private DatabaseHelper(Context contexte) {
        super(contexte, "db", null, 1);
        ouvrirBD();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    //La méthode onCreate n'est exécutée qu'une seule fois lors de l'installation

        db.execSQL("create table scores ( _id INTEGER PRIMARY KEY AUTOINCREMENT, score INTEGER UNIQUE NOT NULL);");

        ajouterScore(new Score(0), db);
    }

    public void ajouterScore (Score s, SQLiteDatabase db){
        ContentValues cv = new ContentValues();
        cv.put("score", s.getValeur());
        db.insert ("scores", null, cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    //la plateforme vérifie si la plateforme avec une bd existe, sinon, oncreate, sinon vérifie la version
        //db.execSQL("delete from inventeurs"); //changer la table pour scores
        onCreate(db);
    }


    public void ouvrirBD(){
        database = this.getWritableDatabase();
    }

    public void fermerBD(){
        database.close();
    }

    public int highestScore ()
    {

        Cursor c = database.rawQuery("SELECT MAX(score) FROM scores", null);
        c.moveToFirst();

        return c.getInt(0);
    }
}


    /* execSQL: pour la majorité des énoncés, sauf INSERT et SELECT
    *insert: 3 paramètres: nomTable, null (nom d'un champ si on désire que toutes les valeurs de ce champ soit null,
    * ContentValues contentValues; fonctionne comme une hashtable clé - valeur (nom des champs)
    *
    *
    * */




