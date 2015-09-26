import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

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
          .expireAfterAccess(7, TimeUnit.DAYS)
          .build();

  public static void main(String[] args) {

    port(Integer.valueOf(System.getenv("PORT")));

    get("/:hash", "application/json", (request, response) -> {
      String hash = request.params(":hash");
      return cache.get(hash, () -> {
        response.status(404);
        return "{\"error\":\"Forgot state of board. Please retry.\"}";
      });
    });

    get("/", "application/json", (request, response) -> {;
      return Boolean.FALSE;
    });

    put("/", "application/json", (request, response) -> {
      String hash = hash(request.body());
      Board board = new Board();
      String[] tokens = request.body().split("\n+");
      for (String token : tokens) {
        MoveProcessor.processMove(board, token);
      }
      cache.put(hash, objectMapper.valueToTree(board).toString());
      response.redirect("/" + hash, 303); // very important to use 303
      return null;
    });

    options("/*", (request, response) -> {
      return "OK";
    });

    before((request,response)-> {
      String origin = request.headers("Origin");
      if(origin != null) {
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
