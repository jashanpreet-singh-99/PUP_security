package com.pup.pupsecurity.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.pup.pupsecurity.activities.AdminDetailedSOS;
import com.pup.pupsecurity.activities.SOSMap;
import com.pup.pupsecurity.model.InfoDataModel;
import com.pup.pupsecurity.model.SOSDataModel;

import java.util.ArrayList;

public class SOSListAdapter extends RecyclerView.Adapter<SOSListAdapter.viewHolder> {

    private ArrayList<SOSDataModel> sosList;
    private Context context;
    private boolean historyMode = false;

    public SOSListAdapter(ArrayList<SOSDataModel> sosList, Context context, boolean historyMode) {
        this.sosList = sosList;
        this.context = context;
        this.historyMode = historyMode;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sos, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final viewHolder holder, final int position) {
        holder.sosContact.setText(sosList.get(position).getContact());
        holder.sosName.setText(sosList.get(position).getTime());
        holder.sosCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + sosList.get(position).getContact()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        holder.sosParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, AdminDetailedSOS.class);
                intent.putExtra("data", sosList.get(position));
                intent.putExtra("history_mode", historyMode);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        holder.sosLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SOSMap.class);
                Bundle bundle = new Bundle();
                bundle.putString("lat", sosList.get(position).getLatitude());
                bundle.putString("long", sosList.get(position).getLongitude());
                bundle.putString("contact", sosList.get(position).getContact());
                bundle.putString("name", sosList.get(position).getContact());
                intent.putExtras(bundle);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        if (sosList.get(position).getImage().equals("none")) {
            holder.sosImage.setImageResource(R.drawable.asset_user);
        } else {
            Glide.with(context).asBitmap().load(sosList.get(position).getImage()).into(new BitmapImageViewTarget(holder.sosImage){
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    super.onResourceReady(resource, transition);
                    holder.loadImage.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return sosList.size();
    }

    class viewHolder extends RecyclerView.ViewHolder {

        TextView sosContact;
        TextView sosName;
        ImageButton sosLocation;
        ImageButton sosCall;
        ImageView sosImage;
        LinearLayout sosParent;

        ProgressBar loadImage;

         viewHolder(@NonNull View itemView) {
            super(itemView);
            sosContact = itemView.findViewById(R.id.sos_contact);
            sosParent = itemView.findViewById(R.id.sos_parent);
            sosName = itemView.findViewById(R.id.sos_name);
            sosImage = itemView.findViewById(R.id.sos_image);
            sosLocation = itemView.findViewById(R.id.location_sos_btn);
            sosCall = itemView.findViewById(R.id.call_sos_btn);
            loadImage = itemView.findViewById(R.id.load_image);
        }


    }

}
