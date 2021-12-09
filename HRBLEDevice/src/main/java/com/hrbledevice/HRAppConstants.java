package com.hrbledevice;


 public interface HRAppConstants {

    /*============== UNIQUE IDENTIFIER ============================*/
    String LOCATION_NOTIFICATION_BROADCAST = "com.eSubha.location";
    String FILTER_NOTIFICATIONS = "com.eSubha.notification";
    String CHANNEL_ID = "com.eSubha.channel";
    String CHANNEL_ID_TRIP = "com.eSubha.channel.custom_sound";
    String CHANNEL_ID_MESSAGE = "com.eSubha.channel.message";
    String CHANNEL_NAME = "Notification";
    String CHANNEL_DESCRIPTION = "Example Partner Notifications";


    /*============== BROADCAST RECEIVER UNIQUE IDENTIFIERS =============*/
    String CHARACTERISTIC_BROADCAST= "com.char.location";
    String READ_CHARACTERISTIC_BROADCAST= "com.read.location";


    /*================ CHARACTERISTICS IDENTIFIERS ==================*/
    int CHECK_NUMBER_ROTATIONS_CHARACTERISTIC = 5;
    int BUZZER_START_TO_WORK = 7;
    int CLICK_BATTERY = 13;
}