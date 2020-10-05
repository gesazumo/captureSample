package com.example.capturetest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class MyService extends Service {

    WindowManager wm;
    View mView;
    ImageButton bt;
    WindowManager.LayoutParams params;

    @Override
    public IBinder onBind(Intent intent) { return null; }


    private void takeScreenshot() {
        Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);

        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/" + now + ".jpg";

            // create bitmap screen capture

            View v1 = (View)mView.findViewById(R.id.captures);
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

//            openScreenshot(imageFile);
        } catch (Throwable e) {
            // Several error may come out with file handling or DOM
            e.printStackTrace();
        }
    }

//    private void openScreenshot(File imageFile) {
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_VIEW);
//        Uri uri = Uri.fromFile(imageFile);
//        intent.setDataAndType(uri, "image/*");
//        startActivity(intent);
//    }

    @Override
    public void onCreate() {
        super.onCreate();
        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.LEFT | Gravity.TOP;
        mView = inflate.inflate(R.layout.view_in_service, null);
        bt =  (ImageButton) mView.findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bt.setImageResource(R.mipmap.ic_launcher_round);
                takeScreenshot();
            }
        });

        bt.setOnTouchListener(mViewTouchListener);

        wm.addView(mView, params);
    }

    float START_X = 0;
    float START_Y = 0;
    float PREV_X = 0;
    float PREV_Y = 0;

    private View.OnTouchListener mViewTouchListener = new View.OnTouchListener() {
        @Override public boolean onTouch(View v, MotionEvent event) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:                //사용자 터치 다운이면
                    START_X = event.getRawX();                    //터치 시작 점
                    START_Y = event.getRawY();                    //터치 시작 점
                    PREV_X = params.x;                            //뷰의 시작 점
                    PREV_Y = params.y;                            //뷰의 시작 점
                    return false;
                case MotionEvent.ACTION_MOVE:

                    int x = (int)(event.getRawX() - START_X);    //이동한 거리
                    int y = (int)(event.getRawY() - START_Y);    //이동한 거리

                    //터치해서 이동한 만큼 이동 시킨다
                    params.x = (int) (PREV_X + x);
                    params.y = (int) (PREV_Y + y);

                    wm.updateViewLayout(mView, params);    //뷰 업데이트
                    break;
            }
            return false;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(wm != null) {
            if(mView != null) {
                wm.removeView(mView);
                mView = null;
            }
            wm = null;
        }
    }
}