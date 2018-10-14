package com.apkglobal.tablemaster;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import jxl.Sheet;
import jxl.Workbook;


public class Adjustment extends AppCompatActivity {

    final String spinnerSelection[] = new String[7];
    SQLiteDatabase sd;
    ArrayList<Boolean> faculty_present = new ArrayList<>();
    int cursor_count = 0;
    String Tag = "---------->";
    int color_present = Color.GREEN;
    int color_absent = Color.RED;
    int facultyCount = 0;
    String facultyLocked = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjustment);

        /*Declaring layout for handling programatically*/
        FlexboxLayout buttonsLayout = (FlexboxLayout) findViewById(R.id.buttons_layout);
        final LinearLayout spinnerLayout = (LinearLayout) findViewById(R.id.spinner_layout);
        final LinearLayout l_layout = (LinearLayout) findViewById(R.id.linear_layout);
        final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        l_layout.setOrientation(LinearLayout.VERTICAL);

        facultyCount = getFacultyCount();
        Log.d(Tag, "facultyCount: " + facultyCount);
        final ArrayList<String> facultyList = getFaculty();
        final String[] lectureTime = getLectureTime();
        final Boolean[] faculty_present = create_faculty_buttons(buttonsLayout, facultyCount);


        Button btn_adjust = (Button) findViewById(R.id.btn_adjust);
        btn_adjust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getDistinct() >= 1) {
                    int size = facultyCount;
                    for (int faculty_status = 0; faculty_status < facultyCount; faculty_status += 1) {
                        if (faculty_present[faculty_status] == Boolean.FALSE) {
                            facultyLocked = facultyList.get(faculty_status);
                            break;
                        }
                    }
                    Log.d(Tag, "Faculty locked: " + facultyLocked + "at index: ");
                    Toast.makeText(Adjustment.this, "Faculty locked: " + facultyLocked, Toast.LENGTH_SHORT).show();

                    /*Locked faculty found....*/
                    String lD = "Monday";

                    sd = openOrCreateDatabase("minor_project", Context.MODE_PRIVATE, null);
                    Arrays.fill(spinnerSelection, "FREE");
                    for (int lecture = 0; lecture < 8; lecture += 1) {
                        if (lecture != 4) { /*Excluding recess time*/
                            String lecturetime = lectureTime[lecture];
                            Cursor temp = sd.rawQuery("select " + lecturetime + " from timetable where faculty='" + facultyLocked + "' and day = '" + lD + "';", null);
                            temp.moveToFirst();
                            String current_faculty_lecture = temp.getString(0);
                            Log.d(Tag, "checking if free: " + current_faculty_lecture);
                            if (temp.getString(0).equals("FREE")) {
                                Log.d(Tag, "Lecture is Free");
                                Spinner spinner = new Spinner(Adjustment.this);
                                ArrayList<String> spinnerArray = new ArrayList<String>();
                                spinnerArray.add("FREE");
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Adjustment.this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
                                spinner.setAdapter(arrayAdapter);
                                spinnerLayout.addView(spinner);

                            } else {
                                Cursor cursor = sd.rawQuery("select faculty,priority from timetable where " + lecturetime + " = 'FREE' and day = '" + lD + "' " +
                                        " and faculty !='" + facultyLocked + "' order by priority asc; ", null);

                            /*creating the spinner here*/
                                ArrayList<String> spinnerArray = new ArrayList<String>();
                                cursor.moveToFirst();
                                while (!cursor.isAfterLast()) {
                                    spinnerArray.add("" + cursor.getString(0) + " : " + cursor.getInt(1) + " : "+current_faculty_lecture);
                                    Log.d(Tag, "spinner list: " + cursor.getString(0) + " " + cursor.getString(1));
                                    cursor.moveToNext();
                                }
                            /*attaching the spinner to the layout*/
                                final Spinner spinner = new Spinner(Adjustment.this);
                                spinner.setId(lecture);
                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Adjustment.this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
                                spinner.setAdapter(arrayAdapter);
                                final int finalLecture1 = lecture;
                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        Log.d(Tag, "spinner selection: "+finalLecture1+ " lecture: ");
                                        if(finalLecture1 > 4)
                                            spinnerSelection[finalLecture1-1]=spinner.getSelectedItem().toString();
                                        else
                                            spinnerSelection[finalLecture1]=spinner.getSelectedItem().toString();

                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                                spinnerLayout.addView(spinner);
                                String text = spinner.getSelectedItem().toString();
                                final int finalLecture = lecture;

                            }

                        }
                    }
                }

            }
        });

        /*Dynamically creates buttons for each faculty for marking the present status populated in
        * a Linear layout placed statically inside the xml file*/


        Button btn_select = (Button) findViewById(R.id.btn_select);
        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sd = openOrCreateDatabase("minor_project", Context.MODE_PRIVATE, null);
                for (int i = 0; i < 7; i++) {
                    String selection = spinnerSelection[i];
                    selection = selection.split(" :")[0];
                    Log.d(Tag, "selection after purificatino : " + selection);
                    sd.execSQL("update timetable set priority = priority+1 where faculty='" + selection + "' ;");
                }
                Log.d(Tag, "Priorities updated");
                Cursor cursor = sd.rawQuery("select distinct faculty from timetable", null);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Cursor temp = sd.rawQuery("select distinct priority from timetable where faculty='" + cursor.getString(0) + "' ;", null);
                    temp.moveToFirst();
                    Log.d(Tag, "" + cursor.getString(0) + " priority: " + temp.getInt(0));
                    cursor.moveToNext();
                }
            }
        });

    }

    public Boolean[] create_faculty_buttons(FlexboxLayout buttonsLayout, int facultyCount) {
        final Boolean[] faculty_present = new Boolean[facultyCount];
        Arrays.fill(faculty_present, true);
        try {
            sd = openOrCreateDatabase("minor_project", Context.MODE_PRIVATE, null);
            AssetManager assetManager = getAssets();
            assetManager = this.getAssets();
            InputStream inputStream = assetManager.open("file.xls"); //open the excel sheet
            Workbook workbook = Workbook.getWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(0);
            int cols = sheet.getColumns();

            for (int faculty = 2; faculty < cols; faculty += 1) {
                final String faculty_name = sheet.getCell(faculty, 0).getContents();
                final int id = faculty - 2;
                final Button button = new Button(this);
                button.setText(faculty_name);
                button.setHighlightColor(Color.WHITE);
                button.setBackgroundColor(Color.GREEN);
                button.setId(faculty - 2);
                buttonsLayout.addView(button);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (faculty_present[id] == Boolean.TRUE) {
                            faculty_present[id] = Boolean.FALSE;
                            button.setBackgroundColor(Color.RED);
                        } else {
                            faculty_present[id] = Boolean.TRUE;
                            button.setBackgroundColor(Color.GREEN);
                        }
                        Toast.makeText(Adjustment.this, "status: [" + id + "] set to: " + faculty_present[id], Toast.LENGTH_SHORT).show();
                        Toast.makeText(Adjustment.this, "" + faculty_name + " : " + id, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        } catch (Exception exception) {
            Toast.makeText(this, "Execption caught....", Toast.LENGTH_SHORT).show();
        }
        return faculty_present;
    } /*Programmatically creates button for marking
                                                                                                   faculties presence*/

    public ArrayList<String> getFaculty() {
        ArrayList<String> facultyList = new ArrayList<>();
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
                Log.d(Tag, "j: " + j + " contents: " + contents);
                facultyList.add(contents);
            }
        } catch (Exception exception) {
            Toast.makeText(this, "Exception while fetching faculties list from teh excel sheet", Toast.LENGTH_SHORT).show();
            Log.d(Tag, "Exception caught while fetching faculty list form the excel sheet");

        }
        return facultyList;
    }   /*Returns the list fo facutlies in an arrayList | ArrayList<String>*/

    public String[] getLectureTime() {
        String[] lectureTime = new String[8];
        try {
            String contents = "";
            AssetManager assetManager = getAssets();
            assetManager = this.getAssets();
            InputStream inputStream = assetManager.open("file.xls"); //open the excel sheet
            Workbook workbook = Workbook.getWorkbook(inputStream);
            Sheet sheet = workbook.getSheet(0);
            int row = sheet.getRows();
            int col = sheet.getColumns();
            Log.d(Tag, "Logging lecture time slot list for the function: ->> getLectureTime() in Adjustment.java");
            for (int i = 1; i <= 8; i++) {
                contents = sheet.getCell(1, i).getContents();
                contents = purify(contents);
                Log.d(Tag, "i: " + i + " content: " + contents);
                lectureTime[i - 1] = contents; //add the fetched data to the array list | lecture timeslots
            }
        } catch (Exception exception) {
            Toast.makeText(this, "Exception while fetching timeslots list from teh excel sheet" + exception, Toast.LENGTH_SHORT).show();
            Log.d(Tag, "Exception caught while fetching timeslot list form the excel sheet" + exception);
        }
        return lectureTime;
    } /*Fetches timeslots of lecture from excel sheet | Timeslots lecture*/

    public int[] getPriorityList() {
        sd = openOrCreateDatabase("minor_project", Context.MODE_PRIVATE, null);
        Cursor cursor = sd.rawQuery("select priority from timetable", null);
        int cursor_count = cursor.getCount();
        Log.d(Tag, "Cursor count priority List: " + cursor_count);
        int[] priorityList = new int[cursor_count];
        int iterator = 1;
        cursor.moveToFirst();
        while (iterator <= cursor_count) {
            priorityList[iterator - 1] = cursor.getInt(0);
            Log.d(Tag, "priority iteration " + iterator + ": " + priorityList[iterator - 1]);
            iterator += 1;
            cursor.moveToNext();
        }

        return priorityList;
    }   /*Returns a priority list for all the faculties*/

    public int getDistinct() {
        sd = openOrCreateDatabase("minor_project", Context.MODE_PRIVATE, null);
        Cursor cursor = sd.rawQuery("select distinct priority from timetable", null);
        int cursor_count = cursor.getCount();
        sd.close();
        Toast.makeText(this, "distinct priorities: " + cursor_count, Toast.LENGTH_SHORT).show();
        Log.d(Tag, "distinct priorities count: " + cursor_count + " requested by: getDistinct() at Adjustment.class");
        return cursor_count;
    }   /*Returns number of distinct priorities*/

    public int getFacultyCount() {
        sd = openOrCreateDatabase("minor_project", Context.MODE_PRIVATE, null);
        Cursor cursor = sd.rawQuery("select distinct faculty from timetable", null);
        int cursor_count = cursor.getCount();
        sd.close();
        Log.d(Tag, "Cursor count: " + cursor_count);
        return cursor_count;
    } /*Returns distinct faculty count*/

    public String purify(String time) {
        time = time.replaceAll(":", "");
        time = time.replaceAll("-", "_");
        time = time.replace(" ", "");
        time = "ts_" + time;
        return time;
    } /*Purifies time format to fit for database norms*/

    public void adjust() {
        int[] priority = new int[getFacultyCount()];
        priority = getPriorityList();
    }
}
