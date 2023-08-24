package com.skarbonka.pam.ui.cash;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.widget.Toast;

import com.skarbonka.pam.R;
import com.skarbonka.pam.databinding.FragmentCashBinding;
import com.skarbonka.pam.UserData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.PrimitiveIterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class CashFragment extends Fragment {

    private FragmentCashBinding binding;
    private CashViewModel cashViewModel;
    private RadioButton radio_deposit, radio_withdraw;
    private EditText transactionValueEditText, transactionTitleEditText;
    private TextView balanceAfterTextView, accountBalanceTextView, balanceAfterTitle;
    private Button SubmitBTN;
    UserData userData = UserData.getInstance();
    double accountBalance = userData.getAccountBalance();
    JSONArray transactionsArray = userData.getTransactionsArray();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cashViewModel = new ViewModelProvider(requireActivity()).get(CashViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCashBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        SubmitBTN = binding.SubmitBTN;
        balanceAfterTitle = binding.balanceAfterTitle;
        transactionValueEditText = binding.TransactionValue;
        transactionTitleEditText = binding.TransactionTitle;
        accountBalanceTextView = root.findViewById(R.id.account_balance);
        balanceAfterTextView = binding.balanceAfter;
        balanceAfterTextView.setText("");

        if(userData.getCzyZalogowany()){
            SubmitBTN.setVisibility(View.VISIBLE);
            accountBalanceTextView.setVisibility(View.VISIBLE);
            balanceAfterTitle.setVisibility(View.VISIBLE);
            balanceAfterTextView.setVisibility(View.VISIBLE);
            accountBalanceTextView.setText("Stan konta: " + accountBalance + " PLN");
            balanceAfterTitle.setText("Stan konta po operacji:");
        }

        radio_deposit = binding.Deposit;
        radio_deposit.setChecked(true);
        radio_withdraw = binding.Withdraw;

        radio_deposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radio_deposit.isChecked()) {
                    radio_withdraw.setChecked(false);
                }
            }
        });

        radio_withdraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (radio_withdraw.isChecked()) {
                    radio_deposit.setChecked(false);
                }
            }
        });

        transactionValueEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String transactionValue = charSequence.toString();
                if (!transactionValue.isEmpty()) {
                    double value = Double.parseDouble(transactionValue);
                    if(radio_withdraw.isChecked()) {value = -value;}
                    if(radio_deposit.isChecked()){value = Math.abs(value);}
                    double stan_tmp = userData.getAccountBalance();//cashViewModel.getAccountBalance().getValue();
                    double balanceAfter = stan_tmp + value;
                    updateBalanceAfter(balanceAfter, value);

                    // Zmień kolor tła i tekstu w zależności od możliwości transakcji
                    if (balanceAfter >= 0) {
                        balanceAfterTextView.setTextColor(getResources().getColor(R.color.green));
                    }else if (balanceAfter < 0) {
                        balanceAfterTextView.setTextColor(getResources().getColor(R.color.red));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        SubmitBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String transactionValue = transactionValueEditText.getText().toString();
                String transactionTitle = transactionTitleEditText.getText().toString();
                if (radio_withdraw.isChecked() || radio_deposit.isChecked()){
                    double value = Double.parseDouble(transactionValue);
                    if (!transactionValue.isEmpty() && !transactionTitle.isEmpty() && value!=0) {
                        if (radio_withdraw.isChecked()) {
                            value = -value;
                        }
                        double newBalance = userData.getAccountBalance() + value;//cashViewModel.getAccountBalance().getValue() + value;
                        if (newBalance >= 0){
                            //cashViewModel.setAccountBalance(newBalance);
                            showToast("Transakcja udana");
                            try {
                                transactionNewEntry(value, newBalance, transactionTitle);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }

                            accountBalanceTextView.setText("Stan konta: $" + newBalance);
                            UserData.getInstance().setAccountBalance(newBalance);
                            ///////////////////////////////
                            transactionValueEditText.setText("");
                            transactionTitleEditText.setText("");
                            updateBalanceAfter(0.00, 0.00);
                            accountBalance = userData.getAccountBalance();
                        }
                        else {showToast("Transakcja nieudana - brak wystarczających środków");}
                    }
                    else if(Math.abs(value)==0){showToast("Transakcja nie może być zerowa!");}
                    else{showToast("Brakujące dane do transakcji!");}
                }
                else {showToast("Wybierz rodzaj transakcji");}

            }
        });

        return root;
    }
    private void transactionNewEntry(double kwota, double stan_po, String nazwa) throws JSONException {
        // Nowy wpis do dodania

        String typ = "wpłata";
        if(kwota<0){
            typ = "wypłata";
        }
        JSONObject entry = new JSONObject();
        entry.put("id", 1);
        entry.put("typ_transakcji", typ);
        entry.put("kwota", kwota);
        entry.put("nazwa", nazwa);
        entry.put("data", getCurrentDate());
        entry.put("stan_konta_po", stan_po);
        transactionsArray.put(entry);

        UserData.getInstance().setTransactionsArray(transactionsArray); //szybki zapis do globalnej
        transactionsArray = UserData.getInstance().getTransactionsArray();
    }
    private void updateBalanceAfter(double balance, double transactionAmount) {
        String balanceAfterText = "";
        if (transactionAmount>0){
            balanceAfterText = String.format("%.2f PLN [+%.2f]", balance, transactionAmount);
        }
        else if (transactionAmount<0 && balance>transactionAmount){
            balanceAfterText = String.format("%.2f PLN [%.2f]", balance, transactionAmount);
        }
        balanceAfterTextView.setText(balanceAfterText);
    }

    public String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String currentDate = dateFormat.format(new Date());

        LocalTime currentTime = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = currentTime.format(formatter);

        String wynik = (currentDate + " - " + formattedTime);
        return wynik;
    }
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

