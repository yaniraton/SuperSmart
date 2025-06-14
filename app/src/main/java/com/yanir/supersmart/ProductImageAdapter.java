package com.yanir.supersmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * Adapter class for displaying product images using Firebase Storage references in a RecyclerView.
 * Each image is represented by a {@link StorageReference} and displayed using Glide.
 *
 * This adapter is used in the {@link ProductViewActivity} activity to present product thumbnails.
 */
public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder> {

    private final List<StorageReference> imageRefs;
    private final ProductViewActivity productScreen;

    /**
     * Constructs a new ProductImageAdapter.
     *
     * @param imageRefs      List of StorageReference objects pointing to the product images in Firebase Storage.
     * @param productScreen  Context/activity where the images are displayed.
     */
    public ProductImageAdapter(List<StorageReference> imageRefs, ProductViewActivity productScreen) {
        this.imageRefs = imageRefs;
        this.productScreen = productScreen;
    }

    @NonNull
    /**
     * Inflates the layout for each product image item in the RecyclerView.
     *
     * @param parent   The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new View.
     * @return A new ImageViewHolder instance containing the inflated view.
     */
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(productScreen).inflate(R.layout.item_product_image, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Binds the StorageReference image to the ViewHolder using Glide.
     *
     * @param holder   The ViewHolder which should be updated.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        StorageReference imageRef = imageRefs.get(position);
        Glide.with(productScreen)
                .load(imageRef)
                .into(holder.imageView);
    }

    /**
     * Returns the total number of images to be displayed.
     *
     * @return The size of the imageRefs list.
     */
    @Override
    public int getItemCount() {
        return imageRefs.size();
    }

    /**
     * ViewHolder class for holding the ImageView for a single product image.
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivProductThumbnail);
        }
    }
}
