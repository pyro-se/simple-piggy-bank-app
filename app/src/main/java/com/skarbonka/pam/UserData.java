package com.skarbonka.pam;

import org.json.JSONArray;

public class UserData {
    private static UserData instance;
    private double accountBalance;
    private JSONArray transactionsArray;
    private boolean czyZalogowany;

    private UserData() {
        // Prywatny konstruktor Singletona
        accountBalance = 0.00;
        transactionsArray = new JSONArray();
        czyZalogowany = false;
    }

    public static UserData getInstance() {
        if (instance == null) {
            synchronized (UserData.class) {
                if (instance == null) {
                    instance = new UserData();
                }
            }
        }
        return instance;
    }

    public double getAccountBalance() {return accountBalance;}
    public boolean getCzyZalogowany() {return czyZalogowany;}

    public void setAccountBalance(double accountBalance) {
        this.accountBalance = accountBalance;
    }
    public void setCzyZalogowany(boolean czyZalogowany) {this.czyZalogowany = czyZalogowany;}

    public JSONArray getTransactionsArray() {
        return transactionsArray;
    }

    public void setTransactionsArray(JSONArray transactionsArray) {
        this.transactionsArray = transactionsArray;
    }
}
