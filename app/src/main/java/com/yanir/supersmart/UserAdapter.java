package com.yanir.supersmart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.yanir.supersmart.R;
import com.yanir.supersmart.User;

import java.util.List;


/**
 * Adapter for displaying a list of users in a RecyclerView.
 * This adapter is used in the admin panel to allow administrators to view users
 * and toggle their admin permission status (approve or revoke).
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    private Context context;

    /**
     * Constructs a new UserAdapter.
     *
     * @param context the context in which the adapter is used
     * @param userList the list of users to display
     */
    public UserAdapter(Context context, List<User> userList) {
        this.userList = userList;
        this.context = context;
    }

    /**
     * Updates the list of users and refreshes the RecyclerView.
     *
     * @param users the new list of users
     */
    public void setUsers(List<User> users) {
        this.userList = users;
        notifyDataSetChanged();
    }

    /**
     * Creates a new UserViewHolder for a user item.
     *
     * @param parent the parent ViewGroup
     * @param viewType the view type of the new View
     * @return a new UserViewHolder
     */
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    /**
     * Binds the user data to the ViewHolder.
     * Also handles the approval/revocation of admin permission for users.
     *
     * @param holder the ViewHolder to bind data to
     * @param position the position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvDisplayName.setText(user.getDisplayName());
        holder.tvEmail.setText(user.getEmail());

        boolean isAdmin = user.isPermission();
        holder.btnApproveRevoke.setText(isAdmin ? "Revoke" : "Approve");

        holder.btnApproveRevoke.setOnClickListener(v -> {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(user.getUid());

            ref.child("permission").setValue(!isAdmin);
        });
    }

    /**
     * Returns the number of users in the list.
     *
     * @return the number of users
     */
    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    /**
     * ViewHolder class for user items.
     * Holds references to the UI elements in each item.
     */
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvDisplayName, tvEmail;
        Button btnApproveRevoke;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnApproveRevoke = itemView.findViewById(R.id.btnApproveRevoke);
        }
    }
}