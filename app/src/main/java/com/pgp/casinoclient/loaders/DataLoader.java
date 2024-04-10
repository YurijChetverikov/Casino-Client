package com.pgp.casinoclient.loaders;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.pgp.casinoclient.core.Game;
import com.pgp.casinoclient.core.GameDataBlock;
import com.pgp.casinoclient.core.GameSession;
import com.pgp.casinoclient.core.Player;
import com.pgp.casinoclient.core.Transaction;
import com.pgp.casinoclient.core.TransactionType;
import com.pgp.casinoclient.ui.MainActivity;
import com.pgp.casinoclient.utils.BinaryUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DataLoader {
    private static DataLoader Singleton;


    public ArrayList<Player> Players = new ArrayList<Player>();
    public Player CurrentPlayer = null;
    public ArrayList<Game> Games = new ArrayList<Game>();
    public Map<Integer,Transaction> Transactions = new HashMap<Integer,Transaction>(0);
    public ArrayList<Player> PlayersInGameRoom = new ArrayList<Player>();
    private Bitmap casinoBitmap = null;
    public byte[] CompressedBitmap = new byte[0];
    public Game CurrentGame = null;

    public byte TransactionComission = 0; // В процентах



    final String PLAYERS_TABLE_FILENAME = "pl.bin";
    final String TRANSACTIONS_DATA_FILENAME = "dat1.dat";
    final String GAMEDATA_DATA_FILENAME = "dat2.dat";
    final String CASINO_IMAGE_FILENAME = "pic.dat";


    public static DataLoader Singleton(){
        if (Singleton == null){
            Singleton = new DataLoader();
        }

        return Singleton;
    }

    // Пишет и файл и игроками, и файл с их данными
    public void WriteAllCahce(@NonNull Context context) throws IOException {
        setSavingTitle(true);
        WriteTableCache(context);
        setSavingTitle(true);
        for (Player pl: Players){
            WriteDataCahce(context, pl);
            setSavingTitle(true);
        }
        setSavingTitle(false);
    }

    public void WriteTableCache(@NonNull Context context) throws IOException{
        setSavingTitle(true);
        FileOutputStream stream = context.openFileOutput(PLAYERS_TABLE_FILENAME, Context.MODE_PRIVATE);
        stream.write(getPlayersTable());
        stream.close();
        setSavingTitle(false);
    }


    // Пишет и файл с данными игроков
    public void WriteDataCahce(@NonNull Context context, @NonNull Player player) throws IOException {
        setSavingTitle(true);
        FileOutputStream stream = context.openFileOutput(String.format("pl%d.dat", player.ID), Context.MODE_PRIVATE);
        stream.write(getPlayerData(player));
        stream.close();
        setSavingTitle(false);
    }

    public void WriteCasinoImageCache(@NonNull Context context) throws IOException{
        FileOutputStream stream = context.openFileOutput(CASINO_IMAGE_FILENAME, Context.MODE_PRIVATE);
        if (CompressedBitmap != null){
            stream.write(CompressedBitmap);
        }else{
            stream.write(0);
        }
        stream.close();
    }

//    public void WriteBigDataCache(@NonNull Context context) throws IOException{
//        setSavingTitle(true);
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        outputStream.write(BinaryUtils.Int2Bytes(Transactions.size()));
//        for (Map.Entry<Integer, Transaction> t: Transactions.entrySet()){
//            outputStream.write(BinaryUtils.Int2Bytes(t.getKey()));
//            outputStream.write(BinaryUtils.Int2Bytes(t.getValue().Sender.ID));
//            outputStream.write(BinaryUtils.Int2Bytes(t.getValue().Receiver.ID));
//            outputStream.write(BinaryUtils.Int2Bytes(t.getValue().Amount));
//            outputStream.write(BinaryUtils.Int2Bytes(t.getValue().SenderPaid));
//            outputStream.write((byte)t.getValue().Type.ordinal());
//            outputStream.write(BinaryUtils.WriteString(t.getValue().Description));
//        }
//
//        FileOutputStream stream = context.openFileOutput(BIG_DATA_FILENAME, Context.MODE_PRIVATE);
//        stream.write(outputStream.toByteArray());
//        stream.close();
//
//        setSavingTitle(false);
//    }


    public CacheReadingResult ReadCache(@NonNull Context context) throws Exception {
        // Алгоритм такой:
        // Считываем игроков
        // Считываем все транзакции
        // Считываем все данные об сыгранных играх

        FileInputStream fin = null;
        byte[] bytes = null;
        try{
            fin = context.openFileInput(PLAYERS_TABLE_FILENAME);
        }catch(Exception ex){
            return CacheReadingResult.NOT_READED;
        }
        bytes = new byte[fin.available()];
        fin.read(bytes);
        fin.close();
        ByteBuffer b = ByteBuffer.wrap(bytes);
        b.position(16);

        try{
            // Парсим игрока, чей аккаунт

            CurrentPlayer = new Player(b.getInt(), b.getInt(), BinaryUtils.ReadString(b), b.getInt());
            byte flags = b.get(); // TODO: Inmortant
            CurrentPlayer.TransactionsLeft = b.get();
            CurrentPlayer.RegistrationDate = new Date(b.getLong());

            // Парсим какие игры бывают

            byte gamesCount = b.get();
            for(byte i = 0; i < gamesCount; i++){
                Game g = new Game();
                g.ID = b.get();
                g.Name = BinaryUtils.ReadString(b);
                Games.add(g);
            }

            TransactionComission = b.get();

            // Дальше парсим игроков, с которым у него были транзакции

            int playersCount = b.getInt();
            for(int i = 0; i < playersCount; i++){
                Players.add(new Player(b.getInt(), BinaryUtils.ReadString(b)));
            }
        }catch(Exception ex){
            return CacheReadingResult.READED_WITH_ERRORS;
        }




        // Дальше парсим транзакции игрока


        try{
            fin = context.openFileInput(TRANSACTIONS_DATA_FILENAME);

            bytes = new byte[fin.available()];
            fin.read(bytes);
            fin.close();
            b = ByteBuffer.wrap(bytes);

            try{
                int transCount = b.getInt();

                for(int i = 0; i < transCount; i++){
                    Transaction t = new Transaction();
                    int transID = b.getInt();
                    int senderId = b.getInt();
                    int receiverId = b.getInt();
                    t.Sender = GetPlayerById(senderId);
                    t.Receiver = GetPlayerById(receiverId);
                    t.Amount = b.getInt();
                    t.SenderPaid = b.getInt();
                    t.Type = TransactionType.Get((int)b.get());
                    t.Description = BinaryUtils.ReadString(b);
                    Transactions.put(transID, t);
                    GetPlayerById(senderId).Transactions.add(Transactions.get(transID));
                    GetPlayerById(receiverId).Transactions.add(Transactions.get(transID));
                }
            }catch(Exception ex){
                return CacheReadingResult.READED_WITH_ERRORS;
            }


        }catch(Exception ex){
            // Ничё не делаем, просто у игркоа нет сохранённых транзакций
        }

        // Дальше парсим игры игрока

        try{
            fin = context.openFileInput(GAMEDATA_DATA_FILENAME);

            bytes = new byte[fin.available()];
            fin.read(bytes);
            fin.close();
            b = ByteBuffer.wrap(bytes);

            try{
                int gameSessions = b.getInt();
                for (int i = 0; i < gameSessions; i++){
                    GameSession gs = new GameSession(b.getLong());
                    gs.EndDate = b.getLong();
                    gs.BalanceChange = b.getInt();
                    gs.WinsCount = b.getInt();
                    gs.LoseCount = b.getInt();
                    int dataBlockCount = b.getInt();
                    for (int j = 0; j < dataBlockCount; j++){
                        GameDataBlock g = new GameDataBlock(GetGameByID(b.get()), b.getLong());
                        g.EndDate = b.getLong();
                        g.WinsCount = b.getInt();
                        g.LoseCount = b.getInt();
                        gs.GameBlocks.add(g);
                    }
                    CurrentPlayer.GameSessions.add(gs);
                }
            }catch(Exception ex){
                return CacheReadingResult.READED_WITH_ERRORS;
            }

        }catch(Exception ex){
            // Ничё не делаем, просто у игркоа нет сохранённых игр
        }


        // Считываем картинку казина


        try{
            fin = context.openFileInput(CASINO_IMAGE_FILENAME);
            bytes = new byte[fin.available()];
            fin.read(bytes);
            fin.close();

            if (fin.available() > 4){
                b = ByteBuffer.wrap(bytes);
                CompressedBitmap = b.array();
                casinoBitmap = BitmapFactory.decodeByteArray(CompressedBitmap, 0, CompressedBitmap.length);
            }
        }catch(Exception ex){
            // Ничё не делаем, просто у игрока нет сохранённой картинки казина
        }



        return CacheReadingResult.READED_SUCCESSFULLY;
    }


    @Nullable
    private byte[] getPlayerData(@NonNull Player pl) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(BinaryUtils.Int2Bytes(pl.GameSessions.size()));
        for (GameSession gs : pl.GameSessions){
            outputStream.write(BinaryUtils.Long2Bytes(gs.StartDate));
            outputStream.write(BinaryUtils.Long2Bytes(gs.EndDate));
            outputStream.write(BinaryUtils.Int2Bytes(gs.BalanceChange));
            outputStream.write(BinaryUtils.Int2Bytes(gs.WinsCount));
            outputStream.write(BinaryUtils.Int2Bytes(gs.LoseCount));
            outputStream.write(BinaryUtils.Int2Bytes(gs.GameBlocks.size()));
            for (GameDataBlock g : gs.GameBlocks){
                outputStream.write(g.Game.ID);
                outputStream.write(BinaryUtils.Long2Bytes(g.StartDate));
                outputStream.write(BinaryUtils.Long2Bytes(g.EndDate));
                outputStream.write(BinaryUtils.Int2Bytes(g.WinsCount));
                outputStream.write(BinaryUtils.Int2Bytes(g.LoseCount));
            }
        }

        return outputStream.toByteArray();
    }


    @NonNull
    private byte[] getPlayersTable() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(new byte[16]); // резерв
        if (CurrentPlayer != null){
            outputStream = writePlayer(CurrentPlayer, outputStream);
            outputStream.write(Games.size());

            for (Game g: Games){
                outputStream.write(g.ID);
                outputStream.write(BinaryUtils.WriteString(g.Name));
            }

            outputStream.write(TransactionComission);
            outputStream.write(BinaryUtils.Int2Bytes(Players.size()));

            for (Player pl: Players) {
                if (pl.ID != CurrentPlayer.ID){
                    outputStream = writePlayer(pl, outputStream);
                }
            }
        }

        byte[] res = outputStream.toByteArray();

        outputStream.close();

        return res;
    }


    private ByteArrayOutputStream writePlayer(@NonNull Player pl, @NonNull ByteArrayOutputStream outputStream) throws IOException {
        outputStream.write(BinaryUtils.Int2Bytes(pl.ID));
        outputStream.write(BinaryUtils.Int2Bytes(pl.Password));
        outputStream.write(BinaryUtils.WriteString(pl.Name));
        outputStream.write(BinaryUtils.Int2Bytes(pl.Balance));
        outputStream.write(0); // Флаги
        outputStream.write(pl.TransactionsLeft);
        outputStream.write(BinaryUtils.Long2Bytes(pl.RegistrationDate.getTime()));

        return outputStream;
    }


    @Nullable
    public Player GetPlayerById(int id){
        for(Player pl: Players){
            if (pl.ID == id){
                return pl;
            }
        }

        return null;
    }

    public int GetNextFreeID(){
        return Players.size();
    }

    // Если возвратит 255 => все id заняты
    public byte GetNextFreeGameID(){
        boolean correct = true;
        for (byte i = 0; i < 255; i++){
            for (Game g: Games) {
                if (g.ID == i){
                    correct = false;
                    break;
                }
            }
            if (correct){
                return i;
            }
            correct = true;
        }

        return (byte)255;
    }

    public int getNextTransactionID(){
        if (Transactions.keySet().size() == 0){
            return 0;
        }
        return (int)Transactions.keySet().toArray()[Transactions.keySet().size() - 1];
    }


    public Game GetGameByID(byte id){
        for(Game g: Games){
            if (g.ID == id){
                return g;
            }
        }

        return null;
    }

    public Bitmap getCasinoBitmap(){
        return casinoBitmap;
    }

    public void setCasinoBitmap(@NonNull Bitmap newImage){
        casinoBitmap = newImage.copy(newImage.getConfig(), true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        newImage.compress(Bitmap.CompressFormat.WEBP, 100, stream);
        CompressedBitmap = stream.toByteArray();
        newImage.recycle();


    }


    private boolean isSaving = false;
    private String oldTitle;
    private void setSavingTitle(boolean saving){
        if (MainActivity.Singleton() != null){
            if (isSaving != saving){
                if (isSaving == true){
                    //MainActivity.Singleton().setToolbarTitle(oldTitle);
                }else{
                    //oldTitle = MainActivity.Singleton().getToolbarTitle();
                    //MainActivity.Singleton().setToolbarTitle( oldTitle + " (Сохр.)");
                }
            }
        }
        isSaving = saving;
    }
}

