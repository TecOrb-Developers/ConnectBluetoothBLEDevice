package com.hr;

public class HRValidationHelper {

    public static boolean isNull(String input) {
         return (input == null || input.trim().equals("") || input.length() < 1 || input.trim().equals("null") || input.trim().equals("nil"));
    }

    public static String optional(String input){
        if(input==null||input.trim().equals("")||input.length()<1||input.trim().equals("null")|| input.trim().equals("nil")){
            return "";
        }else {
            return input.replace("null","");
        }
    }
}
