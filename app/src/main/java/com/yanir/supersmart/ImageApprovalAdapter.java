package com.yanir.supersmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.StorageReference;

import java.util.List;

/**
 * Adapter for displaying a list of image suggestions with approval and denial options.
 * This adapter uses Firebase StorageReferences to load images and notifies the listener
 * on approval or denial actions.
 */
public class ImageApprovalAdapter extends RecyclerView.Adapter<ImageApprovalAdapter.ImageViewHolder> {

    /**
     * Listener interface for image approval actions.
     */
    public interface OnImageActionListener {
        void onApprove(StorageReference ref);
        void onDeny(StorageReference ref);
    }

    /** List of Firebase StorageReferences pointing to the suggested images. */
    private List<StorageReference> imageRefs;

    /** Callback listener for handling approval or denial actions. */
    private OnImageActionListener listener;

    public ImageApprovalAdapter(List<StorageReference> imageRefs, OnImageActionListener listener) {
        this.imageRefs = imageRefs;
        this.listener = listener;
    }

    /**
     * Inflates the item layout and creates a new ViewHolder.
     *
     * @param parent   The parent ViewGroup.
     * @param viewType The view type of the new View.
     * @return A new ImageViewHolder instance.
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_approval, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Binds the image and buttons to the ViewHolder at the specified position.
     *
     * @param holder   The ViewHolder to bind.
     * @param position The position in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        StorageReference imageRef = imageRefs.get(position);

        Glide.with(holder.itemView.getContext())
                .load(imageRef)
                .into(holder.imageView);

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(imageRef));
        holder.btnDeny.setOnClickListener(v -> listener.onDeny(imageRef));
    }

    /**
     * Returns the number of items in the adapter.
     *
     * @return The size of the image reference list.
     */
    @Override
    public int getItemCount() {
        return imageRefs.size();
    }

    /**
     * ViewHolder class for holding the image and buttons.
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        /** ImageView displaying the suggested image. */
        ImageView imageView;

        /** Button to approve the image. */
        Button btnApprove;

        /** Button to deny the image. */
        Button btnDeny;

        /**
         * Constructor that initializes the ViewHolder's views.
         *
         * @param itemView The view of the item.
         */
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivSuggestedImage);
            btnApprove = itemView.findViewById(R.id.btnApproveImage);
            btnDeny = itemView.findViewById(R.id.btnDenyImage);
        }
    }
}
