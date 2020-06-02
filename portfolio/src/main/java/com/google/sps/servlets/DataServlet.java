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

import com.google.sps.data.QuotePerson;
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

  private List<QuotePerson> quotes;
  private int index = -1;

  /**
   * Add all the quotes to quotes using type QuotePerson on start.
   */
  @Override
  public void init() {
    quotes = new ArrayList<>();
    quotes.add(new QuotePerson("Bears. Beets. Battlestar Galactica.", "Jim Halpert"));
    quotes.add(new QuotePerson("I'm not supersitious, but I am a little stitious.", "Michael Scott"));
    quotes.add(new QuotePerson("The worst thing about prison was the dementors.", "Michael Scott"));
    quotes.add(new QuotePerson("I talk a lot. so I've learned to tune myself out.", "Kelly Kapoor"));
    quotes.add(new QuotePerson("You couldn’t handle my undivided attention.", "Dwight Schrute"));
    quotes.add(new QuotePerson(
        "Sometimes I'll start a sentence and I don't even know where it's going. "
            + "I just hope I find it along the way.", "Michael Scott"));
  }

  /**
   * For a get request, return a JSON version of a quote and person.
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    QuotePerson quote;

    if (index == -1) {
      quote = quotes.get((int) (Math.random() * quotes.size()));
    }
    else {
      quote = quotes.get(index);
    }

    String json = convertToJsonUsingGson(quote);

    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  /**
   * For a post request, change the index that determines which quote is returned during get request.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    
    try {

      // Try and convert input quote-number to integer.
      int quoteNumber = Integer.parseInt(request.getParameter("quote-number"));
          
      // If valid quote number, set index as quote number, otherwise, set index to -1.
      if (quoteNumber <= 0 || quoteNumber > quotes.size()) {
        System.err.println("Index out of range, default to random.");
        index = -1;
      }
      else {
        index = quoteNumber - 1;
      }
    } catch (NumberFormatException e) {
      System.err.println("Could not convert quote-number input to a string.");
      index = -1;
    }

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  /**
   * Converts a ServerStats instance into a JSON string using the Gson library. Note: Gson library 
   * dependency added to pom.xml.
   */
  private String convertToJsonUsingGson(QuotePerson quote) {
    Gson gson = new Gson();
    String json = gson.toJson(quote);
    return json;
  }
}