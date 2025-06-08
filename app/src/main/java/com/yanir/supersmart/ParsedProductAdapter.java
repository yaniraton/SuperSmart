
package com.yanir.supersmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Adapter for displaying a list of parsed products in a RecyclerView.
 * Each product is displayed with its barcode, name, and price.
 */
public class ParsedProductAdapter extends RecyclerView.Adapter<ParsedProductAdapter.ProductViewHolder> {

    private List<Product> productList;

    /**
     * Constructs a new ParsedProductAdapter with the given list of products.
     *
     * @param productList List of Product objects to display.
     */
    public ParsedProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    /**
     * Inflates the item view and creates a new ProductViewHolder.
     *
     * @param parent The parent view group.
     * @param viewType The view type of the new View.
     * @return A new ProductViewHolder instance.
     */
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_parsed_product, parent, false);
        return new ProductViewHolder(view);
    }

    /**
     * Binds product data to the views in the given holder.
     *
     * @param holder   The ProductViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvBarcode.setText("Barcode: " + product.getBarcode());
        holder.tvName.setText("Name: " + product.getName());
        holder.tvPrice.setText("Price: ₪" + product.getPrice());
    }

    /**
     * Returns the total number of items in the list.
     *
     * @return The size of the product list.
     */
    @Override
    public int getItemCount() {
        return productList.size();
    }

    /**
     * ViewHolder class for displaying individual product items.
     */
    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBarcode, tvPrice;

        /**
         * Constructs a new ProductViewHolder and initializes its views.
         *
         * @param itemView The item view to bind.
         */
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBarcode = itemView.findViewById(R.id.tvBarcode);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}
