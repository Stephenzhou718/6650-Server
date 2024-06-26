package Skier;

import com.google.gson.Gson;
import infra.RabbitMQService;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SkierServlet extends HttpServlet {

  private RabbitMQService rabbitMQService;

  @Override
  public void init() throws ServletException {
    super.init();
    try {
      this.rabbitMQService = new RabbitMQService("54.245.21.195");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("text/plain");
    String urlPath = req.getPathInfo();
    String[] urlParts = urlPath.split("/");

    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
    } else {
      res.setStatus(HttpServletResponse.SC_OK);
      // do any sophisticated processing with urlParts which contains all the url params
      res.getWriter().write("It works!");
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();

    // check we have a URL!
    String[] urlParts = urlPath.split("/");
    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("missing paramterers");
      return;
    }
    if (urlParts.length != 8) {
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
      return;
    }

    try {
      int resortID = Integer.parseInt(urlParts[1]);
      int seasonID = Integer.parseInt(urlParts[3]);
      int dayID = Integer.parseInt(urlParts[5]);
      int skierID = Integer.parseInt(urlParts[7]);

      // Proceed with processing the request using the extracted parameters
      processSkierPostRequest(resortID, seasonID, dayID, skierID, req, res);
    } catch (NumberFormatException e) {
      res.sendError(HttpServletResponse.SC_BAD_REQUEST, "URL parameters should be integers");
    }
    res.setStatus(HttpServletResponse.SC_CREATED);
    }

  private void processSkierPostRequest(int resortID, int seasonID, int dayID, int skierID, HttpServletRequest req, HttpServletResponse res)
      throws IOException {
    Gson gson = new Gson();
    SkiersLogResponse skiersLogResponse = new SkiersLogResponse();

    try {
      // parse parameters
      StringBuilder sb = new StringBuilder();
      String s = "";
      while ((s = req.getReader().readLine()) != null) {
        sb.append(s);
      }
      SkiersLog skiersLog = (SkiersLog) gson.fromJson(sb.toString(), SkiersLog.class);
      skiersLog.setResortID(resortID);
      skiersLog.setSeasonID(seasonID);
      skiersLog.setDayID(dayID);
      skiersLog.setSkierID(skierID);
      System.out.println("write one log: " +  gson.toJson(skiersLog));
      skiersLogResponse.setMessage("You have request the post api of Skiers successfully!");

      // send log
      rabbitMQService.sendMessage("skiers-data", gson.toJson(skiersLog));
    } catch (Exception ex) {
      ex.printStackTrace();
      skiersLogResponse.setMessage(ex.getMessage());
    }

    // response
    res.getOutputStream().print(gson.toJson(skiersLogResponse));
    res.getOutputStream().flush();
  }

  private boolean isUrlValid(String[] urlPath) {
    if (urlPath == null || urlPath.length == 0) {
      return false;
    }
    return true;
  }
}
