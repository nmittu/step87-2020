package com.google.sps.servlets.queue;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.common.annotations.VisibleForTesting;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.sps.authentication.Authenticator;
import com.google.sps.firebase.FirebaseAppManager;
import com.google.sps.tasks.TaskSchedulerFactory;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/end-help")
public class EndHelp extends HttpServlet {
  private FirebaseAuth authInstance;
  private DatastoreService datastore;
  private TaskSchedulerFactory taskSchedulerFactory;
  private Authenticator auth;

  @VisibleForTesting protected String QUEUE_NAME;

  @Override
  public void init(ServletConfig config) throws ServletException {
    QUEUE_NAME = System.getenv("WORKSPACE_QUEUE_ID");
    taskSchedulerFactory = TaskSchedulerFactory.getInstance();
    try {
      authInstance = FirebaseAuth.getInstance(FirebaseAppManager.getApp());
      auth = new Authenticator();
    } catch (IOException e) {
      throw new ServletException(e);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get the input from the form.
    // navigate to /_ah/admin to view Datastore

    datastore = DatastoreServiceFactory.getDatastoreService();

    try {
      String classCode = request.getParameter("classCode").trim();
      String taToken = request.getParameter("taToken");

      if (auth.verifyTaOrOwner(taToken, classCode)) {
        int retries = 10;
        while (true) {
          Transaction txn = datastore.beginTransaction();
          try {
            // Retrive class entity
            Key classKey = KeyFactory.stringToKey(classCode);
            Entity classEntity = datastore.get(txn, classKey);

            // Get studentID from studentEmail
            String studentEmail = request.getParameter("studentEmail");
            UserRecord userRecord = authInstance.getUserByEmail(studentEmail);
            String studentID = userRecord.getUid();

            // Update beingHelped
            EmbeddedEntity beingHelped = (EmbeddedEntity) classEntity.getProperty("beingHelped");

            EmbeddedEntity studentEntity = (EmbeddedEntity) beingHelped.getProperty(studentID);

            taskSchedulerFactory
                .create(QUEUE_NAME, "/tasks/deleteWorkspace")
                .schedule(
                    (String) studentEntity.getProperty("workspaceID"), TimeUnit.HOURS.toSeconds(1));

            beingHelped.removeProperty(studentID);

            classEntity.setProperty("beingHelped", beingHelped);
            datastore.put(txn, classEntity);

            txn.commit();
            break;
          } catch (ConcurrentModificationException e) {
            if (retries == 0) {
              throw e;
            }
            // Allow retry to occur
            --retries;
          } finally {
            if (txn.isActive()) {
              txn.rollback();
            }
          }
        }
      } else {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
      }
    } catch (EntityNotFoundException e) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
    } catch (IllegalArgumentException e) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    } catch (FirebaseAuthException e) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
  }
}
