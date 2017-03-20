package com.udacity.stockhawk.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by kheireddine on 20/03/17.
 */

public class Utils {
    public static final String TAG = "stock_log";

    /**
     * Show a short toast Message
     * @param msg : Message to display
     */
    public static void showShortToastMessage(Context context, String msg){
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    /**
     * Show a long toast Message
     * @param msg : Message to display
     */
    public static void showLongToastMessage(Context context, String msg){
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        toast.show();
    }

    /**
     * Check the network connection
     * @param context : Activity context
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
