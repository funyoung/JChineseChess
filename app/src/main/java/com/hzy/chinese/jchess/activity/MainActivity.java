package com.hzy.chinese.jchess.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import com.hzy.chinese.jchess.ChessPresenter;
import com.hzy.chinese.jchess.R;
import com.hzy.chinese.jchess.view.GameBoardView;

/**
 * @author yangfeng
 */
public class MainActivity extends AppCompatActivity {

    private ChessPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GameBoardView mGameBoard = findViewById(R.id.game_board);
        ProgressBar mGameProgress = findViewById(R.id.game_progress);
        SharedPreferences mPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        presenter = new ChessPresenter(this, mGameBoard, mGameProgress, mPreference);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_exit:
                finish();
                break;
            case R.id.main_menu_retract:
                presenter.retract();
                break;
            case R.id.main_menu_restart:
                presenter.restart();
                break;
            case R.id.main_menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.main_menu_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }
}
