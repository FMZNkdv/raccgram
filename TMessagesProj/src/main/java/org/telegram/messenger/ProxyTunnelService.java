package org.telegram.messenger;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import org.json.JSONObject;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.LaunchActivity;

public class ProxyTunnelService extends Service {

    public static final String CHANNEL_ID = "proxy_tunnel_service";
    public static final int NOTIFICATION_ID = 42901;

    public static void start(Context context, JSONObject outbound) {
        Intent intent = new Intent(context, ProxyTunnelService.class);
        intent.putExtra("outbound", outbound.toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, ProxyTunnelService.class));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createChannel();
        startForeground(NOTIFICATION_ID, buildNotification());

        String outboundJson = intent != null ? intent.getStringExtra("outbound") : null;
        if (outboundJson != null) {
            try {
                JSONObject outbound = new JSONObject(outboundJson);
                SingBoxProxyController.getInstance().start(outbound);
                FileLog.d("ProxyTunnelService: sing-box started ok");
            } catch (Exception e) {
                FileLog.e(e);
                stopSelf();
                return START_NOT_STICKY;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        SingBoxProxyController.getInstance().stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = manager.getNotificationChannel(CHANNEL_ID);
            if (channel == null) {
                channel = new NotificationChannel(CHANNEL_ID, "Proxy", NotificationManager.IMPORTANCE_MIN);
                channel.setShowBadge(false);
                manager.createNotificationChannel(channel);
            }
        }
    }

    private Notification buildNotification() {
        Intent openIntent = new Intent(this, LaunchActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, openIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        builder.setContentTitle(LocaleController.getString("Proxy", R.string.Proxy))
                .setSmallIcon(R.drawable.outline_shield_check)
                .setContentIntent(contentIntent)
                .setOngoing(true);
        return builder.build();
    }
}
