package com.google.sps.servlets.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.gson.Gson;
import com.google.sps.models.UserData;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetUserDataTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private DatastoreService datastore;

  @Mock HttpServletRequest httpRequest;

  @Mock HttpServletResponse httpResponse;

  @Mock FirebaseAuth authInstance;

  @InjectMocks GetUserData getUserData;

  @Before
  public void setUp() {
    helper.setUp();
    datastore = DatastoreServiceFactory.getDatastoreService();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void newUser() throws Exception {
    Entity initClass = new Entity("Class");
    initClass.setProperty("name", "testClass");
    initClass.setProperty("beingHelped", new EmbeddedEntity());
    initClass.setProperty("studentQueue", Collections.emptyList());

    datastore.put(initClass);

    when(httpRequest.getParameter("idToken")).thenReturn("uID");
    FirebaseToken mockToken = mock(FirebaseToken.class);
    when(authInstance.verifyIdToken("uID")).thenReturn(mockToken);
    when(mockToken.getUid()).thenReturn("uID");
    when(mockToken.getEmail()).thenReturn("user@google.com");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(httpResponse.getWriter()).thenReturn(writer);

    getUserData.doGet(httpRequest, httpResponse);

    assertEquals(new Gson().toJson(Collections.emptyList()), stringWriter.toString());

    Entity testUserEntity = datastore.prepare(new Query("User")).asSingleEntity();
    assertEquals(testUserEntity.getProperty("userEmail"), "user@google.com");
  }

  @Test
  public void existingUser() throws Exception {
    Entity initClass = new Entity("Class");
    initClass.setProperty("name", "testClass");
    initClass.setProperty("beingHelped", new EmbeddedEntity());
    initClass.setProperty("studentQueue", Collections.emptyList());

    datastore.put(initClass);

    Entity initUser = new Entity("User");

    initUser.setProperty("userEmail", "user@google.com");
    initUser.setProperty("registeredClasses", Collections.emptyList());
    initUser.setProperty("ownedClasses", Collections.emptyList());
    initUser.setProperty("taClasses", Arrays.asList(initClass.getKey()));

    datastore.put(initUser);

    when(httpRequest.getParameter("idToken")).thenReturn("uID");
    FirebaseToken mockToken = mock(FirebaseToken.class);
    when(authInstance.verifyIdToken("uID")).thenReturn(mockToken);
    when(mockToken.getUid()).thenReturn("uID");
    when(mockToken.getEmail()).thenReturn("user@google.com");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(httpResponse.getWriter()).thenReturn(writer);

    getUserData.doGet(httpRequest, httpResponse);

    assertEquals(
        new Gson()
            .toJson(
                Arrays.asList(
                    new UserData(
                        KeyFactory.keyToString(initClass.getKey()), "testClass", "taClasses"))),
        stringWriter.toString());

    Entity testUserEntity = datastore.prepare(new Query("User")).asSingleEntity();
    assertTrue(datastore.prepare(new Query("User")).countEntities() == 1);
    assertEquals(testUserEntity.getProperty("userEmail"), "user@google.com");
  }

  @Test
  public void existingUserInQueue() throws Exception {
    Entity initClass = new Entity("Class");
    initClass.setProperty("name", "testClass");
    initClass.setProperty("beingHelped", new EmbeddedEntity());

    EmbeddedEntity inQueue = new EmbeddedEntity();
    inQueue.setProperty("uID", "uID");

    initClass.setProperty("studentQueue", Arrays.asList(inQueue));

    datastore.put(initClass);

    Entity initUser = new Entity("User");

    initUser.setProperty("userEmail", "user@google.com");
    initUser.setProperty("registeredClasses", Arrays.asList(initClass.getKey()));
    initUser.setProperty("ownedClasses", Collections.emptyList());
    initUser.setProperty("taClasses", Collections.emptyList());

    datastore.put(initUser);

    when(httpRequest.getParameter("idToken")).thenReturn("uID");
    FirebaseToken mockToken = mock(FirebaseToken.class);
    when(authInstance.verifyIdToken("uID")).thenReturn(mockToken);
    when(mockToken.getUid()).thenReturn("uID");
    when(mockToken.getEmail()).thenReturn("user@google.com");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(httpResponse.getWriter()).thenReturn(writer);

    getUserData.doGet(httpRequest, httpResponse);

    assertEquals(
        new Gson()
            .toJson(
                Arrays.asList(
                    new UserData(
                        KeyFactory.keyToString(initClass.getKey()),
                        "testClass",
                        "registeredClasses",
                        true))),
        stringWriter.toString());

    Entity testUserEntity = datastore.prepare(new Query("User")).asSingleEntity();
    assertTrue(datastore.prepare(new Query("User")).countEntities() == 1);
    assertEquals(testUserEntity.getProperty("userEmail"), "user@google.com");
  }

  @Test
  public void existingUserBeingHelped() throws Exception {
    Entity initClass = new Entity("Class");
    initClass.setProperty("name", "testClass");
    EmbeddedEntity beingHelped = new EmbeddedEntity();
    beingHelped.setProperty("uID", new EmbeddedEntity());
    initClass.setProperty("beingHelped", beingHelped);

    initClass.setProperty("studentQueue", Collections.emptyList());

    datastore.put(initClass);

    Entity initUser = new Entity("User");

    initUser.setProperty("userEmail", "user@google.com");
    initUser.setProperty("registeredClasses", Arrays.asList(initClass.getKey()));
    initUser.setProperty("ownedClasses", Collections.emptyList());
    initUser.setProperty("taClasses", Collections.emptyList());

    datastore.put(initUser);

    when(httpRequest.getParameter("idToken")).thenReturn("uID");
    FirebaseToken mockToken = mock(FirebaseToken.class);
    when(authInstance.verifyIdToken("uID")).thenReturn(mockToken);
    when(mockToken.getUid()).thenReturn("uID");
    when(mockToken.getEmail()).thenReturn("user@google.com");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(httpResponse.getWriter()).thenReturn(writer);

    getUserData.doGet(httpRequest, httpResponse);

    assertEquals(
        new Gson()
            .toJson(
                Arrays.asList(
                    new UserData(
                        KeyFactory.keyToString(initClass.getKey()),
                        "testClass",
                        "registeredClasses",
                        true))),
        stringWriter.toString());

    Entity testUserEntity = datastore.prepare(new Query("User")).asSingleEntity();
    assertTrue(datastore.prepare(new Query("User")).countEntities() == 1);
    assertEquals(testUserEntity.getProperty("userEmail"), "user@google.com");
  }
}
