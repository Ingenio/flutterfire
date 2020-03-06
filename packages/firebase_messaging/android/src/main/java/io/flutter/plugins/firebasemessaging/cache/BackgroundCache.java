package io.flutter.plugins.firebasemessaging.cache;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackgroundCache {
    private static final String NOTIFICATION_ID = "sessionId";
    private static final String FILE_NAME = "temporary_notifications_data_storage";

    private final ObjectSerializer<ArrayList<HashMap<String, String>>> serializer  =
            new ObjectSerializer<>();

    private final ArrayList<HashMap<String, String>> notificationsData = new ArrayList<>();

    private final WeakReference<Context> weakContext;

    public BackgroundCache(Context context) {
        weakContext = new WeakReference<>(context);
        try {
            ArrayList<HashMap<String, String>> notificationsData = serializer.read(context, FILE_NAME);
            if (notificationsData != null && !notificationsData.isEmpty()) {
                this.notificationsData.addAll(notificationsData);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            Log.d("NOTIFICATION SERIALIZER", "error: " + e);
        }
    }

    public void put(Map<String, String> data) {
        //TODO this logic hardcoded on session id key from Advisor push data
        final String sessionId = data.get(NOTIFICATION_ID);
        if (sessionId != null) {
            for (HashMap<String, String> notificationData : notificationsData) {
                final String id = notificationData.get(NOTIFICATION_ID);
                if (sessionId.equals(id)) {
                    notificationsData.remove(notificationData);
                    break;
                }
            }
            notificationsData.add(new HashMap<>(data));
            save();
        }
    }

    public void clear() {
        notificationsData.clear();
        save();
    }

    private void save() {
        try {
            Context context = weakContext.get();
            if (context != null) {
                serializer.write(context, FILE_NAME, notificationsData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<? extends Map<String, String>> getAll() {
        return notificationsData;
    }
}
