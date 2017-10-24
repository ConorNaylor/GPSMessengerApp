package com.example.conornaylor.gps_chat_app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    Button write, seeMap, seeMessages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        write = (Button) findViewById(R.id.write);
        seeMap = (Button) findViewById(R.id.seeMap);
        seeMessages = (Button) findViewById(R.id.seeMessages);

        write.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent messageintent = new Intent(HomeActivity.this, WriteMessageActivity.class);
                startActivity(messageintent);
            }
        });

        seeMap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(HomeActivity.this, SeeMapActivity.class);
                startActivity(mapIntent);
            }
        });

        seeMessages.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent historyIntent = new Intent(HomeActivity.this, MessageHistoryActivity.class);
                startActivity(historyIntent);
            }
        });
    }


}
