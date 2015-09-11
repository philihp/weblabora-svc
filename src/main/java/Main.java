import java.util.*;
import static spark.Spark.*;
import com.google.gson.Gson;
import com.philihp.weblabora.model.*;

public class Main {

  public static void main(String[] args) {

    port(Integer.valueOf(System.getenv("PORT")));

    post("/", "application/json", (request, response) -> {
      Board board = new Board();
      return board;
    }, new JsonTransformer());

  }

}
