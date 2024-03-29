package edu.cnm.deepdive.nasaapod.controller;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.cnm.deepdive.nasaapod.ApodApplication;
import edu.cnm.deepdive.nasaapod.BuildConfig;
import edu.cnm.deepdive.nasaapod.R;
import edu.cnm.deepdive.nasaapod.controller.DateTimePickerFragment.Mode;
import edu.cnm.deepdive.nasaapod.model.Apod;
import edu.cnm.deepdive.nasaapod.model.ApodDB;
import edu.cnm.deepdive.nasaapod.service.ApodService;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    boolean handled = true;
    switch (item.getItemId()) {
      default:
        handled = super.onOptionsItemSelected(item);
      case R.id.sign_out:
        signOut();
        break;
    }
    return handled;
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
      calendar
          .setTimeInMillis(savedInstanceState.getLong(CALENDAR_KEY, calendar.getTimeInMillis()));
      apod = (Apod) savedInstanceState.getSerializable(APOD_KEY);
    }
    if (apod != null) {
      progressSpinner.setVisibility(View.VISIBLE);
      webView.loadUrl(apod.getUrl());
    } else {
      loadApod();
    }
  }

  private void signOut() {
    ApodApplication app = ApodApplication.getInstance();
    app.getClient().signOut().addOnCompleteListener(this, (task) -> {
      app.setAccount(null);
      Intent intent = new Intent(this, LoginActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
    });
  }

  private void pickDate() {
    DateTimePickerFragment picker = new DateTimePickerFragment();
    picker.setMode(Mode.DATE);
    picker.setCalendar(calendar);
    picker.setListener((cal) -> loadApod(cal.getTime()));
    picker.show(getSupportFragmentManager(), picker.getClass().getSimpleName());
  }

  private void loadApod() {
    loadApod(calendar.getTime());
  }

  private void loadApod(Date date) {
    new QueryApod().execute(date);
  }

  // TODO Consider a finalizeApod method, that updates UI & database.

  private class QueryApod extends AsyncTask<Date, Void, Apod> {

    private Date date;

    @Override
    protected void onPreExecute() {
      progressSpinner.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(Apod apod) {
      MainActivity.this.apod = apod;
      // TODO Load from internal storage, if possible.
      webView.loadUrl(apod.getUrl());
    }

    @Override
    protected Apod doInBackground(Date... dates) {
      Date date = new Date(dates[0].getYear(), dates[0].getMonth(), dates[0].getDate());
      List<Apod> apods = ApodDB.getInstance(MainActivity.this).getApodDao().find(date);
      if (apods.size() > 0) {
        calendar.setTime(date);
        return apods.get(0);
      }
      new ApodTask().execute(date);
      cancel(true);
      return null;
    }
  }

  private class ApodTask extends AsyncTask<Date, Void, Apod> {

    private Date date;

    @Override
    protected void onPreExecute() {
      progressSpinner.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(Apod apod) {
      MainActivity.this.apod = apod;
      // TODO Handle hdUrl.
      webView.loadUrl(apod.getUrl());
    }

    @Override
    protected void onCancelled(Apod apod) {
      progressSpinner.setVisibility(View.GONE);
      Toast.makeText(MainActivity.this, R.string.error_message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected Apod doInBackground(Date... dates) {
      Apod apod = null;
      try {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);
        date = (dates.length == 0) ? calendar.getTime() : dates[0];
        Response<Apod> response = service.get(apiKey, format.format(date)).execute();
        if (response.isSuccessful()) {
          apod = response.body();
          calendar.setTime(date);
          ApodDB.getInstance(MainActivity.this).getApodDao().insert(apod);
        }
      } catch (IOException e) {
        // Do nothing: apod is already null.
      } finally {
        if (apod == null) {
          cancel(true);
        }
      }
      return apod;
    }
  }

}
