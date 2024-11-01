package com.example.weatherapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout homeRl;
    private ImageView iconIv, searchIv;
    private TextView cityNameTv, temperature, conditionTv;
    private TextInputEditText cityNameEt;
    private RecyclerView weatherRv;
    private ProgressBar progressBar;
    private ArrayList<WeatherRVModel> weatherRVModelArrayList;
    private WeatherRvAdapter weatherRvAdapter;

    private static final int PERMISSION_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;

    String apikey = BuildConfig.API_KEY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Make the layout full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        // Initialize views
        homeRl = findViewById(R.id.idHomeRl);
        iconIv = findViewById(R.id.idImagecloud);
        searchIv = findViewById(R.id.idIvSearch);
        cityNameTv = findViewById(R.id.idTvCityName);
        temperature = findViewById(R.id.idTvTemp);
        conditionTv = findViewById(R.id.idCondition);
        cityNameEt = findViewById(R.id.idTvInput);
        weatherRv = findViewById(R.id.idRvWeather);
        progressBar = findViewById(R.id.progressBar);


        // Setting up RecyclerView and Adapter
        weatherRVModelArrayList = new ArrayList<>();
        weatherRvAdapter = new WeatherRvAdapter(this, weatherRVModelArrayList);
        weatherRv.setAdapter(weatherRvAdapter);

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //check for location permission
        checkLocationPermission();

        //Set up search button to handle user input for city name
        searchIv.setOnClickListener(view -> {
            String enteredCityName = cityNameEt.getText().toString().trim();
            if (enteredCityName.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter a valid city name.", Toast.LENGTH_SHORT).show();
            } else {
                cityNameTv.setText(enteredCityName);
                cityNameEt.setText("");
                getWeatherInfo(enteredCityName);
            }
        });
    }

    // Method to check location permission
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        } else {
            getLastLocation();
        }
    }

    // fetch last known location
    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            progressBar.setVisibility(View.GONE);
                            homeRl.setVisibility(View.VISIBLE);

                            if (location != null) {
                                double longitude = location.getLongitude();
                                double latitude = location.getLatitude();
                                getWeatherInfo(latitude,longitude);
                            } else {
                                Toast.makeText(MainActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }
    }

    // Handle result of permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Please provide permission..", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //fetch  weather info using latitude and longitude
    private void getWeatherInfo(double latitude, double longitude) {
        String url = "http://api.weatherapi.com/v1/forecast.json?key="+apikey+"&q=" + latitude + "," + longitude + "&days=1&aqi=yes&alerts=yes";
        fetchWeatherData(url);
    }

    //fetch  weather info using cityName
    private void getWeatherInfo(String cityName) {
        String url = "http://api.weatherapi.com/v1/forecast.json?key="+apikey+"&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        fetchWeatherData(url);
    }

    // Fetch weather information using WeatherAPI
    private void fetchWeatherData(String url) {
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                weatherRVModelArrayList.clear();
                try {

                    //parse and set city name
                    String cityName = response.getJSONObject("location").getString("name");
                    cityNameTv.setText(cityName);

                    // Set current temperature and weather conditions
                    String temp = response.getJSONObject("current").getString("temp_c");
                    temperature.setText(temp +"°C");

                    String conditionText = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    conditionTv.setText(conditionText);

                    //load condition icon using picasso library
                    String icon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                    String iconUrl = "https:" + icon;
                    Picasso.get().load(iconUrl).error(R.drawable.error_place).into(iconIv);

                    // fetch hourly weather forecast
                    JSONArray hourArray = response.getJSONObject("forecast").getJSONArray("forecastday").getJSONObject(0).getJSONArray("hour");
                    for (int i = 0; i < hourArray.length(); i++) {
                        JSONObject hourObject = hourArray.getJSONObject(i);
                        String time = hourObject.getString("time");
                        String tempC = hourObject.getString("temp_c");
                        String conditionIcon = hourObject.getJSONObject("condition").getString("icon");
                        String url = "https:" + conditionIcon;
                        String windKph = hourObject.getString("wind_kph");

                        // Add weather data to the RecyclerView list
                        weatherRVModelArrayList.add(new WeatherRVModel(time, tempC, url, windKph));
                    }
                    weatherRvAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "Error fetching data";
                if (error.networkResponse != null&& error.networkResponse.data!=null) {
                    int statusCode = error.networkResponse.statusCode;
                    errorMessage += " - Status Code: " + statusCode;
                    String responseBody = new String(error.networkResponse.data);
                    errorMessage += " - Response: " + responseBody;
                }
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
        //add request to queue
        requestQueue.add(jsonObjectRequest);
    }
}
