package com.apkglobal.tablemaster;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;

import jxl.Sheet;
import jxl.Workbook;

import static com.apkglobal.tablemaster.R.id.tv;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> facultyList = new ArrayList<String>();
    ArrayList<String> timeSlotList = new ArrayList<String>();
    ArrayList<String> days = new ArrayList<String>();
    ArrayList<String> lectures = new ArrayList<>();
    String TAG = "->"; //for lagcat identification purposes
    SQLiteDatabase sd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        incept();
        populateDays();
        printList(getFaculty());
        populate();
        read();
    }

    public void read() /*Displays all records from the table*/ {
        sd = openOrCreateDatabase("minor_project", Context.MODE_PRIVATE, null);
        Log.d(TAG, " commencing reading operation from the database...");
        Cursor sc = sd.rawQuery("select * from timetable", null);
        int cursor_count = sc.getCount();
        Log.e("--------->", "Total queries: " + cursor_count);
        if (cursor_count > 0) {
            sc.moveToFirst();
            int count = 1;
            while (count <= cursor_count) {
                Log.d(TAG, "" + sc.getInt(0) + " " + sc.getString(1) + " " + sc.getInt(2) + " " + sc.getString(3) + " " + sc.getString(4) + " " + sc.getString(5) + " " + sc.getString(6) + " " + sc.getString(7) + " " + sc.getString(8) + " " + sc.getString(9) + " " + sc.getString(10) + " " + sc.getString(11) + " ");
                count += 1;
                sc.moveToNext();
            }/*
            do{
                Log.d(TAG, ""+sc.getInt(0)+" "+sc.getString(1)+" "+sc.getInt(2)+" "+sc.getString(3)+" "+sc.getString(4)+" "+sc.getString(5)+" "+sc.getString(6)+" "+sc.getString(7)+" "+sc.getString(8)+" "+sc.getString(9)+" "+sc.getString(10)+" "+sc.getString(11)+" ");
            }while(sc.moveToNext());
        }*/
        }
        sd.close();


    }

    public void populate()/*Populates the timetable in sqlite database*/ {
        try {
            String contents_facultyName = "";
            String contents = "";
            String contents_time = "";
            String contents_day = "";
            AssetManager assetManager = getAssets();
            assetManager = this.getAssets();
            InputStream inputStream = assetManager.open("file.xls"); //open the excel sheet
            Workbook workbook = Workbook.getWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(0);
            int rows = sheet.getRows();
            int cols = sheet.getColumns();
            Log.d(TAG,"rows: "+rows+" columns: "+cols);

            int time_index = 1;
            int day_index = 0;
            for (int faculty = 0; faculty < cols - 2; faculty += 1) {
                lectures.clear();
                contents_facultyName = sheet.getCell(faculty + 2, 0).getContents();
                for (int row = 1; row <rows; row++) {
                    contents_day = sheet.getCell(day_index, row).getContents();
                    contents_time = sheet.getCell(time_index, row).getContents();
                    contents = sheet.getCell(faculty + 2, row).getContents();
                    contents = contents.replaceAll("\n", "");
                    if (contents.length() == 0)
                        contents = "FREE";
                    if (contents_time.length() == 0)    /*separation of days*/
                        contents = "BREAK";
                    if (contents_time.equals("12:45-01:40"))
                        contents = "RECESS";
                    Log.d(TAG, "faculty: " + contents_facultyName + " day: " + contents_day + " time: " + contents_time + " lecture: " + contents + "length: " + contents.length());
                    if (!contents.equals("BREAK")) {
                        lectures.add(contents);
                    }
                }

                Log.d(TAG, "Inserting into the database...");
                sd = openOrCreateDatabase("minor_project", Context.MODE_PRIVATE, null);
                int listSize = lectures.size();
                Log.d(TAG, "\tLectureList size: " + listSize);
                for (int day = 0; day < 5; day++) {
                    Log.d(TAG, "\t\t day count: " + (day + 1));
                    Log.d(TAG, "" + lectures.get(8 * day) + " " + (8 * day));
                    Log.d(TAG, "" + lectures.get(8 * day + 1) + " " + (8 * day + 1));
                    Log.d(TAG, "" + lectures.get(8 * day + 2) + " " + (8 * day + 2));
                    Log.d(TAG, "" + lectures.get(8 * day + 3) + " " + (8 * day + 3));
                    Log.d(TAG, "" + lectures.get(8 * day + 4) + " " + (8 * day + 4));
                    Log.d(TAG, "" + lectures.get(8 * day + 5) + " " + (8 * day + 5));
                    Log.d(TAG, "" + lectures.get(8 * day + 6) + " " + (8 * day + 6));
                    Log.d(TAG, "" + lectures.get(8 * day + 7) + " " + (8 * day + 7));

                    String query = " insert into timetable(day, faculty_id, faculty, ts_905_1000, ts_1000_1055, " +
                            "ts_1055_1150, ts_1150_1245, ts_1245_0140, ts_0140_0235,ts_0235_0330," +
                            "ts_0330_0425) values('" + days.get(day) + "', " + (faculty + 1) + ", '" + contents_facultyName + "','" + lectures.get(8 * day) + "'," +
                            "'" + lectures.get(8 * day + 1) + "','" + lectures.get(8 * day + 2) + "','" + lectures.get(8 * day + 3) + "'," +
                            "'" + lectures.get(8 * day + 4) + "','" + lectures.get(8 * day + 5) + "','" + lectures.get(8 * day + 6) + "'," +
                            "'" + lectures.get(8 * day + 7) + "');";
                    sd.execSQL(query);
                    Log.d(TAG, "\tQuery inserted...");
                }
                sd.close();
            }

        } catch (Exception exception) {
            Toast.makeText(this, "Exception while fetching faculties list from teh excel sheet", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "" + exception);

        }
    }

    public void incept() {

        Log.d(TAG, "Creating the timetable in sqlite database");
        sd = openOrCreateDatabase("minor_project", Context.MODE_PRIVATE, null);
        sd.execSQL("drop table timetable");
        sd.execSQL("create table if not exists timetable (id integer not null primary key autoincrement, day text, faculty_id integer, faculty text);");
        timeSlotList = getTimeSlots();
        Log.d(TAG, "Adding timeslots to the table");

        try {
            for (String timeslot : timeSlotList) {
                timeslot = purify(timeslot);
                Log.d(TAG, "\tPurifiied time :" + timeslot);
                sd.execSQL("alter table timetable add " + timeslot + " varchar(50);");
                Log.d(TAG, "\t\ttimeslot " + timeslot + " added...");
            }
            Log.d(TAG, "Table timetable created succesfully");

        } catch (Exception exception) {
            Log.d(TAG, "Timeslots columns already exits, no need for alteration");
        }
        sd.close();
    }         /*Creating the timetable in sqlite database*/

    public String purify(String time) {
        time = time.replaceAll(":", "");
        time = time.replaceAll("-", "_");
        time = time.replace(" ", "");
        time = "ts_" + time;
        return time;
    } /*Purifies time format to fit for database norms*/

    public ArrayList<String> populateDays() {
        days.clear();
        days.add("Monday");
        days.add("Tuesday");
        days.add("Wednesday");
        days.add("Thursday");
        days.add("Friday");
        days.add("Saturday");
        days.add("Sunday");
        return days;
    } /*Populates arraylist | Days*/

    public ArrayList<String> getTimeSlots() {
        try {
            String contents = "";
            AssetManager assetManager = getAssets();
            assetManager = this.getAssets();
            InputStream inputStream = assetManager.open("file.xls"); //open the excel sheet
            Workbook workbook = Workbook.getWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(0);
            int row = sheet.getRows();
            int col = sheet.getColumns();
            timeSlotList.clear(); /*Resetting the facultyList | arrayList */
            for (int i = 1; i <= 8; i++) {
                contents = sheet.getCell(1, i).getContents();
                Log.d(TAG, "i: " + i + " content: " + contents);
                timeSlotList.add(contents); //add the fetched data to the array list | lecture timeslots
            }
        } catch (Exception exception) {
            Toast.makeText(this, "Exception while fetching timeslots list from teh excel sheet", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Exception caught while fetching timeslot list form the excel sheet");
        }
        return timeSlotList;
    } /*Fetches timeslots of lecture from excel sheet | Timeslots lecture*/

    public ArrayList<String> getFaculty() {
        try {
            String contents = "";
            AssetManager assetManager = getAssets();
            assetManager = this.getAssets();
            InputStream inputStream = assetManager.open("file.xls"); //open the excel sheet
            Workbook workbook = Workbook.getWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(0);
            int row = sheet.getRows();
            int col = sheet.getColumns();
            facultyList.clear(); /*Resetting the facultyList | arrayList */
            for (int j = 0; j < col - 2; j++) {
                contents = sheet.getCell(j + 2, 0).getContents(); //add the fetched data to the array list | Faculties
                Log.d(TAG, "j: " + j + " contetns: " + contents);
                facultyList.add(contents);
            }
        } catch (Exception exception) {
            Toast.makeText(this, "Exception while fetching faculties list from teh excel sheet", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Exception caught while fetching faculty list form the excel sheet");

        }
        return facultyList;
    }

    public void printList(ArrayList<String> arraylist) {
        TextView textView = (TextView) findViewById(tv);
        String str = "";
        for (String item : arraylist)
            str += " " + item + "\n";
        textView.setText(str);
    }

    /*


            lectures.clear();
            for (int faculty = 0; faculty < cols - 2; faculty++) {
                for (int row = row + 9; row < faculty * 9 + 10; faculty += 1) {

                }
            }


            for (int faculty = 0; faculty < cols - 2; faculty++) {
                contents_facultyName = sheet.getCell(faculty + 2, 0).getContents();
                for (int row = (9 * faculty + 1); row < (9 * faculty + 10); row++) {
                    content_time = sheet.getCell(1, row).getContents();
                    content_day = sheet.getCell(0, row).getContents();
                    contents = sheet.getCell(faculty + 2, row).getContents();
                    contents = contents.replaceAll("\n", "");
                    if (contents.length() == 0)
                        contents = "FREE";
                    if (content_time.length() == 0)
                        contents = "BREAK";
                    if (content_time.equals("12:45-01:40"))
                        contents = "RECESS";
                    Log.d(TAG, "faculty: " + contents_facultyName + " day: " + content_day + " time: " + content_time + " lecture: " + contents + "length: " + contents.length());
                    if (!contents.equals("BREAK")) {
                        lectures.add(contents);
                    }
                }


                Log.d(TAG, "Inserting into the database...");
                sd = openOrCreateDatabase("minor_project", Context.MODE_PRIVATE, null);
                int listSize = lectures.size();
                Log.d(TAG, "\tLectureList size: " + listSize);
                for (int day = 0; day < 5; day++) {
                    Log.d(TAG, "\t\t day count: " + day);
                    Log.d(TAG, "" + lectures.get(0) + " " + (0));
                    Log.d(TAG, "" + lectures.get(1) + " " + (1));
                    Log.d(TAG, "" + lectures.get(2) + " " + (2));
                    Log.d(TAG, "" + lectures.get(3) + " " + (3));
                    Log.d(TAG, "" + lectures.get(4) + " " + (4));
                    Log.d(TAG, "" + lectures.get(5) + " " + (5));
                    Log.d(TAG, "" + lectures.get(6) + " " + (6));
                    Log.d(TAG, "" + lectures.get(7) + " " + (7));


                   */
/* Log.d(TAG,"\t\t day count: "+day);
                    Log.d(TAG, ""+lectures.get(8*day)+" "+(8*day));
                    Log.d(TAG, ""+lectures.get(8*day+1)+" "+(8*day+1));
                    Log.d(TAG, ""+lectures.get(8*day+2)+" "+(8*day+2));
                    Log.d(TAG, ""+lectures.get(8*day+3)+" "+(8*day+3));
                    Log.d(TAG, ""+lectures.get(8*day+4)+" "+(8*day+4));
                    Log.d(TAG, ""+lectures.get(8*day+5)+" "+(8*day+5));
                    Log.d(TAG, ""+lectures.get(8*day+6)+" "+(8*day+6));
                    Log.d(TAG, ""+lectures.get(8*day+7)+" "+(8*day+7));
*//*

                    String query = " insert into timetable(day, faculty_id, faculty, ts_905_1000, ts_1000_1055, " +
                            "ts_1055_1150, ts_1150_1245, ts_1245_0140, ts_0140_0235,ts_0235_0330," +
                            "ts_0330_0425) values('" + days.get(day) + "', " + (faculty + 1) + ", '" + contents_facultyName + "','" + lectures.get(8 * day) + "'," +
                            "'" + lectures.get(8 * day + 1) + "','" + lectures.get(8 * day + 2) + "','" + lectures.get(8 * day + 3) + "'," +
                            "'" + lectures.get(8 * day + 4) + "','" + lectures.get(8 * day + 5) + "','" + lectures.get(8 * day + 6) + "'," +
                            "'" + lectures.get(8 * day + 7) + "');";
                    sd.execSQL(query);
                }
                sd.close();
                Log.d(TAG, "\tQuery inserted...");

            }
*/


}
