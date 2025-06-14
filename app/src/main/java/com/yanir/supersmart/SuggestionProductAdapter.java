package com.yanir.supersmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView Adapter to display a list of product suggestions based on barcodes.
 * Allows users to click on an item to trigger a callback.
 */
public class SuggestionProductAdapter extends RecyclerView.Adapter<SuggestionProductAdapter.SuggestionViewHolder> {

    /**
     * Interface for handling barcode click events.
     */
    public interface OnBarcodeClickListener {
        /**
         * Called when a barcode item is clicked.
         *
         * @param barcode The clicked barcode string.
         */
        void onBarcodeClick(String barcode);
    }

    private List<String> barcodeList;
    private OnBarcodeClickListener listener;

    /**
     * Constructs the adapter with a list of barcode strings.
     *
     * @param barcodeList List of barcode strings to display.
     */
    public SuggestionProductAdapter(List<String> barcodeList) {
        this.barcodeList = barcodeList;
    }

    /**
     * Sets the barcode click listener.
     *
     * @param listener The listener to handle barcode click events.
     */
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
        holder.tvImageCount.setText(""); // Placeholder

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

    /**
     * ViewHolder class for barcode suggestion items.
     */
    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvBarcode, tvImageCount;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBarcode = itemView.findViewById(R.id.tvBarcode);
            tvImageCount = itemView.findViewById(R.id.tvImageCount);
        }
    }
}
