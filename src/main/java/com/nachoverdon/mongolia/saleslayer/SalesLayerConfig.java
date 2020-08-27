package com.nachoverdon.mongolia.saleslayer;

import com.nachoverdon.mongolia.utils.TimeUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.BooleanUtils;

@Data
public class SalesLayerConfig {
  public static final String SL_API = "https://api.saleslayer.com/";

  /**
   * The Connector Relationship Code.
   */
  private String code;
  /**
   * The secret key of the connector.
   */
  private String secretKey;
  /**
   * timestamp() (International POSIX datel).
   */
  private long time;
  /**
   * Random security number.
   */
  private int unique;
  /**
   * SHA256 code with the combination of 'code' + 'secret_key' + 'time' + 'unique'.
   */
  private String key256;
  /**
   * The timestamp from the last update we want to fetch.
   */
  private long lastUpdate;
  /**
   * Version of the SL API.
   * @see <a href="https://support.saleslayer.com/api/versioning-sales-layer-api">SL API</a>
   */
  private String ver = "1.18";
  /**
   * Indicates that it is only necessary to verify the connection and authentication.
   */
  private boolean test = false;
  /**
   * If set to true, the API will return the multi-category products grouped together.
   * @see <a href="https://support.saleslayer.com/api/other-modeling-parameters">SL API</a>
   */
  private boolean groupCategoryId = true;
  /**
   * Number of items per page (0 = all).
   */
  private int pagination = 0;
  /**
   * Indicates whether the response should be compressed or not.
   */
  private boolean compression = true;

  /**
   * Sales Layer configuration mandatory identification.
   *
   * @param code The Connector Relationship Code.
   * @param secretKey The secret key of the connector.
   * @param lastUpdate The timestamp from the last update we want to fetch.
   */
  public SalesLayerConfig(String code, String secretKey, long lastUpdate) {
    this.code = code;
    this.secretKey = secretKey;
    this.lastUpdate = lastUpdate;

    generateKey();
  }

  /**
   * Generates the time, unique and key256 parameters.
   */
  public void generateKey() {
    this.time = TimeUtils.nowInMs();
    this.unique = Math.abs(new Random().nextInt());
    this.key256 = DigestUtils.sha256Hex(code + secretKey + time + unique);
  }

  /**
   * Whether the request is paginated or not.
   *
   * @return boolean
   */
  public boolean isPaginated() {
    return pagination > 0;
  }

  /**
   * Gets the object as a Map of strings.
   *
   * @return A Map of the parameters
   */
  public Map<String, String> getParameters() {
    return new HashMap<String, String>() {
      {
        put("code", code);
        put("time", String.valueOf(time));
        put("unique", String.valueOf(unique));
        put("key256", key256);
        put("last_update", String.valueOf(lastUpdate));
        put("ver", ver);
        put("pagination", String.valueOf(pagination));
        put("group_category_id", groupCategoryId ? "1" : "0");
        put("compression", compression ? "1" : "0");
        put("test", test ? "1" : "0");
      }
    };
  }

  @Override
  public String toString() {
    return SL_API
        + "?code=" + code
        + "&time=" + time
        + "&unique=" + unique
        + "&key256=" + key256
        + "&last_update=" + lastUpdate
        + "&ver=" + ver
        // + "&pagination=" + pagination
        + "&group_category_id=" + BooleanUtils.toInteger(groupCategoryId)
        + "&compression=" + BooleanUtils.toInteger(compression)
        + "&test=" + BooleanUtils.toInteger(test);
  }
}
