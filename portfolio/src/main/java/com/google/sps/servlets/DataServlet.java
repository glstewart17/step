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
import com.google.appengine.api.datastore.FetchOptions.Builder;
import com.google.sps.data.Comment;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns a random quote from the office. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  /**
   * For a get request, return a JSON version of all the comments.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    // Get the count from the request.
    int commentCount = getCommentCount(request);

    // Prepare query and get all comments in Datastore.
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    // Create a list of comments based on results, with a limited number of comments.
    List<Comment> comments = new ArrayList<>();
    for (Entity entity : results.asIterable(Builder.withLimit(commentCount))) {
      Comment comment = new Comment(
        (long) entity.getKey().getId(),  
        (String) entity.getProperty("content"), 
        (String) entity.getProperty("author"), 
        (long) entity.getProperty("timestamp"));
      comments.add(comment);
    }

    // Converts comments into a JSON string using the Gson library.
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(comments));
  }

  /**
   * For a post request, get all attributes, create an entity, fill the attributes, and store entity.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    String content = request.getParameter("content");
    String author = request.getParameter("author");
    long timestamp = System.currentTimeMillis();

    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("content", content);
    commentEntity.setProperty("author", author);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
  }

  /**
   * Get the number of comments from the request.
   */
  private int getCommentCount(HttpServletRequest request) {

    // Convert the string version of the count to a float and define max.
    Float commentCount = Float.parseFloat(request.getParameter("count"));
    final int MAX_COMMENT_COUNT = 30;

    if (commentCount < 0) {
      return (int) 0;
    }

    if(commentCount > MAX_COMMENT_COUNT){
      return (int) MAX_COMMENT_COUNT;
    }

    return (int) Math.round(commentCount);
  }
}
