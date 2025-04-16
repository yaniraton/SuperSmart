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

public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder> {

    private final List<StorageReference> imageRefs;
    private final product_screen productScreen;

    public ProductImageAdapter(List<StorageReference> imageRefs, product_screen productScreen) {
        this.imageRefs = imageRefs;
        this.productScreen = productScreen;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(productScreen).inflate(R.layout.item_product_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        StorageReference imageRef = imageRefs.get(position);
        Glide.with(productScreen)
                .load(imageRef)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageRefs.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivProductThumbnail);
        }
    }
}
