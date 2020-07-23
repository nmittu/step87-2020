package com.google.sps.servlets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.gson.Gson;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.sps.queue.StudentStatus;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
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
public class CheckStudentStatusTest {

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

  private DatastoreService datastore;

  @Mock HttpServletRequest httpRequest;

  @Mock HttpServletResponse httpResponse;

  @Mock FirebaseAuth authInstance;

  @InjectMocks CheckStudentStatus queue;

  private String WORKSPACE_ID = "WORKSPACE_ID";

  private static final LocalDate LOCAL_DATE = LocalDate.of(2020, 07, 06);
  private static final Date DATE =
      Date.from(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant());

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
  public void firstInQueue() throws Exception {
    Entity init = new Entity("Class");

    init.setProperty("owner", "ownerID");
    init.setProperty("name", "testClass");

    EmbeddedEntity addQueue1 = new EmbeddedEntity();
    EmbeddedEntity studentInfo1 = new EmbeddedEntity();
    studentInfo1.setProperty("timeEntered", DATE);
    studentInfo1.setProperty("workspaceID", WORKSPACE_ID);
    addQueue1.setProperty("uID", studentInfo1);

    init.setProperty("studentQueue", Arrays.asList(addQueue1));

    datastore.put(init);

    when(httpRequest.getParameter("classCode")).thenReturn(KeyFactory.keyToString(init.getKey()));
    when(httpRequest.getParameter("studentToken")).thenReturn("testID");

    FirebaseToken mockToken = mock(FirebaseToken.class);
    when(authInstance.verifyIdToken("testID")).thenReturn(mockToken);
    when(mockToken.getUid()).thenReturn("uID");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(httpResponse.getWriter()).thenReturn(writer);

    queue.doGet(httpRequest, httpResponse);

    Gson gson = new Gson();

    assertEquals(stringWriter.toString(), gson.toJson(new StudentStatus(1, WORKSPACE_ID)));
  }

  @Test
  public void midQueue() throws Exception {
    Entity init = new Entity("Class");

    init.setProperty("owner", "ownerID");
    init.setProperty("name", "testClass");
    init.setProperty("beingHelped", "");

    EmbeddedEntity addQueue1 = new EmbeddedEntity();
    EmbeddedEntity studentInfo1 = new EmbeddedEntity();
    studentInfo1.setProperty("timeEntered", DATE);
    studentInfo1.setProperty("workspaceID", WORKSPACE_ID);
    addQueue1.setProperty("test1", studentInfo1);

    EmbeddedEntity addQueue2 = new EmbeddedEntity();
    EmbeddedEntity studentInfo2 = new EmbeddedEntity();
    studentInfo2.setProperty("timeEntered", DATE);
    studentInfo2.setProperty("workspaceID", WORKSPACE_ID);
    addQueue2.setProperty("test2", studentInfo2);

    EmbeddedEntity addQueue3 = new EmbeddedEntity();
    EmbeddedEntity studentInfo3 = new EmbeddedEntity();
    studentInfo3.setProperty("timeEntered", DATE);
    studentInfo3.setProperty("workspaceID", WORKSPACE_ID);
    addQueue3.setProperty("uID", studentInfo3);

    EmbeddedEntity addQueue4 = new EmbeddedEntity();
    EmbeddedEntity studentInfo4 = new EmbeddedEntity();
    studentInfo4.setProperty("timeEntered", DATE);
    studentInfo4.setProperty("workspaceID", WORKSPACE_ID);
    addQueue4.setProperty("test3", studentInfo4);

    init.setProperty("studentQueue", Arrays.asList(addQueue1, addQueue2, addQueue3, addQueue4));

    datastore.put(init);

    when(httpRequest.getParameter("classCode")).thenReturn(KeyFactory.keyToString(init.getKey()));
    when(httpRequest.getParameter("studentToken")).thenReturn("testID");

    FirebaseToken mockToken = mock(FirebaseToken.class);
    when(authInstance.verifyIdToken("testID")).thenReturn(mockToken);
    when(mockToken.getUid()).thenReturn("uID");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(httpResponse.getWriter()).thenReturn(writer);

    queue.doGet(httpRequest, httpResponse);

    Gson gson = new Gson();

    assertEquals(stringWriter.toString(), gson.toJson(new StudentStatus(3, WORKSPACE_ID)));
  }

  @Test
  public void duplicateInQueue() throws Exception {
    Entity init = new Entity("Class");

    init.setProperty("owner", "ownerID");
    init.setProperty("name", "testClass");
    init.setProperty("beingHelped", "");

    EmbeddedEntity addQueue1 = new EmbeddedEntity();
    EmbeddedEntity studentInfo1 = new EmbeddedEntity();
    studentInfo1.setProperty("timeEntered", DATE);
    studentInfo1.setProperty("workspaceID", WORKSPACE_ID);
    addQueue1.setProperty("uID", studentInfo1);

    EmbeddedEntity addQueue2 = new EmbeddedEntity();
    EmbeddedEntity studentInfo2 = new EmbeddedEntity();
    studentInfo2.setProperty("timeEntered", DATE);
    studentInfo2.setProperty("workspaceID", WORKSPACE_ID);
    addQueue2.setProperty("test2", studentInfo2);

    EmbeddedEntity addQueue3 = new EmbeddedEntity();
    EmbeddedEntity studentInfo3 = new EmbeddedEntity();
    studentInfo3.setProperty("timeEntered", DATE);
    studentInfo3.setProperty("workspaceID", WORKSPACE_ID);
    addQueue3.setProperty("uID", studentInfo3);

    EmbeddedEntity addQueue4 = new EmbeddedEntity();
    EmbeddedEntity studentInfo4 = new EmbeddedEntity();
    studentInfo4.setProperty("timeEntered", DATE);
    studentInfo4.setProperty("workspaceID", WORKSPACE_ID);
    addQueue4.setProperty("test3", studentInfo4);

    init.setProperty("studentQueue", Arrays.asList(addQueue1, addQueue2, addQueue3, addQueue4));

    datastore.put(init);

    when(httpRequest.getParameter("classCode")).thenReturn(KeyFactory.keyToString(init.getKey()));
    when(httpRequest.getParameter("studentToken")).thenReturn("testID");

    FirebaseToken mockToken = mock(FirebaseToken.class);
    when(authInstance.verifyIdToken("testID")).thenReturn(mockToken);
    when(mockToken.getUid()).thenReturn("uID");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(httpResponse.getWriter()).thenReturn(writer);

    queue.doGet(httpRequest, httpResponse);

    Gson gson = new Gson();

    assertEquals(stringWriter.toString(), gson.toJson(new StudentStatus(1, WORKSPACE_ID)));
  }

  @Test
  public void notInQueue() throws Exception {
    Entity init = new Entity("Class");

    init.setProperty("owner", "ownerID");
    init.setProperty("name", "testClass");

    EmbeddedEntity beingHelped = new EmbeddedEntity();
    EmbeddedEntity helping = new EmbeddedEntity();
    helping.setProperty("taID", "TAUID");
    helping.setProperty("workspaceID", WORKSPACE_ID);
    beingHelped.setProperty("uID", helping);
    init.setProperty("beingHelped", beingHelped);

    EmbeddedEntity addQueue1 = new EmbeddedEntity();
    EmbeddedEntity studentInfo1 = new EmbeddedEntity();
    studentInfo1.setProperty("timeEntered", DATE);
    addQueue1.setProperty("test1", studentInfo1);

    EmbeddedEntity addQueue2 = new EmbeddedEntity();
    EmbeddedEntity studentInfo2 = new EmbeddedEntity();
    studentInfo2.setProperty("timeEntered", DATE);
    addQueue2.setProperty("test2", studentInfo2);

    EmbeddedEntity addQueue3 = new EmbeddedEntity();
    EmbeddedEntity studentInfo3 = new EmbeddedEntity();
    studentInfo3.setProperty("timeEntered", DATE);
    addQueue3.setProperty("test3", studentInfo3);

    EmbeddedEntity addQueue4 = new EmbeddedEntity();
    EmbeddedEntity studentInfo4 = new EmbeddedEntity();
    studentInfo4.setProperty("timeEntered", DATE);
    addQueue4.setProperty("test4", studentInfo4);

    init.setProperty("studentQueue", Arrays.asList(addQueue1, addQueue2, addQueue3, addQueue4));

    datastore.put(init);

    when(httpRequest.getParameter("classCode")).thenReturn(KeyFactory.keyToString(init.getKey()));
    when(httpRequest.getParameter("studentToken")).thenReturn("testID");

    FirebaseToken mockToken = mock(FirebaseToken.class);
    when(authInstance.verifyIdToken("testID")).thenReturn(mockToken);
    when(mockToken.getUid()).thenReturn("uID");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(httpResponse.getWriter()).thenReturn(writer);

    queue.doGet(httpRequest, httpResponse);

    Gson gson = new Gson();

    assertEquals(stringWriter.toString(), gson.toJson(new StudentStatus(0, WORKSPACE_ID)));
  }

  @Test
  public void notBeingHelped() throws Exception {
    Entity init = new Entity("Class");

    init.setProperty("owner", "ownerID");
    init.setProperty("name", "testClass");

    EmbeddedEntity beingHelped = new EmbeddedEntity();
    init.setProperty("beingHelped", beingHelped);

    EmbeddedEntity addQueue1 = new EmbeddedEntity();
    EmbeddedEntity studentInfo1 = new EmbeddedEntity();
    studentInfo1.setProperty("timeEntered", DATE);
    addQueue1.setProperty("test1", studentInfo1);

    EmbeddedEntity addQueue2 = new EmbeddedEntity();
    EmbeddedEntity studentInfo2 = new EmbeddedEntity();
    studentInfo2.setProperty("timeEntered", DATE);
    addQueue2.setProperty("test2", studentInfo2);

    EmbeddedEntity addQueue3 = new EmbeddedEntity();
    EmbeddedEntity studentInfo3 = new EmbeddedEntity();
    studentInfo3.setProperty("timeEntered", DATE);
    addQueue3.setProperty("test3", studentInfo3);

    EmbeddedEntity addQueue4 = new EmbeddedEntity();
    EmbeddedEntity studentInfo4 = new EmbeddedEntity();
    studentInfo4.setProperty("timeEntered", DATE);
    addQueue4.setProperty("test4", studentInfo4);

    init.setProperty("studentQueue", Arrays.asList(addQueue1, addQueue2, addQueue3, addQueue4));

    datastore.put(init);

    when(httpRequest.getParameter("classCode")).thenReturn(KeyFactory.keyToString(init.getKey()));
    when(httpRequest.getParameter("studentToken")).thenReturn("testID");

    FirebaseToken mockToken = mock(FirebaseToken.class);
    when(authInstance.verifyIdToken("testID")).thenReturn(mockToken);
    when(mockToken.getUid()).thenReturn("uID");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(httpResponse.getWriter()).thenReturn(writer);

    queue.doGet(httpRequest, httpResponse);

    Gson gson = new Gson();

    assertEquals(stringWriter.toString(), gson.toJson(new StudentStatus(0, "")));
  }
}