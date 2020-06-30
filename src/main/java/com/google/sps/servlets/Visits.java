package com.google.sps;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.sps.firebase.FirebaseAppManager;
import java.io.IOException;
import java.util.ArrayList;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Retrieve the number of visits per class from Visit entity and send to chart
@WebServlet("/visits")
public class Visits extends HttpServlet {

  FirebaseAuth authInstance;

  // Get the current session
  @Override
  public void init(ServletConfig config) throws ServletException {
    try {
      authInstance = FirebaseAuth.getInstance(FirebaseAppManager.getApp());
    } catch (IOException e) {
      throw new ServletException(e);
    }
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    ArrayList<String> listOfClassNames = new ArrayList<String>();
    ArrayList<Long> visitsPerClass = new ArrayList<Long>();

    // Obtain visits from datastore and filter them into results query
    Query query = new Query("Visit");
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Store the class name and number of visits into two separate lists
    for (Entity entity : results.asIterable()) {
      String className = (String) entity.getProperty("className");
      long classVisits = (long) entity.getProperty("numVisits");

      listOfClassNames.add(className);
      visitsPerClass.add(classVisits);
    }

    // Send both class names list and visits to bar chart function
    VisitParent parent = new VisitParent(listOfClassNames, visitsPerClass);
    Gson gson = new Gson();
    String json = gson.toJson(parent);
    response.getWriter().println(json);
  }
}