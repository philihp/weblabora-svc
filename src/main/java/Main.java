import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.util.concurrent.TimeUnit.*;

import static spark.Spark.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.philihp.weblabora.model.*;
import org.apache.commons.codec.binary.Hex;

public class Main {

  private static ObjectMapper objectMapper = new ObjectMapper();

  private static Cache<String, String> cache =
      CacheBuilder.newBuilder()
          .maximumSize(100000)
          .expireAfterAccess(7, DAYS)
          .build();

  public static void main(String[] args) {

    if(System.getenv("PORT") == null) {
      port(5000);
    }
    else {
      port(Integer.valueOf(System.getenv("PORT")));
    }

    get("/:hash", "application/json", (request, response) -> {
      String hash = request.params(":hash");
      String value = cache.getIfPresent(hash);
      if(value == null) {
        response.status(404);
        return objectMapper.writeValueAsString(new Exception("Forgot cached board state. Please re-post."));
      }
      else {
        response.header("Cache-Control", "max-age=86400");
        return value;
      }
    });

    get("/", (request, response) ->
      "<html>" +
        "<head>" +
        "</head>" +
        "<body>" +
          "<form action=\"#\" method=\"POST\">" +
          "<textarea cols=\"80\" rows=\"40\" name=\"actions\"></textarea>" +
          "<input type=\"submit\" />" +
          "</form>" +
        "</body>" +
      "</html>"
    );

    post("/", "application/json", (request, response) -> {
      String body = request.queryParams("actions");
      if(body == null) body = request.body();
      if(body == null) body = "";
      final String finalBody = body;

      String hash = hash(finalBody);
      String value = cache.get(hash, () -> {
        try {
          Board board = new Board();
          String[] tokens = finalBody.split("[\r\n]+");
          for (String token : tokens) {
            MoveProcessor.processMove(board, token);
          }
          return objectMapper.valueToTree(board).toString();
        }
        catch(Exception e) {
          response.status(400);
          return objectMapper.writeValueAsString(e);
        }
      });
      response.redirect("/" + hash, 303); // very important to use 303
      return value;
    });

    options("/*", (request, response) -> "OK");

    before((request, response) -> {
      String origin = request.headers("Origin");
      if (origin != null) {
        response.header("Access-Control-Allow-Origin", origin);
      }
      String headers = request.headers("Access-Control-Request-Headers");
      if (headers != null) {
        response.header("Access-Control-Allow-Headers", headers);
      }
      String method = request.headers("Access-Control-Request-Method");
      if (method != null) {
        response.header("Access-Control-Allow-Methods", method);
      }
    });

  }

  private static String hash(String contents) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
      messageDigest.update(contents.getBytes());
      return new String(Hex.encodeHex(messageDigest.digest())).substring(0, 8);
    } catch (NoSuchAlgorithmException e) {
      return Integer.toString(contents.hashCode());
    }
  }


}
