
#include <string.h>
#include <SoftwareSerial.h>

SoftwareSerial mySerial(2,3); //시리얼 통신 하는 포트 RX 2, TX 3

const int braille1 = 4;
const int braille2 = 5;
const int braille3 = 6;
const int braille4 = 7;
const int braille5 = 8;
const int braille6 = 9; //점자 입력받는 각 버튼들 4-6번 포트

int b1State = 0;
int b2State = 0;
int b3State = 0;
int b4State = 0;
int b5State = 0;
int b6State = 0;  //버튼들 초기 상태 전부 0으로 초기화

int braille[6] = {0,0,0,0,0,0}; //버튼 상태 입력받아서 전달할 점자 배열

String sb1, sb2, sb3, sb4, sb5, sb6;
String sBraille = ""; //String으로 변환하여 안드로이드로 전달

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  mySerial.begin(9600);

  pinMode (braille1, INPUT);
  pinMode (braille2, INPUT);
  pinMode (braille3, INPUT);
  pinMode (braille4, INPUT);
  pinMode (braille5, INPUT);
  pinMode (braille6, INPUT);  //점자 입력받을 버튼들 INPUT으로 설정

}

void loop() {
  // put your main code here, to run repeatedly:
  b1State = digitalRead (braille1);
  b2State = digitalRead (braille2);
  b3State = digitalRead (braille3);
  b4State = digitalRead (braille4);
  b5State = digitalRead (braille5);
  b6State = digitalRead (braille6); //버튼 상태 입력받아 bState에 저장

  if(b1State == HIGH){
      braille[0] = 1;
  }
  if(b2State == HIGH){
      braille[1] = 1;
  }
  if(b3State == HIGH){
      braille[2] = 1;
  }
  if(b4State == HIGH){
      braille[3] = 1;
  }
  if(b5State == HIGH){
      braille[4] = 1;
  }
  if(b6State == HIGH){
      braille[5] = 1;
  }  //입력받은 버튼 상태가 HIGH일 경우 점자 배열에 1 저장

  sb1 = String(braille[0]);
  sb2 = String(braille[1]);
  sb3 = String(braille[2]);
  sb4 = String(braille[3]);
  sb5 = String(braille[4]);
  sb6 = String(braille[5]); //배열 분리해서 저장

  sBraille = sb1 + sb2 + sb3 + sb4 + sb5 + sb6; //분리한 배열을 문자열로 병합

  mySerial.print( sBraille );
  mySerial.print(" \n");  //블루투스로 안드로이드에 전송
  
  Serial.println("Sent:  " + sBraille); //아두이노 시리얼모니터에 출력

}
