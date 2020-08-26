package com.nachoverdon.mongolia.saleslayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.io.IOUtils;

@AllArgsConstructor
@Getter
public class SalesLayerExporter {
  private final SalesLayerConfig config;

  /**
   * Creates a GET request to the Sales Layer API.
   *
   * @return SalesLayerResponse The response from the API.
   */
  @Nullable
  public SalesLayerResponse fetchData() {
    HttpURLConnection con = null;
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
        .create();

    try {
      URL url = new URL(config.toString());
      con = (HttpURLConnection) url.openConnection();

      con.setRequestMethod("GET");
      con.setDoOutput(false);
      con.setRequestProperty("Content-Type", "application/json");
      // 1 minute timeout
      con.setConnectTimeout(60000);
      con.setReadTimeout(60000);

      String response = IOUtils.toString(con.getInputStream(), StandardCharsets.UTF_8);

      con.disconnect();

      return gson.fromJson(response, SalesLayerResponse.class);

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (con != null) {
        con.disconnect();
      }
    }

    return null;
  }

  static class BooleanTypeAdapter implements JsonDeserializer<Boolean> {
    public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

      return json.getAsInt() != 0;
    }
  }
}

