package com.example.solbin.myapplication;

import java.util.ArrayList;

public class combination {
    static String [] cho = {"ㄱ","ㄲ","ㄴ","ㄷ","ㄸ","ㄹ","ㅁ","ㅂ","ㅃ","ㅅ","ㅆ","ㅇ","ㅈ","ㅉ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ"};
    static String [] jung = {"ㅏ","ㅐ","ㅑ","ㅒ","ㅓ","ㅔ","ㅕ","ㅖ","ㅗ","ㅘ","ㅙ","ㅚ","ㅛ","ㅜ","ㅝ","ㅞ","ㅟ","ㅠ","ㅡ","ㅢ","ㅣ"};
    static String [] jong = {" ","ㄱ","ㄲ","ㄳ","ㄴ","ㄵ","ㄶ","ㄷ","ㄹ","ㄺ","ㄻ","ㄼ","ㄽ","ㄾ","ㄿ","ㅀ","ㅁ","ㅂ","ㅄ","ㅅ","ㅆ","ㅇ","ㅈ","ㅊ","ㅋ","ㅌ","ㅍ","ㅎ"};
    static int chonum, jungnum=0, jongnum;

    static int a = 0;
    static int uni;

    public static void main(String[] args) { }

    String Combi(String[] array) { //'ㄱ'만 입력한 상태에서 띄어쓰기 해버리면 어떡할건지
        if(array[0]!=null){
            for (int i = 0; i < cho.length; i++) {
                if (cho[i].equals(array[0])) {
                    chonum = i;
                }
            }
            if(array[1]!=null){
                for (int i = 0; i < jung.length; i++) {
                    if (jung[i].equals(array[1])) {
                        jungnum = i;
                    }
                }
            }
            for (int i = 0; i < jong.length; i++) {
                if (jong[i].equals(array[2])) {
                    jongnum = i;
                }
            }
            uni = 44032 + (chonum * 588) + (jungnum * 28) + jongnum;
        }else {
            uni = 32;
        }

        return (char)uni+"";
    }
}