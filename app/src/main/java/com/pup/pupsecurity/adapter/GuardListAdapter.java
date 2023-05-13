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
import com.pup.pupsecurity.model.InfoDataModel;
import com.pup.pupsecurity.utils.Config;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static com.pup.pupsecurity.utils.Config.TAG_ADMIN;

public class GuardListAdapter extends RecyclerView.Adapter<GuardListAdapter.viewHolder> {

    private ArrayList<InfoDataModel> data;
    private Context context;

    public GuardListAdapter(ArrayList<InfoDataModel> data, Context context) {
        this.data = data;
        this.context = context;
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
        Log.d(TAG_ADMIN, data.get(position).getTime());
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-MM-yyyy", Locale.getDefault());
        try {
            Date date = sdf.parse(Objects.requireNonNull(data.get(position).getTime()));
            assert date != null;
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, -10);
            if (calendar.getTime().before(date)) {
                holder.guardStat.setVisibility(View.VISIBLE);
            } else {
                holder.guardStat.setVisibility(View.GONE);
            }
        } catch (ParseException e) {
            Log.e(Config.TAG_SPLASH, "ERROR GUARD " + e);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class viewHolder extends RecyclerView.ViewHolder {

        ImageView guardImage;
        ImageView guardStat;
        ImageView guardCall;
        ImageView guardLocation;
        TextView guardName;
        TextView guardContact;
        ProgressBar loadImage;

        viewHolder(@NonNull View itemView) {
            super(itemView);
            guardImage = itemView.findViewById(R.id.guard_image);
            guardStat = itemView.findViewById(R.id.guard_stat);
            guardCall = itemView.findViewById(R.id.call_guard_btn);
            guardLocation = itemView.findViewById(R.id.guard_location);
            guardName = itemView.findViewById(R.id.guard_name);
            guardContact = itemView.findViewById(R.id.guard_contact);
            loadImage = itemView.findViewById(R.id.load_image);
        }
    }
}
