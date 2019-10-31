package com.example.solbin.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.speech.tts.TextToSpeech;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    float[] x = new float[4];
    float[] y = new float[4];
    float moveX=0, moveY=0;
    boolean touch = false;

    private TextToSpeech ttsClient;

    @Override
    public void onInit(int i) {
        ttsClient.setSpeechRate((float) 1.0);
        ttsClient.speak("환영합니다. 어플 조작법을 듣고싶으시면, 두 손가락으로 화면을 위에서 아래로 슬라이드 해주세요.", TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN){
            x[0] = (int) event.getX();
            y[0] = (int) event.getY();
        }
        if (event.getActionMasked()== MotionEvent.ACTION_POINTER_DOWN){
            touch = true;
        }
        if(event.getActionMasked() == MotionEvent.ACTION_UP) {
            x[1] = (int) event.getX();
            y[1] = (int) event.getY();

            moveX = x[0] - x[1];
            moveY = y[0] - y[1];

            if(touch) { //멀티터치
                if ((Math.abs(moveX) < 300) && moveY > 0) { //위로 드래그
                    Intent intent = new Intent(MainActivity.this, inApp.class);
                    startActivity(intent);
                } else if ((Math.abs(moveX) < 300) && moveY < 0) { //아래로 드래그
                    ttsClient.speak("우리 어플의 조작은 모두 화면 슬라이드 방식으로 진행됩니다." +
                                    "두 손가락으로 화면을 상하좌우로 슬라이드하여" +
                                    "입력 방식을 변경하거나 사용법을 들을 수 있습니다." +
                                    "아래에서 위로 슬라이드하면 어플 내에서 점자를 입력할 수 있습니다." +
                                    "왼쪽에서 오른쪽으로 슬라이드하면 버튼을 부착한 입력기를 사용할 수 있습니다." +
                                    "오른쪽에서 왼쪽으로 슬라이드하면 카메라를 부착한 리더기를 사용할 수 있습니다." +
                                    "사용법을 다시 듣고싶으시면 위에서 아래로 슬라이드해주세요.",TextToSpeech.QUEUE_FLUSH,null);
                } else if (moveX < 0 && (Math.abs(moveY) < 300)) { //오른쪽으로 드래그
                    Intent intent = new Intent(MainActivity.this, Input.class);
                    startActivity(intent);
                } else if (moveX > 0 && (Math.abs(moveY) < 300)) { //왼쪽으로 드래그
                    Intent intent = new Intent(MainActivity.this, Reader.class);
                    startActivity(intent);
                }
            }
            touch = false;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ttsClient= new TextToSpeech(getApplicationContext(),this);
    }
}
