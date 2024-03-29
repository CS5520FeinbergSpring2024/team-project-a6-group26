package edu.northeastern.group26.littlemood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarDay;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class CalendarActivity extends AppCompatActivity {
    private ImageView addJournalButton;
    private ImageView searchIcon;
    private ImageView settingIcon;
    private CalendarView calendarView;
    private SearchView searchInput;
    private TextView quoteTextView;
    private Handler quoteHandler;
    private static final int SUCCESS = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        searchIcon = findViewById(R.id.searchIcon);
        calendarView = findViewById(R.id.calendarView);
        addJournalButton = findViewById(R.id.addJournalButton);
        settingIcon = findViewById(R.id.settingsIcon);
        quoteTextView = findViewById(R.id.inspiringWordsTextView);
        searchInput = findViewById(R.id.searchView);

        // get daily quote(inspiring words) from API and update UI
        getDailyQuote();

        // enter mood page
        addJournalButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, MoodActivity.class);
            startActivity(intent);
        });

        // enter setting page
        settingIcon.setOnClickListener(view -> {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        });

        // define search activity
        searchIcon = findViewById(R.id.searchIcon);
//        searchIcon.setOnClickListener(v -> showDatePicker());
        searchIcon.setOnClickListener(v -> showSearchedDate(searchInput));

        // show emojis under each date
        setDay(calendarView);

        calendarView.setOnCalendarDayClickListener(calendarDay -> {
            Intent intent = new Intent(CalendarActivity.this, MoodActivity.class);
            // Extract the year, month, and day from the clicked CalendarDay
            int year = calendarDay.getCalendar().get(Calendar.YEAR);
            // month is zero-based Indexing
            int month = calendarDay.getCalendar().get(Calendar.MONTH) + 1;
            int day = calendarDay.getCalendar().get(Calendar.DAY_OF_MONTH);
            // Put the year, month, and day as extras in the intent
            intent.putExtra("YEAR", year);
            intent.putExtra("MONTH", month);
            intent.putExtra("DAY", day);

            startActivity(intent);
        });
    }

    private void setDay(CalendarView calendarView){
        List<EventDay> events = new ArrayList<>();
        // todo: show emojis from firebase
        // Happy day
        Calendar happyDay = Calendar.getInstance();
        happyDay.set(2024, Calendar.MARCH, 15);
        events.add(new EventDay(happyDay, R.drawable.ic_smiling_face));

        // Sad day
        Calendar sadDay = Calendar.getInstance();
        sadDay.set(2024, Calendar.MARCH, 20);
        events.add(new EventDay(sadDay, R.drawable.ic_sad_face));

        calendarView.setEvents(events);
    }

    private void showSearchedDate(SearchView searchView){
        if (searchView.getVisibility() == View.GONE) {
            searchView.setVisibility(View.VISIBLE);
        } else {
            searchView.setVisibility(View.GONE);
        }
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                format.setLenient(false);
                try {
                    Date date = format.parse(query);
                    Calendar calendar = Calendar.getInstance();
                    if (date != null) {
                        calendar.setTime(date);
                        Toast.makeText(CalendarActivity.this, "Selected date: " + query, Toast.LENGTH_SHORT).show();
                        calendarView.setDate(date);
                        List<CalendarDay> events = new ArrayList<>();
                        CalendarDay searchedDay = new CalendarDay(calendar);
                        searchedDay.setLabelColor(R.color.baby_blue);
                        events.add(searchedDay);
                        calendarView.setCalendarDays(events);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    // Notify user about the wrong date format
                    Toast.makeText(CalendarActivity.this, "Invalid date format. Please use YYYY-MM-DD.", Toast.LENGTH_SHORT).show();
                }
                searchView.setVisibility(View.GONE);
                searchView.clearFocus();
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

//    private void showDatePicker() {
//        // Define the listener to handle date selection
//        OnSelectDateListener listener = calendar -> {
//            Calendar selectedDate = calendar.get(0);
//            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
//            String dateString = format.format(selectedDate.getTime());
//            Toast.makeText(CalendarActivity.this, "Selected Date: " + dateString, Toast.LENGTH_LONG).show();
//        };
//        // Build and show the date picker
//        DatePickerBuilder builder = new DatePickerBuilder(this, listener)
//                .pickerType(CalendarView.RANGE_PICKER);
//        DatePicker datePicker = builder.build();
//        datePicker.show();
//    }
    private void getDailyQuote(){
        quoteHandler = new Handler(message -> {
            if(message.what == SUCCESS){
                String responseData = message.getData().getString("data");
                try {
                    JSONArray jsonArray = new JSONArray(responseData);
                    if (!jsonArray.isNull(0)) {
                        JSONObject jsonObject = jsonArray.getJSONObject(0);
                        String quoteText = jsonObject.optString("q"); // Assuming "q" is the quote text
                        String author = jsonObject.optString("a"); // Assuming "a" is the author
                        String fullQuote = "\"" + quoteText + "\" - " + author;
                        // update UI with the quote
                        runOnUiThread(() -> quoteTextView.setText(fullQuote));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return false;
        });

        new Thread(() -> {
            String jsonData = askZenQuote();
            Log.d("jsonData：",jsonData);
            JSONObject jsonObject = null;
            Message message=Message.obtain();
            Bundle bundle=new Bundle();
            bundle.putString("data",jsonData);
            message.setData(bundle);
            message.what=SUCCESS;
            quoteHandler.sendMessage(message);
        }).start();

    }

    private String askZenQuote(){
        try{
            String url1 = "https://zenquotes.io/api/today"; // Get a quote for daily inspiration
            URL url = new URL(url1);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK){
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String strRead;
                while ((strRead = bufferedReader.readLine()) != null) {
                    stringBuilder.append(strRead).append("\r\n");
                }
                bufferedReader.close();
                inputStream.close();
                return stringBuilder.toString();
            }
        } catch (Exception e){
            e.printStackTrace();
            return e.toString();
        }
        return "Failed to fetch quote";
    }



    }