package com.cebu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MessageActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Messages");
        setContentView(R.layout.message);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.layout.message_activity, menu);
        return true;
    }
}
