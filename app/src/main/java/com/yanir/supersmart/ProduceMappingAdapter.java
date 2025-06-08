package com.yanir.supersmart;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying and editing a list of produce names and their associated numeric codes.
 */
public class ProduceMappingAdapter extends RecyclerView.Adapter<ProduceMappingAdapter.ViewHolder> {

    private final List<String> produceNames;
    private final Map<String, Integer> produceMap;

    /**
     * Constructor for the adapter.
     *
     * @param produceMap The map containing produce names and their corresponding codes.
     */
    public ProduceMappingAdapter(Map<String, Integer> produceMap) {
        this.produceMap = produceMap;
        this.produceNames = new ArrayList<>(produceMap.keySet());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_produce_mapping, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String produceName = produceNames.get(position);
        holder.produceNameTextView.setText(produceName);
        holder.codeEditText.setText(String.valueOf(produceMap.get(produceName)));

        holder.codeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int code = Integer.parseInt(s.toString());
                    produceMap.put(produceName, code);
                } catch (NumberFormatException e) {
                    // Ignore invalid input
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return produceNames.size();
    }

    /**
     * ViewHolder for the RecyclerView items.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView produceNameTextView;
        EditText codeEditText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            produceNameTextView = itemView.findViewById(R.id.tvProduceName);
            codeEditText = itemView.findViewById(R.id.etProduceCode);
        }
    }

    /**
     * Returns the current mapping of produce names to their numeric codes.
     *
     * @return Map of produce names to codes.
     */
    public Map<String, Integer> getCurrentMappings() {
        return produceMap;
    }
}
