package edu.cnm.deepdive.nasaapod.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

@Entity(
    indices = @Index(value = "date", unique = true)
)
public class Apod implements Serializable {

  private static final long serialVersionUID = 2547946263420122184L;

  @ColumnInfo(name = "apod_id")
  @PrimaryKey(autoGenerate = true)
  private long id;

  @NonNull
  @Expose
  private Date date;

  @Expose
  private String title;

  @Expose
  private String explanation;

  @Expose
  private String copyright;

  @Expose
  private String url;

  @Expose
  @SerializedName("media_type")
  private String mediaType;

  @Expose
  @SerializedName("hdurl")
  private String hdUrl;

  @Expose
  @SerializedName("service_version")
  private String serviceVersion;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @NonNull
  public Date getDate() {
    return date;
  }

  public void setDate(@NonNull Date date) {
    this.date = date;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getExplanation() {
    return explanation;
  }

  public void setExplanation(String explanation) {
    this.explanation = explanation;
  }

  public String getCopyright() {
    return copyright;
  }

  public void setCopyright(String copyright) {
    this.copyright = copyright;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public String getHdUrl() {
    return hdUrl;
  }

  public void setHdUrl(String hdUrl) {
    this.hdUrl = hdUrl;
  }

  public String getServiceVersion() {
    return serviceVersion;
  }

  public void setServiceVersion(String serviceVersion) {
    this.serviceVersion = serviceVersion;
  }

}
