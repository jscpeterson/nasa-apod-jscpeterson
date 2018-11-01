package edu.cnm.deepdive.nasaapod.controller;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.cnm.deepdive.nasaapod.BuildConfig;
import edu.cnm.deepdive.nasaapod.R;
import edu.cnm.deepdive.nasaapod.controller.DateTimePickerFragment.Mode;
import edu.cnm.deepdive.nasaapod.model.Apod;
import edu.cnm.deepdive.nasaapod.service.ApodService;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

  private static final String DATE_FORMAT = "yyyy-MM-dd";
  private static final String CALENDAR_KEY = "calendar";
  private static final String APOD_KEY = "apod";

  private WebView webView;
  private String apiKey;
  private ProgressBar progressSpinner;
  private FloatingActionButton jumpDate;
  private Calendar calendar;
  private ApodService service;
  private Apod apod;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    setupWebView();
    setupService();
    setupUI();
    setupDefaults(savedInstanceState);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putLong(CALENDAR_KEY, calendar.getTimeInMillis());
    outState.putSerializable(APOD_KEY, apod);
  }

  private void setupWebView() {
    webView = findViewById(R.id.web_view);
    webView.setWebViewClient(new WebViewClient() {

      @Override
      public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return false;
      }
      @Override
      public void onPageFinished(WebView view, String url) {
        progressSpinner.setVisibility(View.GONE);
        if (apod != null) {
          Toast.makeText(MainActivity.this, apod.getTitle(), Toast.LENGTH_LONG).show();
        }
      }
    });
    WebSettings settings = webView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setSupportZoom(true);
    settings.setBuiltInZoomControls(true);
    settings.setDisplayZoomControls(false);
    settings.setUseWideViewPort(true);
    settings.setLoadWithOverviewMode(true);
  }

  private void setupUI() {
    progressSpinner = findViewById(R.id.progress_spinner);
    progressSpinner.setVisibility(View.GONE);
    jumpDate = findViewById(R.id.jump_date);
    jumpDate.setOnClickListener((v) -> pickDate());
  }

  private void setupService() {
    Gson gson = new GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .setDateFormat(DATE_FORMAT)
        .create();
    Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(getString(R.string.base_url))
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build();
    service = retrofit.create(ApodService.class);
    apiKey = BuildConfig.API_KEY;
  }

  private void setupDefaults(Bundle savedInstanceState) {
    calendar = Calendar.getInstance();
    if (savedInstanceState != null) {
      calendar.setTimeInMillis(savedInstanceState.getLong(CALENDAR_KEY, calendar.getTimeInMillis()));
      apod = (Apod) savedInstanceState.getSerializable(APOD_KEY);
    }
    if (apod != null) {
      progressSpinner.setVisibility(View.VISIBLE);
      webView.loadUrl(apod.getUrl());
    } else {
      loadApod(calendar.getTime());
    }
  }

  private void pickDate() {
    DateTimePickerFragment picker = new DateTimePickerFragment();
    picker.setMode(Mode.DATE);
    picker.setCalendar(calendar);
    picker.setListener((cal) -> loadApod(cal.getTime()));
    picker.show(getSupportFragmentManager(), picker.getClass().getSimpleName());
  }

  private void loadApod(Date date) {
    DateFormat format = new SimpleDateFormat(DATE_FORMAT);
    progressSpinner.setVisibility(View.VISIBLE);
    service.get(apiKey, format.format(date)).enqueue(new ApodCallback(date));
  }

  private class ApodCallback implements Callback<Apod> {

    private Date date;

    public ApodCallback(Date date) {
      this.date = date;
    }

    @Override
    public void onResponse(Call<Apod> call, Response<Apod> response) {
      if (response.isSuccessful()) {
        Apod apod = response.body();
        MainActivity.this.apod = apod;
        calendar.setTime(date);
        webView.loadUrl(apod.getUrl());
      } else {
        progressSpinner.setVisibility(View.GONE);
      }
    }

    @Override
    public void onFailure(Call<Apod> call, Throwable throwable) {
      progressSpinner.setVisibility(View.GONE);
      Toast.makeText(MainActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
    }

  }

}
