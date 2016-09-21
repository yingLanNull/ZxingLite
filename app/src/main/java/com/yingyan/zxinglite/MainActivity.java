package com.yingyan.zxinglite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.zxing.client.android.CaptureActivity;
import com.google.zxing.client.android.Intents;

public class MainActivity extends AppCompatActivity {

    private android.widget.TextView textView;
    private android.widget.Button button;
    private int REQUEST_CODE = 20001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.button = (Button) findViewById(R.id.button);
        this.textView = (TextView) findViewById(R.id.textView);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                intent.setAction(Intents.Scan.ACTION);
                intent.putExtra(Intents.Scan.CHARACTER_SET, "utf-8");
                intent.putExtra(Intents.Scan.WIDTH, dip2px(220));
                intent.putExtra(Intents.Scan.HEIGHT, dip2px(220));
                intent.setClass(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (null != data && requestCode == REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    if (data.getStringExtra(Intents.Scan.RESULT) != null) {
                        String text = data.getStringExtra(Intents.Scan.RESULT);
                        textView.setText(text);
                    }
            }
        }
    }

    /**
     * dp转px
     **/
    public int dip2px(int dipValue) {
        float reSize = this.getResources().getDisplayMetrics().density;
        return (int) ((dipValue * reSize) + 0.5);
    }
}
