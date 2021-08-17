package com.itcast;

public class Demo1 {

    public static String reverseRecursive(String s){
        int length = s.length();
        if(length<=1){
            return s;

        }
        String left  = s.substring(0,length/2);
        String right = s.substring(length/2 ,length);
        String afterReverse = reverseRecursive(right)+reverseRecursive(left);//此处是递归的方法调用
        return afterReverse;
    }

    public static void main(String[] args) {

        String s = Demo1.reverseRecursive("abcdef");
        System.out.println(s);
    }

}
