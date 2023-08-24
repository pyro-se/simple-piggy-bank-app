package com.skarbonka.pam.ui.cash;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.skarbonka.pam.UserData;

public class CashViewModel extends ViewModel {
    //UserData userData = UserData.getInstance();
    private MutableLiveData<Double> accountBalance;

    public CashViewModel() {
        accountBalance = new MutableLiveData<>();
        accountBalance.setValue(0.00);
    }

    public LiveData<Double> getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(double balance) {
        accountBalance.setValue(balance);
    }
}