package com.pgp.casinoclient.ui;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.pgp.casinoclient.R;
import com.pgp.casinoclient.core.Player;
import com.pgp.casinoclient.core.Transaction;
import com.pgp.casinoclient.core.TransactionType;
import com.pgp.casinoclient.loaders.DataLoader;
import com.pgp.casinoclient.ui.animations.framgentchange.TransitionButton;

import java.io.IOException;

public class TransactionFragment extends Fragment {

    private Button button;
    private EditText receiverIdView;
    private TextView receiverNameView;
    private EditText amountView;
    private Spinner typeView;
    private EditText descView;
    private TextView comissionView;
    private TextView totalAmountView;


    private Player receiver;

    private View parentView;
    private ArrayAdapter<String> adapter = null;


    private int totalAmount = 0;
    private int amount = 0;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        parentView = inflater.inflate(R.layout.fragment_transactions, container, false);

        button = parentView.findViewById(R.id.confirmButton);
        receiverIdView = parentView.findViewById(R.id.receiver_id);
        receiverNameView = parentView.findViewById(R.id.receiver_name);
        amountView = parentView.findViewById(R.id.amount);
        typeView = parentView.findViewById(R.id.transactionType);
        descView = parentView.findViewById(R.id.desc);
        comissionView = parentView.findViewById(R.id.transactionCommission);
        totalAmountView = parentView.findViewById(R.id.totalAmount);



        comissionView.setText("Комиссия " +  Byte.toString(DataLoader.Singleton().TransactionComission) + "%");

        adapter = new ArrayAdapter(parentView.getContext(),
                R.layout.simple_spinner_layout, TransactionType.GetNames());

        adapter.setDropDownViewResource(R.layout.simple_spinner_layout);
        typeView.setAdapter(adapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransactionType type = TransactionType.Get(typeView.getSelectedItemPosition());
                String desc = descView.getText().toString();

                Transaction t = new Transaction();
                t.Sender = DataLoader.Singleton().CurrentPlayer;
                t.Receiver = receiver;
                t.Type = type;
                t.Description = desc;
                t.Amount = totalAmount;
                t.SenderPaid = amount;

                int transId = DataLoader.Singleton().getNextTransactionID();

                DataLoader.Singleton().Transactions.put(transId, t);
                DataLoader.Singleton().CurrentPlayer.Transactions.add(DataLoader.Singleton().Transactions.get(transId));
                receiver.Transactions.add(DataLoader.Singleton().Transactions.get(transId));
//                try {
//                    //DataLoader.Singleton().WriteBigDataCache(parentView.getContext());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
                Toast.makeText(parentView.getContext(), "Транзакция выполнена!", Toast.LENGTH_SHORT).show();
            }
        });

        button.setEnabled(false);




        receiverIdView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (receiverIdView.getText().toString().length() > 0) {
                    onFieldUpdate();
                }else{
                    receiver = null;
                    receiverNameView.setText("");
                    button.setEnabled(false);
                }
            }
        });

        amountView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (amountView.getText().toString().length() > 0){
                    amount = Integer.parseInt(amountView.getText().toString());
                    onFieldUpdate();
                }else{
                    amount = 0;
                    amountView.setError(getString(R.string.invalid_money_amount));
                    button.setEnabled(false);
                }
            }
        });

        return parentView;
    }


    private void onFieldUpdate(){
        int id = Integer.parseInt(receiverIdView.getText().toString());
        Player founded = DataLoader.Singleton().GetPlayerById(id);
        if (founded != null){
            if (!founded.IsCasino){
                receiver = founded;
                receiverIdView.setError(null);
                receiverNameView.setText(receiver.Name);

                if (amount > 0){
                    if (DataLoader.Singleton().CurrentPlayer.Balance >= amount){
                        amountView.setError(null);
                        float comissionFloat = (float)((amount * DataLoader.Singleton().TransactionComission) / 100);
                        if (comissionFloat < 1 && DataLoader.Singleton().TransactionComission != 0){
                            comissionFloat = 1;
                        }
                        int comissionAmount = (int)Math.ceil(comissionFloat);

                        totalAmount = amount - comissionAmount;
                        totalAmountView.setText("Итого к переводу: " + Integer.toString(totalAmount));
                        comissionView.setText("Комиссия " +  Byte.toString(DataLoader.Singleton().TransactionComission) + "%: " + Integer.toString(comissionAmount));
                        button.setEnabled(true);
                    }else{
                        amountView.setError(getString(R.string.error_not_enough_money));
                        button.setEnabled(false);
                    }
                }else{
                    amountView.setError(getString(R.string.invalid_money_amount));
                    button.setEnabled(false);
                }
            }else{
                receiver = null;
                receiverNameView.setText("");
                receiverIdView.setError(getString(R.string.missing_player_with_id));
                button.setEnabled(false);
            }

        }else{
            receiver = null;
            receiverNameView.setText("");
            receiverIdView.setError(getString(R.string.missing_player_with_id));
            button.setEnabled(false);
        }
    }

}
