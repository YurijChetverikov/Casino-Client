package com.pgp.casinoclient.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kotlin.UInt;
import kotlin.UShort;

public class Player {

    public String Name;
    public int Balance = 0;
    public int ID = 0;
    public int Password = 0;
    public byte TransactionsLeft = 0;
    public Date RegistrationDate = new Date(0);
    public boolean IsCasino = false;
    public ArrayList<Transaction> Transactions = new ArrayList<Transaction>(0);
    public ArrayList<GameSession> GameSessions = new ArrayList<GameSession>(0);

    public GameSession CurrentSession = null;
    public GameDataBlock CurrentDataBlock = null;


    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 'в' HH:mm:ss z");


    public Player(int id, int password, String name, int balance){
        ID = id;
        Password = password;
        Name = name;
        Balance = balance;
    }

    // Упрощённый конструктор для игроков в транзакциях
    public Player(int id, String name){
        ID = id;
        Name = name;
    }


    public String GetRegistrationDateString(){
        return sdf.format(RegistrationDate);
    }

    public String GetPasswordString(){
        return String.format("%04d", Password);
    }
}
