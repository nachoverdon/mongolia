package com.nachoverdon.mongolia.saleslayer;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class SalesLayerResponse {
  public static final String STATUS = "STATUS";
  public static final String ID = "ID";
  public static final String MODIFIED = "M";
  public static final String DELETED = "D";
  public static final String UNMODIFIED = "U";

  private String version;
  private int time;
  private String action;
  private int error;
  @SerializedName(value = "error_message")
  private String errorMessage;
  private SalesLayerSchema schema;
  @SerializedName(value = "data_schema")
  private Object dataSchema;
  @SerializedName(value = "data_schema_info")
  private Object dataSchemaInfo;
  private Object data;
  @SerializedName(value = "image_packs")
  private Object imagePacks;
  @SerializedName(value = "offline_files")
  private Object offlineFiles;
  @SerializedName(value = "page_count")
  private int pageCount;
  @SerializedName(value = "page_length")
  private int pageLength;
  @SerializedName(value = "next_page")
  private String nextPage;
}
