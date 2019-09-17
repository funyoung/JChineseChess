package com.hzy.chinese.jchess.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.AppUtils;
import com.hzy.chinese.jchess.R;

/**
 * Created by tangbull on 2018/3/27.
 */

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int[] CLICKABLE_ITEM_IDS = {
            R.id.version_info_item,
            R.id.source_code_item,
            R.id.about_me_item
    };

    private TextView mVersionName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        mVersionName = findViewById(R.id.version_name);
        setOnClickListener();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setupVersionInfo();
    }

    private void setupVersionInfo() {
        String versionName = AppUtils.getAppVersionName();
        mVersionName.setText(versionName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setOnClickListener() {
        for (int id : CLICKABLE_ITEM_IDS){
            findViewById(id).setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.version_info_item:
                WebViewActivity.startUrl(this, getString(R.string.github_release_page));
                break;
            case R.id.source_code_item:
                WebViewActivity.startUrl(this, getString(R.string.github_project_page));
                break;
            case R.id.about_me_item:
                WebViewActivity.startUrl(this, getString(R.string.github_user_page));
                break;
        }
    }
}
