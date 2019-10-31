package com.example.solbin.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

import static app.akexorcist.bluetotohspp.library.BluetoothState.REQUEST_ENABLE_BT;
import android.speech.tts.TextToSpeech;

public class Input extends AppCompatActivity implements TextToSpeech.OnInitListener{
    private BluetoothSPP bt;
    float[] x = new float[4];
    float[] y = new float[4];
    float moveX=0, moveY=0;
    boolean touch = false;

    String braille = "";
    String [] array = new String[3];
    String hangle="";

    boolean num; //수표
    boolean dcon;
    boolean realdcon; //된소리
    boolean att; //붙임표

    boolean cho; //방금 전에 배열에 넣은게 초성이냐
    boolean abb;

    String alphabet = "";

    Hash hash = new Hash();
    combination com = new combination();

    private TextToSpeech ttsClient;

    public void onInit(int i) {
        ttsClient.speak("입력기로 점자입력",TextToSpeech.QUEUE_FLUSH,null);
        array[2] = " "; //이유는 모르겠는데 자꾸 처음에 이 전에 했던 종성이 들어감
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        TextView textView = (TextView) findViewById(R.id.text);

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
                if ((Math.abs(moveX) < 300) && moveY < 0) { //아래로 드래그
                    ttsClient.speak("버튼을 이용한 입력기로 점자를 입력할 수 있습니다." +
                            "점자가 튀어나온 부분의 버튼을 눌러서 점자를 입력합니다." +
                            "한번에 점자 한 칸씩 입력합니다. 오른쪽 버튼을 눌러 한 칸의 점자를 전송하며 " +
                            "왼쪽 버튼을 누르면 마지막으로 오른쪽 버튼을 누른 후부터 입력한 점자 한 칸이 삭제됩니다." +
                            "두 손가락을 이용하여 왼쪽에서 오른쪽으로 슬라이드 하면 변환된 문장을 읽어주고." +
                            "오른쪽에서 왼쪽으로 슬라이드하면 지금까지 변환된 문장 전체를 지워줍니다.",TextToSpeech.QUEUE_FLUSH,null);
                } else if ((Math.abs(moveY) < 300) && moveX > 0 ) { //왼쪽으로 드래그 (초기화)
                    if(textView.getText().length()!=0) {
                        ttsClient.speak("전체 삭제", TextToSpeech.QUEUE_FLUSH, null);
                        array[0] = null; array[1] = null; array[2] = " ";
                        cho = false;
                        braille = "";
                        hangle = "";
                        textView.setText(null);
                    }
                } else if ((Math.abs(moveY) < 300) && moveX < 0 ) { //오른쪽으로 드래그 (출력)
                    ttsClient.speak((String)textView.getText(),TextToSpeech.QUEUE_FLUSH,null);
                }
                touch = false; //이거 안하면 한 번 멀티터치 후엔 다음 싱글터치도 다 멀티터치로 인식
            }else { //싱글터치
                if (moveX < 0 && (Math.abs(moveY) < 250)) { //오른쪽으로 드래그 (마지막 글자 출력)
                    print(array);
                }
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        ttsClient= new TextToSpeech(getApplicationContext(),this);

        bt = new BluetoothSPP(this);

        if(!bt.isBluetoothAvailable()) {
            finish();
        }
        bt.disconnect();

        Intent intent = new Intent(this, DeviceList.class);
        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            public void onDataReceived(byte[] data, String message) {
                //Toast.makeText(Input.this, message, Toast.LENGTH_SHORT).show();
                TextView textView = (TextView) findViewById(R.id.text);
                TextView bb = (TextView) findViewById(R.id.bb);

                bb.setText(message);
                braille = message;
                change();
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(Input.this, "기기가 연결되었습니다", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() {
                Toast.makeText(Input.this, "기기와의 연결이 끊어졌습니다.", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() {
                Toast.makeText(Input.this, "기기와의 연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!bt.isBluetoothEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }else if(!bt.isServiceAvailable()){
            bt.setupService();
            bt.startService(BluetoothState.DEVICE_OTHER);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
            } else {
                // Do something if user doesn't choose any device (Pressed back)
            }
        }
    }

    public void change(){
        TextView textView = (TextView) findViewById(R.id.text);

        if(braille.equals("001111")){ //수표
            num = true;
        }else if(braille.equals("001001")){ //붙임표
            att = true;
        }

        //숫자끊기
        alphabet = hash.getbraille(braille, num, realdcon, att);
        if(num==true && braille.equals("001001")){ //001001은 붙임표
            num = false;
            att = false;
        }

        alphabet = hash.getbraille(braille, num, realdcon, att); //해쉬테이블에서 가져온거

        if(alphabet.equals(" ")) { //수표 입력 후에 띄어쓰기 했을 때 초기화, 띄어쓰기
            if(array[0]==null){
                hangle = hangle + " ";
                textView.setText(hangle);
            }else {
                print(array);
                hangle = hangle + " ";
                textView.setText(hangle);
            }
            num = false;
        }else if(alphabet.equals("초성ㅅ") && dcon==false) { //된소리표 or 걍 시옷
            if(array[0]==null){
                array[0] = "ㅅ";
                cho = true;
                dcon = true;
            }else {
                print(array);

                array[0] = "ㅅ";
                cho = true;
                dcon = true;
            }
        }else if (braille.equals("000001") || braille.equals("000100")||
                braille.equals("000101") || braille.equals("000110") || alphabet.equals("초성ㄷ")) { //초성ㄷ는 9랑 겹침
            if(dcon==true) { //된소리
                realdcon = true;

                alphabet = hash.getbraille(braille, num, realdcon, att);
                //ㅅ만 있으면 걍 쌍자음넣고, 다른거 있으면 배열 비우고 [0]에 쌍자음
                if(array[0].equals("ㅅ")){
                    if(alphabet.equals("ㄲ")||alphabet.equals("ㅆ")){
                        array[0] = alphabet;
                        cho = true;
                    }else{
                        array[0] = alphabet;
                        array[1] = "ㅏ";
                        cho = true;
                    }
                }else{
                    print(array);

                    if(alphabet.equals("ㄲ")||alphabet.equals("ㅆ")){
                        array[0] = alphabet;
                        cho = true;
                    }else{
                        array[0] = alphabet;
                        array[1] = "ㅏ";
                        cho = true;
                    }
                }

                dcon = false;
                realdcon = false;
            }else { //쌍자음 가능하지만 쌍자음 아닌 초성친구들
                if(array[0]!=null){
                    print(array);
                }

                if(alphabet.equals("초성ㄱ")||alphabet.equals("초성ㅅ")){
                    array[0] = alphabet.substring(2);
                    cho = true;
                }else{
                    array[0] = alphabet.substring(2);
                    array[1] = "ㅏ";
                    cho = true;
                }
            }
        }else if(braille.equals("001100")) { //ㅆ or ㅖ
            if(att==true){
                print(array);
                array[0]="ㅇ";
                array[1]="ㅖ";

                att=false;
            }else{
                if(array[1]==null || cho==true){
                    if(array[0]==null){
                        array[0] = "ㅇ";
                        array[1] = "ㅖ";
                    }else {
                        if(array[0].equals("ㄱ")||array[0].equals("ㅅ")||array[0].equals("ㄹ")||array[0].equals("ㅊ")){
                            if(array[1]==null){
                                array[1] = "ㅖ";
                                cho = false;
                            }else {
                                print(array);
                                array[0] = "ㅇ";
                                array[1] = "ㅖ";
                            }
                        }else {
                            if(cho==false){
                                print(array);
                                array[0] = "ㅇ";
                                array[1] = "ㅖ";
                            }else {
                                if(array[0].equals("ㄴ")){
                                    array[2] = alphabet.substring(2);
                                    cho = false;
                                }else {
                                    array[1] = "ㅖ";
                                    cho = false;
                                }
                            }
                        }
                    }
                }else {
                    if(array[2].equals(" ") && cho==true){
                        array[2] = alphabet.substring(2);
                        cho = false;
                    }else if(array[1]!=null) {
                        array[2] = alphabet.substring(2);
                        cho = false;
                    }else{
                        print(array);
                        array[0] = "ㅇ";
                        array[1] = "ㅖ";
                    }
                }
            }
        }else {
            if(alphabet.length()==1){
                if(alphabet.equals("가")){
                    if(array[0]!=null && dcon==false){
                        print(array);
                    }

                    if(dcon==true){
                        array[0] = "ㄲ";
                    }else{
                        array[0] = "ㄱ";
                    }

                    array[1] = "ㅏ";
                }else if(alphabet.equals("사")){
                    if(array[0]!=null && dcon==false){
                        print(array);
                    }
                    if(dcon==true){
                        array[0] = "ㅆ";
                    }else{
                        array[0] = "ㅅ";
                    }
                    array[1] = "ㅏ";
                }else{
                    if(array[0]!=null){
                        print(array);
                    }

                    hangle = hangle + alphabet.substring(0,1);
                    print(array);
                }
            }else if(alphabet.length()==2){ //중+종성 약어들
                if(array[0]==null){
                    array[0] = "ㅇ";
                    array[1] = alphabet.substring(0,1);
                    array[2] = alphabet.substring(1);
                }else {
                    if((array[0].equals("ㅅ")||array[0].equals("ㅆ")||array[0].equals("ㅈ")||array[0].equals("ㅉ")||array[0].equals("ㅊ")) && alphabet.equals("ㅕㅇ")){
                        if(array[0].equals("ㅅ")){
                            if(array[1]==null){
                                array[1] = "ㅓ";
                                array[2] = alphabet.substring(1);
                                cho = false;
                            }
                        }else {
                            if(cho==true){
                                array[1] = "ㅓ";
                                array[2] = alphabet.substring(1);
                                cho = false;
                            }
                        }
                    }else if(array[0].equals("ㄱ")||array[0].equals("ㄹ")||array[0].equals("ㅊ")){
                        if(array[1]==null){
                            array[1] = alphabet.substring(0,1);
                            array[2] = alphabet.substring(1);
                            cho = false;
                        }else {
                            print(array);
                            array[0] = "ㅇ";
                            array[1] = alphabet.substring(0,1);
                            array[2] = alphabet.substring(1);
                        }
                    }else {
                        if(cho==false){
                            print(array);
                            array[0] = "ㅇ";
                            array[1] = alphabet.substring(0,1);
                            array[2] = alphabet.substring(1);
                        }else {
                            array[1] = alphabet.substring(0,1);
                            array[2] = alphabet.substring(1);
                            cho = false;
                        }
                    }
                }
            }else if(alphabet.length()==3){
                if(alphabet.substring(0,1).equals("초")){
                    if(abb==true){
                        if(alphabet.substring(2).equals("ㄴ")){
                            hangle = hangle + "그러나";
                            print(array);
                            ttsClient.speak("그러나",TextToSpeech.QUEUE_FLUSH,null);
                            abb = false;
                        }
                    }else {
                        if(array[0]!=null){
                            print(array);
                        }

                        if(alphabet.equals("초성ㄱ")||alphabet.equals("초성ㅅ")||alphabet.equals("초성ㄹ")||alphabet.equals("초성ㅊ")){
                            array[0] = alphabet.substring(2);
                            cho = true;
                        }else{
                            array[0] = alphabet.substring(2);
                            array[1] = "ㅏ";
                            cho = true;
                        }
                    }
                }else if(alphabet.substring(0,1).equals("중")){
                    if(abb==true){
                        if(alphabet.substring(2).equals("ㅓ")){
                            hangle = hangle + "그래서";
                            print(array);
                            ttsClient.speak("그래서",TextToSpeech.QUEUE_FLUSH,null);
                            abb = false;
                        }else if(alphabet.substring(2).equals("ㅔ")){
                            hangle = hangle + "그런데";
                            print(array);
                            ttsClient.speak("그런데",TextToSpeech.QUEUE_FLUSH,null);
                            abb = false;
                        }else if(alphabet.substring(2).equals("ㅗ")){
                            hangle = hangle + "그리고";
                            print(array);
                            ttsClient.speak("그리고",TextToSpeech.QUEUE_FLUSH,null);
                            abb = false;
                        }else if(alphabet.substring(2).equals("ㅕ")){
                            hangle = hangle + "그리하여";
                            print(array);
                            ttsClient.speak("그리하여",TextToSpeech.QUEUE_FLUSH,null);
                            abb = false;
                        }
                    }else {
                        if(array[0]==null){
                            array[0] = "ㅇ";
                            array[1] = alphabet.substring(2);
                        }else {
                            if(alphabet.substring(2).equals("ㅐ")&&att==false){
                                if(array[1]==null){
                                    array[1]=alphabet.substring(2);
                                    cho = false;
                                }else if(array[1].equals("ㅏ")&&cho==false){
                                    print(array);
                                    array[0] = "ㅇ";
                                    array[1] = "ㅐ";
                                }else if(array[1].equals("ㅏ")&&cho==true){
                                    array[1] = "ㅐ";
                                    cho = false;
                                }else if(array[1].equals("ㅑ")){
                                    array[1] = "ㅒ";
                                }else if(array[1].equals("ㅘ")){
                                    array[1] = "ㅙ";
                                }else if(array[1].equals("ㅝ")){
                                    array[1] = "ㅞ";
                                }else if(array[1].equals("ㅜ")){
                                    array[1] =  "ㅟ";
                                }else{
                                    print(array);
                                    array[0] = "ㅇ";
                                    array[1] = "ㅐ";
                                    cho = false;
                                }
                            }else if(array[0].equals("ㄱ")||array[0].equals("ㅅ")||array[0].equals("ㄹ")||array[0].equals("ㅊ")){
                                if(array[1]==null){
                                    array[1]=alphabet.substring(2);
                                    cho = false;
                                    att = false;
                                }else {
                                    print(array);
                                    array[0] = "ㅇ";
                                    array[1] = alphabet.substring(2);
                                    att = false;
                                }
                            }else {
                                if(cho==false){
                                    print(array);
                                    array[0] = "ㅇ";
                                    array[1] = alphabet.substring(2);
                                    att = false;
                                }else {
                                    array[1] = alphabet.substring(2);
                                    cho = false;
                                    att = false;
                                }
                            }
                        }
                    }
                }else if(alphabet.substring(0,1).equals("종")){
                    if(array[0]==null && alphabet.substring(2).equals("ㄱ")){
                        abb = true;
                    }
                    if(abb==true){
                        if(alphabet.substring(2).equals("ㄴ")){
                            hangle = hangle + "그러면";
                            print(array);
                            ttsClient.speak("그러면",TextToSpeech.QUEUE_FLUSH,null);
                            abb = false;
                        }
                    }else {
                        if(array[2].equals(" ")){
                            array[2] = alphabet.substring(2);
                            cho = false;
                        }else {
                            if(array[2].equals("ㄱ")){
                                if(alphabet.substring(2).equals("ㅅ")){
                                    array[2] = "ㄳ";
                                }else {
                                    array[2] = "ㄲ";
                                }
                            }else if(array[2].equals("ㄴ")){
                                if(alphabet.substring(2).equals("ㅈ")){
                                    array[2] = "ㄵ";
                                }else{
                                    array[2] = "ㄶ";
                                }
                            }else if(array[2].equals("ㄹ")){
                                if(alphabet.substring(2).equals("ㄱ")){
                                    array[2] = "ㄺ";
                                }else if(alphabet.substring(2).equals("ㅁ")){
                                    array[2] = "ㄻ";
                                }else if(alphabet.substring(2).equals("ㅂ")){
                                    array[2] = "ㄼ";
                                }else if(alphabet.substring(2).equals("ㅅ")){
                                    array[2] = "ㄽ";
                                }else if(alphabet.substring(2).equals("ㅌ")){
                                    array[2] = "ㄾ";
                                }else if(alphabet.substring(2).equals("ㅍ")){
                                    array[2] = "ㄿ";
                                }else if(alphabet.substring(2).equals("ㅎ")){
                                    array[2] = "ㅀ";
                                }
                            }else if(array[2].equals("ㅂ")){
                                array[2] = "ㅄ";
                            }
                        }
                    }
                }
            }
            dcon = false;
            realdcon = false;
        }

        braille = "";
    }

    public void print(String[] array){
        TextView textView = (TextView) findViewById(R.id.text);

        if(abb==true||num==true){
            textView.setText(hangle);
        } else if(array[0]==null){
            textView.setText(hangle);
        } else {
            String a = com.Combi(array);
            hangle = hangle + a;

            ttsClient.speak(a,TextToSpeech.QUEUE_FLUSH,null);
            textView.setText(hangle);
            array[0] = null; array[1] = null; array[2] = " ";
            cho = false; dcon = false; realdcon = false;
            braille = "";
        }
    }
}