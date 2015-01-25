package uj.edu.android;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.telephony.SmsManager;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shybovycha on 25.01.15.
 */
public class AlarmRecipients implements Serializable {
    public List<String> smsRecipients;
    public String callRecipient;

    public AlarmRecipients() {
        this.smsRecipients = new ArrayList<String>();
    }

    public static AlarmRecipients load(Context context) {
        AlarmRecipients result = new AlarmRecipients();

        try {
            File file = new File(context.getExternalFilesDir("/alarm_button") + "/alarm_recipients.dat");
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream is = new ObjectInputStream(fis);

            result = (AlarmRecipients) is.readObject();

            is.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void save(Context context) {
        try {
            File dir = new File(Environment.getExternalStorageDirectory() + "/alarm_button");

            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File(context.getExternalFilesDir("/alarm_button") + "/alarm_recipients.dat");

            if (!file.exists()) {
                file.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream os = new ObjectOutputStream(fos);

            os.writeObject(this);

            os.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void alarmSequence(Context context) {
        sendSMS(context);
        phoneCall(context);
    }

    protected void sendSMS(Context context)
    {
        if (smsRecipients.size() < 1) {
            return;
        }

        try {
            Location location = getLocation(context);

            double lat = location.getLatitude();
            double lng = location.getLongitude();

            String message = String.format("Help me! I am at (%.8f, %.8f)", lat, lng);
            SmsManager sms = SmsManager.getDefault();

            for (String phoneNumber : smsRecipients) {
                sms.sendTextMessage(phoneNumber, null, message, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void phoneCall(Context context) {
        if (callRecipient == null) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(String.format("tel:%s", this.callRecipient)));
        context.startActivity(intent);
    }

    protected Location getLocation(Context context) {
        String provider = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if (!provider.contains("gps")) {
            context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location locationGPS = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;

        if (locationGPS != null) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (locationNet != null) {
            NetLocationTime = locationNet.getTime();
        }

        if ((GPSLocationTime - NetLocationTime) > 0) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }
}
