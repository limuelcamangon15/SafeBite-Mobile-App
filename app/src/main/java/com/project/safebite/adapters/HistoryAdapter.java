package com.project.safebite.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.safebite.R;
import com.project.safebite.model.Product;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>{

    private List<Product> historyList;
    private Context context;

    public HistoryAdapter(Context context, List<Product> historyList){
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryAdapter.HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.fragment_history_scan_item, parent, false);
        return new HistoryAdapter.HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.HistoryViewHolder holder, int position){

        Product product = historyList.get(position);

        holder.tvBrand.setText(product.getBrand());
        holder.tvName.setText(product.getName());
        holder.tvBarcode.setText(product.getBarcode());

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy | hh:mm a", Locale.getDefault());
        String timestamp = sdf.format(new Date(product.getScannedAt()));
        holder.tvTimestamp.setText(timestamp);

    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }


    public static class HistoryViewHolder extends RecyclerView.ViewHolder{

        TextView tvBrand, tvName, tvBarcode, tvTimestamp;

        public HistoryViewHolder(@NonNull View historyView){
            super(historyView);
            tvBrand = historyView.findViewById(R.id.tvBrand);
            tvName = historyView.findViewById(R.id.tvName);
            tvBarcode = historyView.findViewById(R.id.tvBarcode);
            tvTimestamp = historyView.findViewById(R.id.tvTimestamp);
        }
    }
}
