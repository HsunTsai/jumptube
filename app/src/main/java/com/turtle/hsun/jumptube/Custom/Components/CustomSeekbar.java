package com.turtle.hsun.jumptube.Custom.Components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

@SuppressLint("AppCompatCustomView")
public class CustomSeekbar extends SeekBar implements SeekBar.OnSeekBarChangeListener{

    private CustomSeekbarListener listener;

    public CustomSeekbar(Context context) {
        super(context);
        super.setOnSeekBarChangeListener(this);
    }

    public CustomSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setOnSeekBarChangeListener(this);
    }

    public void setSeekbarListener(CustomSeekbarListener listener) {
        this.listener = listener;
    }

    public interface CustomSeekbarListener {
        public void onStopTrackingTouch(SeekBar seekBar);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        this.listener.onStopTrackingTouch(seekBar);
    }
}
