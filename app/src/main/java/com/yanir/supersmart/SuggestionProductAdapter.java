package com.yanir.supersmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SuggestionProductAdapter extends RecyclerView.Adapter<SuggestionProductAdapter.SuggestionViewHolder> {

    public interface OnBarcodeClickListener {
        void onBarcodeClick(String barcode);
    }

    private List<String> barcodeList;
    private OnBarcodeClickListener listener;

    public SuggestionProductAdapter(List<String> barcodeList) {
        this.barcodeList = barcodeList;
    }

    public void setOnBarcodeClickListener(OnBarcodeClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        String barcode = barcodeList.get(position);
        holder.tvBarcode.setText("Barcode: " + barcode);
        holder.tvImageCount.setText("Images: (loading...)"); // Placeholder

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBarcodeClick(barcode);
            }
        });
    }

    @Override
    public int getItemCount() {
        return barcodeList.size();
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvBarcode, tvImageCount;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBarcode = itemView.findViewById(R.id.tvBarcode);
            tvImageCount = itemView.findViewById(R.id.tvImageCount);
        }
    }
}
