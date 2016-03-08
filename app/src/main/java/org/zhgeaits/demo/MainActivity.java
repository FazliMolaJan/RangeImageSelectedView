package org.zhgeaits.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.zhgeaits.risv.R;
import org.zhgeaits.risv.VideoImageSeekbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private VideoImageSeekbar seekbar;
    private Button setImage;
    private Button addWindow;
    private Button addFixedWindow;
    private Button addrange;
    private Button addpoint;
    private Button removeWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekbar = (VideoImageSeekbar) findViewById(R.id.image_bar);
        setImage = (Button) findViewById(R.id.set_image);
        addWindow = (Button) findViewById(R.id.window);
        addFixedWindow = (Button) findViewById(R.id.fixed_window);
        addrange = (Button) findViewById(R.id.add_range_bar);
        addpoint = (Button) findViewById(R.id.add_range_point);
        removeWindow = (Button) findViewById(R.id.remove_window);

        setImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Integer> ids = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    ids.add(R.mipmap.test);
                }
                seekbar.setProgress(0);
                seekbar.setImageListIds(ids);
            }
        });

        addWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekbar.addScaledRangeView(0, seekbar.getCurrentProgress(), 30);
            }
        });

        removeWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekbar.removeRangeView(0);
            }
        });

        addFixedWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekbar.addFixedRangeView(0, seekbar.getCurrentProgress(), 30, 30);
            }
        });

        addrange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(seekbar.isRangeViewVisible()) {
                    seekbar.addMarkRange(0);
                }
            }
        });

        addpoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(seekbar.isRangeViewVisible()) {
                    seekbar.addMarkPoint(0);
                }
            }
        });
    }

}
