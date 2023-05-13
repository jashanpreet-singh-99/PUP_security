package com.pup.pupsecurity.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.pup.pupsecurity.R;
import com.pup.pupsecurity.activities.SOSMap;
import com.pup.pupsecurity.interfaces.OnGuardSelected;
import com.pup.pupsecurity.model.InfoDataModel;

import java.util.ArrayList;

import static com.pup.pupsecurity.utils.Config.TAG_ADMIN;

public class GuardDeployedAdapter extends RecyclerView.Adapter<GuardDeployedAdapter.viewHolder> {

    private ArrayList<InfoDataModel> data;
    private Context context;
    private OnGuardSelected onGuardSelected;

    public GuardDeployedAdapter(ArrayList<InfoDataModel> data, Context context, OnGuardSelected onGuardSelected) {
        this.data = data;
        this.context = context;
        this.onGuardSelected = onGuardSelected;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_guard, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolder holder, final int position) {
        holder.guardName.setText(data.get(position).getContact());
        holder.guardContact.setText(data.get(position).getTime());
        holder.guardCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + data.get(position).getContact()));
                context.startActivity(intent);
            }
        });
        holder.parentLay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                onGuardSelected.removeGuardSelected(position, data.get(position).getContact());
                return true;
            }
        });
        holder.guardLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SOSMap.class);
                Bundle bundle = new Bundle();
                bundle.putString("lat", data.get(position).getLatitude());
                bundle.putString("long", data.get(position).getLongitude());
                bundle.putString("contact", data.get(position).getContact());
                bundle.putString("name", data.get(position).getContact());
                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        if (data.get(position).getImage().equals("none")) {
            holder.guardImage.setImageResource(R.drawable.asset_user);
            Log.e(TAG_ADMIN, "image : none");
        } else {
            Glide.with(context).asBitmap().load(data.get(position).getImage()).into(new BitmapImageViewTarget(holder.guardImage){
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

        ImageView guardImage;
        ImageView guardCall;
        ImageView guardLocation;
        TextView guardName;
        TextView guardContact;
        ProgressBar loadImage;
        LinearLayout parentLay;

        viewHolder(@NonNull View itemView) {
            super(itemView);
            guardImage = itemView.findViewById(R.id.guard_image);
            guardCall = itemView.findViewById(R.id.call_guard_btn);
            guardLocation = itemView.findViewById(R.id.guard_location);
            guardName = itemView.findViewById(R.id.guard_name);
            guardContact = itemView.findViewById(R.id.guard_contact);
            loadImage = itemView.findViewById(R.id.load_image);
            parentLay = itemView.findViewById(R.id.parent_guard_lay);
        }
    }
}
