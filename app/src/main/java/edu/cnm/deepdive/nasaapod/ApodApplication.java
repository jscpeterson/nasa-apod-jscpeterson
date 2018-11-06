package edu.cnm.deepdive.nasaapod;

import android.app.Application;
import com.facebook.stetho.Stetho;

public class ApodApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    Stetho.initializeWithDefaults(this);
  }

}
