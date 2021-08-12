package com.dandystep.vlnotifier;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;


public class AboutActivity extends AppCompatActivity {

    public void AboutLinks(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        switch (v.getId()) {
            case R.id.about_website:
                intent.setData(Uri.parse("http://dandystep.com"));
                startActivity(intent);
                break;
            case R.id.about_twitter:
                intent.setData(Uri.parse("https://twitter.com/dandystep"));
                startActivity(intent);
                break;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }

        TextView website = (TextView) findViewById(R.id.about_website);
        TextView twitter = (TextView) findViewById(R.id.about_twitter);
        website.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
        twitter.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
    }
}
