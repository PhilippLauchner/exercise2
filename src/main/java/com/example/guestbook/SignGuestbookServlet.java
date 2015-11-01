package com.example.guestbook;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.cmd.Query;
import com.example.guestbook.Guestbook;

/**
 * Form Handling Servlet
 * Most of the action for this sample is in webapp/guestbook.jsp, which displays the
 * {@link Greeting}'s. This servlet has one method
 * {@link #doPost(<#HttpServletRequest req#>, <#HttpServletResponse resp#>)} which takes the form
 * data and saves it.
 */
public class SignGuestbookServlet extends HttpServlet {

  // Process the http POST of the form
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    Greeting greeting;

    UserService userService = UserServiceFactory.getUserService();
    User user = userService.getCurrentUser();  // Find out who the user is.

    String guestbookName = req.getParameter("guestbookName");
    String content = req.getParameter("content");
    if (user != null) {
      greeting = new Greeting(guestbookName, content, user.getUserId(), user.getEmail());
    } else {
      greeting = new Greeting(guestbookName, content);
    }

    com.googlecode.objectify.Key<Guestbook> theBook = com.googlecode.objectify.Key.create(Guestbook.class, guestbookName);
    List<Greeting> greetings = ObjectifyService.ofy()
            .load()
            .type(Greeting.class) // We want only Greetings
            .ancestor(theBook)    // Anyone in this book
            .order("-date").list(); 
    // Use Objectify to save the greeting and now() is used to make the call synchronously as we
    // will immediately get a new page using redirect and we want the data to be present.
            
    if (greetings.size()>4){
    	ObjectifyService.ofy().delete().entity(greetings.get(4)).now();   
    }
    ObjectifyService.ofy().save().entity(greeting).now();

    resp.sendRedirect("/guestbook.jsp?guestbookName=" + guestbookName);
  }
}