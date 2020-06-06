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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.sps.data.Comment;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that deletes all comments from datastore. */
@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {

  /**
   * Delete all comments if id is 1, otherwise delete commnent with specific id.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    
    long id = Long.parseLong(request.getParameter("id"));

    if(id != 1) {
      Key commentEntityKey = KeyFactory.createKey("Comment", id);
      datastore.delete(commentEntityKey);
      return;
    }

    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    List<Key> commentEntityKeys = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      Key commentEntityKey = KeyFactory.createKey("Comment", entity.getKey().getId());
      commentEntityKeys.add(commentEntityKey);
    }
        
    datastore.delete(commentEntityKeys);
  }
}
