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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles the updating of user account. */
@WebServlet("/user")
public class UserServlet extends HttpServlet {

  /**
   * For a post request, get user entity and update the filled out fields.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    // Set up user service and datastore.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();
    
    // If the user is not logged it, return error response. 
    if (!userService.isUserLoggedIn()) {
      response.setContentType("text/html");
      response.getWriter().println("User not logged in.");
      return;
    }

    // Get user id and use it to get the user's entity from datastore.
    String id = userService.getCurrentUser().getUserId();
    Query query =
      new Query("User")
        .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();

    // If name field is not null, change name.
    String name = request.getParameter("name");
    if (name != null) {
        entity.setProperty("name", name);
    }

    // If image field is not null, change image.
    String image = request.getParameter("image");
    if (image != null) {
        entity.setProperty("image", image);
    }

    // Store the entity, it will update existing, and return success.
    datastore.put(entity);
    response.setContentType("text/html");
    response.getWriter().println("User has been updated.");
  }
}
