package icn.icmyas;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.Base64;
import android.util.Log;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Author:  Bradley Wilson
 * Date: 14/07/2017
 * Package: icn.icmyas
 * Project Name: ICMYAS
 */

public class Application extends android.app.Application {

    private Context context = this;
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(context)
                .applicationId("XBwlQmw7OO3a0c8xFlf3Bgz0g6SP7WPV7k7OYzOC")
                .clientKey("P0OdgSjf4H2G3vDbTw114FATkpP77SJOJCz6qO6V")
                .server("https://pg-app-7l3d13e14r88iajhse9xzz30ykekph.scalabl.cloud/1/")
        .build());
        ParseFacebookUtils.initialize(this);
        generateFBKeyHash(context);
    }

    public static void generateFBKeyHash(Context mContext) {
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(
                    "icn.icmyas",
                    PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("fb key hash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            Log.e("failed", e.getMessage());
        }
    }
}
