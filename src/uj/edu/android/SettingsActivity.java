package uj.edu.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by shybovycha on 25.01.15.
 */
public class SettingsActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        AlarmRecipients recipients = AlarmRecipients.load(this);

        EditText smsRecipientsInput = (EditText) findViewById(R.id.emergency_sms_recipients);
        EditText callRecipientInput = (EditText) findViewById(R.id.emergency_call_recipients);

        for (String phone : recipients.smsRecipients) {
            smsRecipientsInput.append(phone + "\n");
        }

        callRecipientInput.setText(recipients.callRecipient);

        Button saveBtn = (Button) findViewById(R.id.save_settings);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingsActivity.this.saveSettings();
            }
        });
    }

    protected void saveSettings() {
        AlarmRecipients recipients = new AlarmRecipients();

        EditText smsRecipientsInput = (EditText) findViewById(R.id.emergency_sms_recipients);
        EditText callRecipientInput = (EditText) findViewById(R.id.emergency_call_recipients);

        String[] smsRecipients = smsRecipientsInput.getText().toString().split("\\n");
        String callRecipient = callRecipientInput.getText().toString();

        final String phoneRe = "^\\+?[\\d]{3,}$";

        for (String phone : smsRecipients) {
            if (!phone.matches(phoneRe)) {
                continue;
            }

            recipients.smsRecipients.add(phone);
        }

        if (callRecipient.matches(phoneRe)) {
            recipients.callRecipient = callRecipient;
        }

        recipients.save(this);

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }
}