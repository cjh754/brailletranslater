package com.example.solbin.myapplication;

import java.util.LinkedList;

public class Hash {
    public static void main(String[] args) { }

    String getbraille(String braille, boolean num, boolean realdcon, boolean att){
        HashTable h = new HashTable(64);
        h.put("000000", " ", " ", "");
        h.put("000001", "초성ㅅ", "", "ㅆ"); //된소리표이기도 함
        h.put("000010", "초성ㄹ", "", "");
        h.put("000011", "초성ㅊ", "", "");
        h.put("000100", "초성ㄱ", "", "ㄲ");
        h.put("000101", "초성ㅈ", "", "ㅉ");
        h.put("000110", "초성ㅂ", "", "ㅃ");
        h.put("000111", "", "", ""); //얘 뭐에 쓰는 애더라?
        h.put("001000", "종성ㅅ", "", "");
        h.put("001001", "", "", ""); //붙임표
        h.put("001010", "종성ㄷ", "", "");
        h.put("001011", "종성ㅎ", "", "");
        h.put("001100", "종성ㅆ", "중성ㅖ", ""); //얘 어떡하지
        h.put("001101", "중성ㅛ", "", "");
        h.put("001110", "중성ㅑ", "", "");
        h.put("001111", "", "", ""); //수표
        h.put("010000", "종성ㄹ", "", "");
        h.put("010001", "종성ㅁ", "", "");
        h.put("010010", "종성ㄴ", "", "");
        h.put("010011", "종성ㅍ", "", "");
        h.put("010100", "초성ㄷ", "9", "ㄸ");
        h.put("010101", "중성ㅡ", "", "");
        h.put("010110", "초성ㅎ", "0", "");
        h.put("010111", "중성ㅢ", "", "");
        h.put("011000", "종성ㅊ", "", "");
        h.put("011001", "종성ㅌ", "", "");
        h.put("011010", "종성ㅋ", "", "");
        h.put("011011", "종성ㅇ", "", "");
        h.put("011100", "중성ㅓ", "", "");
        h.put("011101", "ㅡㄹ", "", "");
        h.put("011110", "ㅓㄹ", "", "");
        h.put("011111", "ㅓㄴ", "", "");
        h.put("100000", "종성ㄱ", "1", "");
        h.put("100001", "ㅕㄴ", "", "");
        h.put("100010", "초성ㅁ", "5", "");
        h.put("100011", "중성ㅕ", "", "");
        h.put("100100", "초성ㄴ", "3", "");
        h.put("100101", "중성ㅠ", "", "");
        h.put("100110", "초성ㅍ", "4", "");
        h.put("100111", "ㅓㄱ", "", "");
        h.put("101000", "종성ㅈ", "", "");
        h.put("101001", "중성ㅗ", "", "");
        h.put("101010", "중성ㅣ", "", "");
        h.put("101011", "ㅡㄴ", "", "");
        h.put("101100", "중성ㅜ", "", "");
        h.put("101101", "ㅗㄱ", "", "");
        h.put("101110", "중성ㅔ", "", "");
        h.put("101111", "중성ㅚ", "", "");
        h.put("110000", "종성ㅂ", "2", "");
        h.put("110001", "중성ㅏ", "", "");
        h.put("110010", "초성ㅌ", "8", "");
        h.put("110011", "ㅕㄹ", "", "");
        h.put("110100", "초성ㅋ", "6", "");
        h.put("110101", "가", "", "");
        h.put("110110", "ㅜㄴ", "7", ""); //원래 value엔 초성ㅇ가, value3에 ㅜㄴ이 있어야함
        h.put("110111", "ㅕㅇ", "", "");
        h.put("111000", "사", "", "");
        h.put("111001", "중성ㅘ", "", "");
        h.put("111010", "중성ㅐ", "중성ㅐ", ""); //ㅐ+ㅑ,ㅘ,ㅝ,ㅜ =ㅒ, ㅙ, ㅞ, ㅟ  이거 안하려면 붙임표
                                                                                //att=true면 value2 가져가는데 소화액 같이 모음+ㅐ 일 때 써야함
        h.put("111011", "ㅗㄴ", "", "");
        h.put("111100", "중성ㅝ", "", "");
        h.put("111101", "ㅜㄹ", "", "");
        h.put("111110", "ㅣㄴ", "", "");
        h.put("111111", "ㅗㅇ", "", "");

        String hangle = "";

        hangle = h.get(braille, num, realdcon, att);

        return hangle;
    }
}

class HashTable {
    class Node {
        String key;
        String value;
        String value2;
        String value3;

        public Node(String key, String value, String value2, String value3) {
            this.key = key;
            this.value = value;
            this.value2 = value2;
            this.value3 = value3;
        }

        String value() {
            return value;
        }
        String value2() {
            return value2;
        }
        String value3() {
            return value3;
        }

        void value(String value) {
            this.value = value;
        }
        void value2(String value2) {
            this.value2 = value2;
        }
        void value3(String value3) {
            this.value3 = value3;
        }

    }

    LinkedList<Node>[] data;

    HashTable(int size) {
        this.data = new LinkedList[size];
    }

    int getHashCode(String key) {
        int hashCode = 0;
        for (char c : key.toCharArray()) {
            hashCode += c;
        }
        return hashCode;
    }

    int convertToindex(int hashcode) {
        return hashcode % data.length;
    }

    Node searchKey(LinkedList<Node> list, String key) {
        if (list == null)
            return null;
        for (Node node : list) {
            if (node.key.equals(key)) {
                return node;
            }
        }
        return null;
    }

    void put(String key, String value, String value2, String value3) {
        int hashCode = getHashCode(key);
        int index = convertToindex(hashCode);

        LinkedList<Node> list = data[index];

        if (list == null) {
            list = new LinkedList<Node>();
            data[index] = list;
        }

        Node node = searchKey(list, key);

        if (node == null) {
            list.addLast(new Node(key, value, value2, value3));
        }
    }

    String get(String key, boolean num, boolean realdcon, boolean att) {
        int hashCode = getHashCode(key);
        int index = convertToindex(hashCode);
        LinkedList<Node> list = data[index];
        Node node = searchKey(list, key);

        if(num==true) { //수표
            return node==null ? "Not found" : node.value2;
        }else if(realdcon==true) { //된소리표
            return node==null ? "Not found" : node.value3;
        }else if(att==true){ //붙임표
            return node==null ? "Not found" : node.value2;
        }else {
            return node==null ? "Not found" : node.value;
        }
    }
}