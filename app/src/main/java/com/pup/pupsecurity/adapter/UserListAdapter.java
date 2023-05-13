package com.pup.pupsecurity.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.pup.pupsecurity.R;
import com.pup.pupsecurity.model.UserData;

import java.util.ArrayList;

import static com.pup.pupsecurity.utils.Config.TAG_ADMIN;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.viewHolder> {

    private ArrayList<UserData> data;
    private Context context;

    public UserListAdapter(ArrayList<UserData> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_user, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolder holder, int position) {
        holder.userName.setText(data.get(position).getName());
        holder.userContact.setText(data.get(position).getContact());
        if (data.get(position).getImage().equals("none")) {
            holder.userImage.setImageResource(R.drawable.asset_user);
        } else {
            Glide.with(context).asBitmap().load(data.get(position).getImage()).into(new BitmapImageViewTarget(holder.userImage){
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    super.onResourceReady(resource, transition);
                    holder.loadImage.setVisibility(View.GONE);
                }
            });
            Log.e(TAG_ADMIN, "image : posted");
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class viewHolder extends RecyclerView.ViewHolder {

        ImageView userImage;
        TextView userName;
        TextView userContact;

        ProgressBar loadImage;

        viewHolder(@NonNull View itemView) {
            super(itemView);
            loadImage = itemView.findViewById(R.id.load_image);
            userImage = itemView.findViewById(R.id.user_image);
            userName = itemView.findViewById(R.id.user_name);
            userContact = itemView.findViewById(R.id.user_contact);
        }
    }
}
