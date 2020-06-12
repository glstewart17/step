// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.sps.data.Comment;
import com.google.sps.data.CommentResult;
import com.google.sps.data.User;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns comments and handles their creation. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  /**
   * For a get request, return a JSON with all the comments and the number of comments.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    // Get the count per page and page number from the request.
    int commentsPerPage = Integer.parseInt(request.getParameter("count"));
    int pageNumber = Integer.parseInt(request.getParameter("page"));

    // Prepare query and get all comments in Datastore.
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Create a list of comments based on results, with a limited number of comments.
    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      Comment comment = new Comment(
        (long) entity.getKey().getId(),  
        (String) entity.getProperty("content"), 
        (String) entity.getProperty("author"), 
        (long) entity.getProperty("timestamp"),
        (String) entity.getProperty("image"));
      comments.add(comment);
    }

    // Store total number of comments, then assign a sublist with the comments on the specified page.
    int commentCount = comments.size();
    if (pageNumber * commentsPerPage > commentCount) {
      comments = comments.subList((pageNumber - 1) * commentsPerPage,  commentCount);
    } else {
      comments = comments.subList((pageNumber - 1) * commentsPerPage, pageNumber * commentsPerPage);
    }

    // If user logged in, find id and get logout url, otherwise find login url.
    UserService userService = UserServiceFactory.getUserService();
    String id = "";
    String url;
    if (userService.isUserLoggedIn()) {
      id = userService.getCurrentUser().getUserId();
      User currentUser = getUserInfo(id);
      id = currentUser.getName();
      url = userService.createLogoutURL("/index.html");
    } else {
      url = userService.createLoginURL("/index.html");
    }

    // Converts comments into a JSON string using the Gson library.
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(new CommentResult(comments, commentCount, id, url)));
  }

  /**
   * For a post request, get all attributes, create an entity, fill the attributes, and store entity.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    UserService userService = UserServiceFactory.getUserService();
    String content = request.getParameter("content");
    String id = userService.getCurrentUser().getUserId();
    User currentUser = getUserInfo(id);
    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("content", content);
    commentEntity.setProperty("author", currentUser.getName());
    commentEntity.setProperty("image", currentUser.getImage());

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
  }

  /**
   * Returns the nickname of the user with id, if one doesn't exist, create a default account.
   */
  private User getUserInfo(String id) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
      new Query("User")
        .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      Entity userEntity = new Entity("User", id);
      userEntity.setProperty("id", id);
      
      Random rand = new Random();
      int number = rand.nextInt(9000) + 1000;
      String name = "User" + Integer.toString(number);
      userEntity.setProperty("name", name);
      
      String image = "images/default.png";
      userEntity.setProperty("image", image);

      // The put() function automatically inserts new data or updates existing data based on ID
      datastore.put(userEntity);
      System.out.println("New user created");
      return new User(id, name, image);
    }
    String name = (String) entity.getProperty("name");
    String image = (String) entity.getProperty("image");
    return new User(id, name, image);
  }
}
