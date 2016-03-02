package org.zhgeaits.demo;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.zhgeaits.risv.R;
import org.zhgeaits.risv.VideoImageSeekbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private VideoImageSeekbar seekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekbar = (VideoImageSeekbar) findViewById(R.id.image_bar);

        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < 20; i ++) {
            ids.add(R.mipmap.test);
        }
        seekbar.setProgress(0);
        seekbar.setImageListIds(ids);
        seekbar.setLeftChange(true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                seekbar.addLabelRangeView(0, 20d, 30);
            }
        }, 2000);
    }

}
