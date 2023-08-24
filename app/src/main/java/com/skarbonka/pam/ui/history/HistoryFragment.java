package com.skarbonka.pam.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skarbonka.pam.databinding.FragmentHistoryBinding;
import com.skarbonka.pam.UserData;
import com.skarbonka.pam.R;

import org.json.JSONArray;
import org.json.JSONException;

public class HistoryFragment extends Fragment {

    private UserData userData = UserData.getInstance();
    private JSONArray transactionsArray = userData.getTransactionsArray();
    private FragmentHistoryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HistoryViewModel historyViewModel =
                new ViewModelProvider(this).get(HistoryViewModel.class);

        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        if(userData.getCzyZalogowany()){
            RecyclerView recyclerView = binding.RecView;
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            HistoryAdapter historyAdapter = new HistoryAdapter(transactionsArray);
            recyclerView.setAdapter(historyAdapter);
        }


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}