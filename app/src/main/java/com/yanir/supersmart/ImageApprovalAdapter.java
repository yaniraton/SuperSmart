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

public class ImageApprovalAdapter extends RecyclerView.Adapter<ImageApprovalAdapter.ImageViewHolder> {

    public interface OnImageActionListener {
        void onApprove(StorageReference ref);
        void onDeny(StorageReference ref);
    }

    private List<StorageReference> imageRefs;
    private OnImageActionListener listener;

    public ImageApprovalAdapter(List<StorageReference> imageRefs, OnImageActionListener listener) {
        this.imageRefs = imageRefs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_approval, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        StorageReference imageRef = imageRefs.get(position);

        Glide.with(holder.itemView.getContext())
                .load(imageRef)
                .into(holder.imageView);

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(imageRef));
        holder.btnDeny.setOnClickListener(v -> listener.onDeny(imageRef));
    }

    @Override
    public int getItemCount() {
        return imageRefs.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        Button btnApprove, btnDeny;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivSuggestedImage);
            btnApprove = itemView.findViewById(R.id.btnApproveImage);
            btnDeny = itemView.findViewById(R.id.btnDenyImage);
        }
    }
}
