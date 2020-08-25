package com.nachoverdon.mongolia.saleslayer;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;
import lombok.Data;

@Data
public class SalesLayerSchema {
  public String[] languages;
  @SerializedName(value = "correct_language")
  public String correctLanguage;
  @SerializedName(value = "offline_mode")
  public boolean offlineMode;
  @SerializedName(value = "default_language")
  public String defaultLanguage;
  @SerializedName(value = "connector_type")
  public String connectorType;
  @SerializedName(value = "connector_ID")
  public String connectorID;
  @SerializedName(value = "company_ID")
  public String companyID;
  @SerializedName(value = "company_name")
  public String companyName;
  @SerializedName(value = "sanitized_table_names")
  public LinkedTreeMap<String, String> sanitizedTableNames;
  @SerializedName(value = "language_table_names")
  public LinkedTreeMap<String, LinkedTreeMap<String, String>> languageTableNames;
}
