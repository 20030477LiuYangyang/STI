package Utils;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.testbeaconapp.R;

import java.util.HashMap;
import java.util.Map;

public class NotificationUtil {

    public static void createNotificationChannel(Context context, int channelNo, String channelCode, String channelName, String channelDescription, int importance) {

        Map<String, NotificationChannel> channels = new HashMap<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channels.put("channel" + channelNo, new NotificationChannel(channelCode, channelName, importance));
            channels.get("channel" + channelNo).setDescription(channelDescription);
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channels.get("channel" + channelNo));
        }
    }

    public static void sendNotification(Context context, String channelCode, String contentTitle, String contentText, int icon, int priority) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelCode);
        builder.setContentTitle(contentTitle);
        builder.setContentText(contentText);
        builder.setSmallIcon(icon);
        builder.setPriority(priority);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        managerCompat.notify(1, builder.build());
    }
}
