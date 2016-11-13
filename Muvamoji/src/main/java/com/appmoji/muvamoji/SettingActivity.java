package com.appmoji.muvamoji;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SettingActivity extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        // init toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setContentInsetsAbsolute(0, 0);

        Button close = (Button) findViewById(R.id.btn_setting_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RelativeLayout lateLayout = (RelativeLayout) findViewById(R.id.layout_rate);
        lateLayout.setOnClickListener(this);

        RelativeLayout layoutContact = (RelativeLayout) findViewById(R.id.layout_contact);
        layoutContact.setOnClickListener(this);

        RelativeLayout layoutFollow = (RelativeLayout) findViewById(R.id.layout_follow);
        layoutFollow.setOnClickListener(this);

        RelativeLayout layoutLike = (RelativeLayout) findViewById(R.id.layout_like);
        layoutLike.setOnClickListener(this);

        RelativeLayout layoutTweet = (RelativeLayout) findViewById(R.id.layout_tweet);
        layoutTweet.setOnClickListener(this);

        RelativeLayout layoutSnap = (RelativeLayout) findViewById(R.id.layout_snap);
        layoutSnap.setOnClickListener(this);

        RelativeLayout layoutTerms = (RelativeLayout) findViewById(R.id.layout_terms);
        layoutTerms.setOnClickListener(this);

        RelativeLayout layoutPrivacy = (RelativeLayout) findViewById(R.id.layout_privacy);
        layoutPrivacy.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent browserIntent;
        switch (v.getId()) {
            case R.id.layout_rate:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://itunes.apple.com/us/app/muvamoji/id1087839782?ls=1&mt=8"));
                startActivity(browserIntent);
                break;
            case R.id.layout_contact:
                sendEmail();
                break;
            case R.id.layout_follow:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://instagram.com/muvamoji"));
                startActivity(browserIntent);
                break;
            case R.id.layout_like:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://facebook.com/muvamoji"));
                startActivity(browserIntent);
                break;
            case R.id.layout_tweet:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://twitter.com/muvamoji"));
                startActivity(browserIntent);
                break;
            case R.id.layout_snap:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://snapchat.com/add/muvamoji"));
                startActivity(browserIntent);
                break;
            case R.id.layout_terms:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.muvamoji.com/terms.html"));
                startActivity(browserIntent);
                break;
            case R.id.layout_privacy:
                browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.muvamoji.com/privacy.html"));
                startActivity(browserIntent);
                break;
        }
    }

    private void sendEmail() {
        Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
        mailIntent.setType("text/plain");
        mailIntent.setData(Uri.parse("mailto:" + "info@muvamoji.com"));
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
        mailIntent.putExtra(Intent.EXTRA_TEXT, "Feature request or bug report?");
        try {
            startActivity(Intent.createChooser(mailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }
}

