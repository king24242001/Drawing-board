package com.example.dukepen;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.dukepen.ColorPickerDialog.OnColorChangedListener;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends Activity implements OnClickListener {

    private PenDrawView penDrawView;
    private LinearLayout layout;
    private Button clearBt, penBt, eraserBt, colorBt, nextBt, lastBt, saveBt;
    private ColorPickerDialog colorPickerDialog;
    private SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        setListener();
        setColor();
        setSize();
    }

    private void findViews(){
        penDrawView = (PenDrawView) findViewById(R.id.pendrawview);
        clearBt = (Button) findViewById(R.id.clear_bt);
        penBt = (Button) findViewById(R.id.pen_bt);
        eraserBt = (Button) findViewById(R.id.eraser_bt);
        colorBt = (Button) findViewById(R.id.color_bt);
        nextBt = (Button) findViewById(R.id.next_bt);
        lastBt = (Button) findViewById(R.id.last_bt);
        saveBt = (Button) findViewById(R.id.save_bt);
        layout = (LinearLayout) findViewById(R.id.layout);
        seekBar = (SeekBar)findViewById(R.id.seekbar);
    }

    private void setListener(){
        clearBt.setOnClickListener(this);
        penBt.setOnClickListener(this);
        eraserBt.setOnClickListener(this);
        colorBt.setOnClickListener(this);
        nextBt.setOnClickListener(this);
        lastBt.setOnClickListener(this);
        saveBt.setOnClickListener(this);
    }

    private void setColor(){
        colorPickerDialog = new ColorPickerDialog(this, "點圓心選顏色",
                new OnColorChangedListener() {
                    @Override
                    public void colorChanged(int color) {
                        penDrawView.setPenColor(color);
                    }
                });
    }

    private void setSize(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                penDrawView.setPenWidth(progress);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_bt:
                penDrawView.clear();
                Toast.makeText(MainActivity.this, "清除畫面", Toast.LENGTH_SHORT).show();
                break;
            case R.id.pen_bt:
                penDrawView.changePen();
                penDrawView.setVisibility(View.VISIBLE);
                layout.setVisibility(View.VISIBLE);
                eraserBt.setVisibility(View.VISIBLE);
                colorBt.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "畫筆模式", Toast.LENGTH_SHORT).show();
                break;
            case R.id.eraser_bt:
                penDrawView.changeEraser();
                Toast.makeText(MainActivity.this, "橡皮擦模式", Toast.LENGTH_SHORT).show();
                break;
            case R.id.color_bt:
                colorPickerDialog.setmInitialColor(penDrawView.getPenColor());
                colorPickerDialog.show();
                break;
            case R.id.last_bt:
                penDrawView.lastStep();
                break;
            case R.id.next_bt:
                penDrawView.nextStep();
                break;
            case R.id.save_bt:
                try {
                    File file = new File(Environment.getExternalStorageDirectory(),
                            System.currentTimeMillis() + ".png");
                    FileOutputStream stream = new FileOutputStream(file);
                    penDrawView.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);

                    Toast.makeText(MainActivity.this, "保存成功", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "保存失敗", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                break;
        }
    }
}
