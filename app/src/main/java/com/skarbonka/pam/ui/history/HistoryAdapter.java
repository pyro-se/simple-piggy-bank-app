package com.skarbonka.pam.ui.history;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.skarbonka.pam.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private JSONArray transactionsArray;

    public HistoryAdapter(JSONArray transactionsArray) {
        this.transactionsArray = transactionsArray;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject transaction = transactionsArray.getJSONObject(position);

            String id = transaction.getString("id");
            String transactionType = transaction.getString("typ_transakcji");
            String opis = transaction.getString("nazwa");
            double amount = transaction.getDouble("kwota");
            String date = transaction.getString("data");
            double balance = transaction.getDouble("stan_konta_po");

            if(transactionType.equals("wypłata")){
                holder.imageView.setImageResource(R.drawable.wyplata);
            }
            else{
                holder.imageView.setImageResource(R.drawable.wplata);
            }

            //holder.idTextView.setText(id);
            //holder.typeTextView.setText(transactionType);
            holder.nameTextView.setText(opis);
            holder.amountTextView.setText(String.valueOf(amount)+" PLN");
            holder.dateTextView.setText(date);
            holder.balanceTextView.setText(": [" + String.valueOf(balance) + " PLN]");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return transactionsArray.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        //public TextView idTextView;
        //public TextView typeTextView;
        public ImageView imageView;
        public TextView amountTextView;
        public TextView dateTextView;
        public TextView balanceTextView;
        public TextView nameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            //idTextView = itemView.findViewById(R.id.textViewId);
            //typeTextView = itemView.findViewById(R.id.textViewTransactionType);
            imageView = itemView.findViewById(R.id.imageView);
            amountTextView = itemView.findViewById(R.id.textKwota);
            dateTextView = itemView.findViewById(R.id.textDate);
            nameTextView = itemView.findViewById(R.id.textOpis);
            balanceTextView = itemView.findViewById(R.id.textStanPo);
        }
    }
}
