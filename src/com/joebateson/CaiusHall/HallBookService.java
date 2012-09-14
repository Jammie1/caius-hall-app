package com.joebateson.CaiusHall;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.app.IntentService;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class HallBookService extends IntentService {

    public HallBookService() {
        super("HallBookService");
    }

    private class BookAllDesiredHallsTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean veggie = settings.getBoolean("veggie", false);

            int[] days = {Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY};

            //String[] dayTypes = new String[7];
            Map<Integer, String> dayTypes = new HashMap<Integer, String>(7);

            String daySetting = settings.getString("hallType", "undefined");
            if (daySetting.equals("advanced")){
                dayTypes.put(days[0], settings.getString("hallTypeMonday", "undefined"));
                dayTypes.put(days[1], settings.getString("hallTypeTuesday", "undefined"));
                dayTypes.put(days[2], settings.getString("hallTypeWednesday", "undefined"));
                dayTypes.put(days[3], settings.getString("hallTypeThursday", "undefined"));
                dayTypes.put(days[4], settings.getString("hallTypeFriday", "undefined"));
                dayTypes.put(days[5], settings.getString("hallTypeSaturday", "undefined"));
                dayTypes.put(days[6], settings.getString("hallTypeSunday", "undefined"));
            } else if (daySetting.equals("alwaysFirst")){
                for (int day : days){
                    dayTypes.put(day, "first");
                }
            } else if (daySetting.equals("alwaysFormal")) {
                for (int day : days){
                    dayTypes.put(day, "formal");
                }
            }

            for (int day : days) {
                Date theDay = DisplayHallInfoActivity.futureDay(day);
                if (dayTypes.get(day).equals("first")){
                    DisplayHallInfoActivity.netBookHall(theDay, true, veggie);
                    DisplayHallInfoActivity.localPutHallBooking(settings, theDay, true, veggie);
                } else if (dayTypes.get(day).equals("formal")){
                    DisplayHallInfoActivity.netBookHall(theDay, false, veggie);
                    DisplayHallInfoActivity.localPutHallBooking(settings, theDay, false, veggie);
                } else if (dayTypes.get(day).equals("noHall")){
                    //DisplayHallInfoActivity.netCancelHall(theDay);
                    //DisplayHallInfoActivity.localCancelHallBooking(settings, theDay);
                    Log.w("HallBookService", "(unimplemented) CANCEL BOOKING ON " + theDay.toString());
                }
            }
            return false;
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        new BookAllDesiredHallsTask().execute();
    }

    private class BookHallTask extends AsyncTask<String, Void, Boolean> {

        private Date day;
        private boolean firstHall;
        private boolean vegetarian;

        protected BookHallTask(Date day, boolean firstHall, boolean veggie){
            this.day = day;
            this.firstHall = firstHall;
            this.vegetarian = veggie;
        }

        @Override
        protected Boolean doInBackground(String... params) {

            return DisplayHallInfoActivity.netBookHall(day, firstHall, vegetarian);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!result){
                // problem
            } else {
                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                DisplayHallInfoActivity.localPutHallBooking(settings, day, firstHall, vegetarian);
            }
        }

    }
}
