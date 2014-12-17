package edu.wisc.drivesense.views;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import edu.wisc.drivesense.R;
import edu.wisc.drivesense.activities.MainActivity;
import edu.wisc.drivesense.businessLogic.BackgroundRecordingService;

/**
 * Created by Damouse on 12/16/2014.
 *
 * Creating and managing the taskbar notifications
 */
public class TaskbarNotifications {
    private Context context;

    public TaskbarNotifications(Context parent) {
        context = parent;
        initNotification();
    }

    private void initNotification() {
        Notification note = new Notification(R.drawable.ic_launcher_app_icon, "KnowMyDrive Started", System.currentTimeMillis());
        Intent i = new Intent(context, BackgroundRecordingService.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);

        note.setLatestEventInfo(context, "KnowMyDrive", "Waiting for a trip to start", pi);
        note.flags |= Notification.FLAG_NO_CLEAR;

        Service service = (Service) context;
        service.startForeground(1337, note);
    }

    /**
     * Update the top bar notification text as passed
     */
    public void updateServiceNotifcation(String status) {
        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification note = new NotificationCompat.Builder(context)
                .setContentTitle("KnowMyDrive")
                .setContentText(status)
                .setSmallIcon(R.drawable.ic_launcher_app_icon)
                .setContentIntent(resultPendingIntent)
                .build();

        NotificationManager noteManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (noteManager == null)
            return;

        noteManager.notify(1337, note);
    }
}
