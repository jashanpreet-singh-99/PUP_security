package com.pup.pupsecurity.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.pup.pupsecurity.service.GuardLocationService;
import com.pup.pupsecurity.utils.Config;
import com.pup.pupsecurity.utils.PreferenceManager;

import java.util.Objects;

import static com.pup.pupsecurity.utils.Config.KEY_GUARD_ON_DUTY;
import static com.pup.pupsecurity.utils.Config.TAG_BROADCAST;

public class ServiceOnBootStartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(Objects.equals(intent.getAction(), Intent.ACTION_BOOT_COMPLETED)){
            String role = PreferenceManager.getString(context, Config.KEY_ROLE);
            Log.d(TAG_BROADCAST, "Service is running the check");
            if (role.equals("guard") && PreferenceManager.getBoolean(context, KEY_GUARD_ON_DUTY)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(new Intent(context, GuardLocationService.class));
                } else {
                    context.startService(new Intent(context, GuardLocationService.class));
                }
            }
        }
    }
}
