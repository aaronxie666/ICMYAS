package icn.icmyas.Listeners;

/**
 * Author: Tom Linford
 * Date: 12/01/2018
 * Package: icn.icmyas.Listeners
 * Project Name: ICMYAS
 */

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

public class PushReceiver extends ParsePushBroadcastReceiver {
    @Override
    protected Notification getNotification(Context context, Intent intent) {
        // deactivate standard notification
        return null;
    }
    @Override
    protected void onPushReceive(Context context, Intent intent) {
        System.out.println("Received");
        super.onPushReceive(context, intent);
        Log.e("PushReceiver", "Received!");
        if (intent == null)
            return;

        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            Log.e("PushReceiver", "Push received: " + json);
        } catch (JSONException e) {
            Log.e("PushReceiver", "Push message JSON exception: " + e.getMessage());
        }
    }

    @Override
    public void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
        System.out.println("Received");
    }
}
