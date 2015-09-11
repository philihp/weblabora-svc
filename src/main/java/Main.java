import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

import static spark.Spark.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.philihp.weblabora.model.*;
import org.apache.commons.codec.binary.Hex;

public class Main {

  private static ObjectMapper objectMapper = new ObjectMapper();

  private static ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

  public static void main(String[] args) {

    port(Integer.valueOf(System.getenv("PORT")));

    get("/:hash", "application/json", (request, response) -> {
      String hash = request.params(":hash");
      return cache.get(hash);
    });

    put("/", "application/json", (request, response) -> {
      String hash = hash(request.body());
      if(!cache.contains(hash)) {
        Board board = new Board();
        String[] tokens = request.body().split("\n+");
        for (String token : tokens) {
          MoveProcessor.processMove(board, token);
        }
        cache.put(hash, objectMapper.valueToTree(board).toString());
      }
      response.redirect("/" + hash, 303);
      return null;
    });

  }

  private static String hash(String contents) {
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
      messageDigest.update(contents.getBytes());
      return new String(Hex.encodeHex(messageDigest.digest())).substring(0,6);
    } catch (NoSuchAlgorithmException e) {
      return Integer.toString(contents.hashCode());
    }
  }

}
