package com.apkglobal.tablemaster;

import android.content.res.AssetManager;
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
    String TAG = "------------------>"; //for lagcat identification purposes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        printList(getFaculty());
    }

    public  ArrayList<String> populateDays() {
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
                contents = sheet.getCell(j+2, 0).getContents(); //add the fetched data to the array list | Faculties
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

}
