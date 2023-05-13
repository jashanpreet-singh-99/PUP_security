package com.pup.pupsecurity.utils;

public class Config {
    /**
     * Foreground Service Notification id variables
     */
    public final static int PICK_IMAGE_REQUEST = 12;
    public static final String NOTIFICATION_CHANNEL_ID = "notification";
    public static final int PERMISSION_REQUEST_CODE = 9090;

    /**
     * Preference Manager keys
     */
    public static final String KEY_IS_SERVICE_RUNNING = "IS_SERVICE_RUNNING";
    public static final String KEY_ROLE = "ROLE_OF_USER";
    public static final String KEY_TOKEN = "USER_TOKEN_FMC";
    public static final String KEY_GUARD_ON_DUTY = "GUARD_ON_DUTY";
    public static final String KEY_ADMIN_SUBSCRIBE = "ADMIN_SUBSCRIBE";
    public static final String KEY_REGISTERED = "is_user_registered_with_info";
    public static final String KEY_SOS_ID = "SOS_ID_UNIQUE";

    /**
     * Log TAGS
     */
    public static final String TAG_SPLASH        = "splash_activity";
    public static final String TAG_REGISTER      = "register_activity";
    public static final String TAG_ADMIN         = "admin_activity";
    public static final String TAG_GUARD         = "guard_activity";
    public static final String TAG_USER          = "user_activity";
    public static final String TAG_BROADCAST     = "broadcast_location";
    public static final String TAG_SERVICE       = "service_location";
    public static final String TAG_SERVICE_USER  = "service_location_user";

    /**
     * FCM Topic Var
     */
    public static final String FCM_TOPIC = "Admin";
    public static final String FCM_KEY = "key=AAAAxwV_bw8:APA91bFBN2laGT6qbuSEh0a4rdxyGWWqNygE2P5DETotV50dKPgRD-Nobi5BPkDBTWmQDbewPqwiXllR8hJnKdSloXOcx07dIWWJbksML8vnlXMPF0TrJ4JP6mmrx5NRmhz1VmxlZz9p";
}
