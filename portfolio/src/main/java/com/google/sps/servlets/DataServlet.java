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
import com.google.sps.data.Comment;
import com.google.sps.data.CommentResult;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        (long) entity.getProperty("timestamp"));
      comments.add(comment);
    }

    // Store total number of comments, then assign a sublist with the comments on the specified page.
    int commentCount = comments.size();
    if (pageNumber * commentsPerPage > commentCount) {
      comments = comments.subList((pageNumber - 1) * commentsPerPage,  commentCount);
    } else {
      comments = comments.subList((pageNumber - 1) * commentsPerPage, pageNumber * commentsPerPage);
    }

    // Converts comments into a JSON string using the Gson library.
    Gson gson = new Gson();
    response.setContentType("application/json;");
    response.getWriter().println(gson.toJson(new CommentResult(comments, commentCount)));
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
}
