package com.example.solbin.myapplication;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;

import static app.akexorcist.bluetotohspp.library.BluetoothState.REQUEST_ENABLE_BT;

public class Reader extends AppCompatActivity implements TextToSpeech.OnInitListener {
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

    private static final String TAG = "Main";

    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter mBluetoothAdapter;
    private IntentFilter filter;
    private int MESSAGE_READ = 99;

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
                    ttsClient.speak("사용설명",TextToSpeech.QUEUE_FLUSH,null);
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
            }
        }
        return false;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };
    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<BluetoothDevice> mBleDevice = new ArrayList<>();

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            TextView textView = (TextView) findViewById(R.id.text);


            if(msg.what == MESSAGE_READ){
                String readMessage = new String((byte[]) msg.obj, 0, msg.arg1);
                /////////////////////////////////////////////////////////////////////////////////////////////
                String[] words = readMessage.split("\\s");

                for (String wo : words ){
                    braille = wo;
                    change();
                }
                print(array);
                ttsClient.speak((String) textView.getText(),TextToSpeech.QUEUE_FLUSH,null);
                ////////////////////////////////////////////////////////////////////////////////////////////
            }
            Log.e("Handler Msg", ""+msg.what);
            return false;
        }
    });

    public void onInit(int i) {
        ttsClient.speak("카메라로 점자입력",TextToSpeech.QUEUE_FLUSH,null);
        array[2] = " "; //이유는 모르겠는데 자꾸 처음에 이 전에 했던 종성이 들어감
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reader);

        ttsClient= new TextToSpeech(getApplicationContext(),this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mArrayAdapter = new ArrayAdapter<String>(Reader.this, android.R.layout.select_dialog_singlechoice);
        if (mBluetoothAdapter == null) {
            finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBleDevice.add(device);
            }
        }
        // Create a BroadcastReceiver for ACTION_FOUND
        // Register the BroadcastReceiver
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                Reader.this);
        alertBuilder.setTitle("페어링할 기기를 선택하세요");
        alertBuilder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        // Adapter 셋팅
        alertBuilder.setAdapter(mArrayAdapter,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // AlertDialog 안에 있는 AlertDialog
                        String strName = mArrayAdapter.getItem(id);
                        new ConnectThread(mBleDevice.get(id)).run();
                    }
                });
        alertBuilder.show();
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
                                array[1] = alphabet.substring(2);
                                cho = false;
                            }
                        }
                    }
                }else {
                    array[2] = alphabet.substring(2);
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

            textView.setText(hangle);
            array[0] = null; array[1] = null; array[2] = " ";
            cho = false; dcon = false; realdcon = false;
            braille = "";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            Toast.makeText(Reader.this, device.getName() + "\n" + device.getAddress(), Toast.LENGTH_SHORT).show();
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                if(Build.VERSION.SDK_INT >= 10){
                    try {
                        final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                        tmp = (BluetoothSocket) m.invoke(device, MY_UUID);
                    } catch (Exception e) {
                        Log.e(TAG, "Could not create Insecure RFComm Connection",e);
                    }
                }else{
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                }
            } catch (IOException e) {
                Toast.makeText(Reader.this, "에러 : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ConnectThread main E", e.getMessage());
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                new ConnectedThread(mmSocket).run();
                Toast.makeText(Reader.this, "연결되었습니다", Toast.LENGTH_SHORT).show();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Toast.makeText(Reader.this, "연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    Log.e("ConnectThread close E", closeException.getMessage());
                }
                Toast.makeText(Reader.this, "연결에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                Log.e("ConnectThread connect E", connectException.getMessage());
            }
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(Reader.this, "에러 : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ConnectedThread main E", e.getMessage());
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();

                    //cancel();
                    //if(buffer.equals())
                } catch (IOException e) {
                    Toast.makeText(Reader.this, "에러 : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ConnectedThread read E", e.getMessage());
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(Reader.this, "에러 : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ConnectedThread write E", e.getMessage());
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
}