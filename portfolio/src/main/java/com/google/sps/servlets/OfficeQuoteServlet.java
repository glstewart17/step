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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns a random quote from the office. */
@WebServlet("/random-office-quote")
public final class OfficeQuoteServlet extends HttpServlet {

  private List<String> quotes;

  @Override
  public void init() {
    quotes = new ArrayList<>();
    quotes.add("Bears. Beets. Battlestar Galactica.");
    quotes.add("I'm not supersitious, but I am a little stitious.");
    quotes.add("The worst thing about prison was the dementors.");
    quotes.add("I talk a lot. so I've learned to tune myself out.");
    quotes.add(
        "Sometimes I'll start a sentence and I don't even know where it's going. "
            + "I just hope I find it along the way.");
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String quote = quotes.get((int) (Math.random() * quotes.size()));

    response.setContentType("text/html;");
    response.getWriter().println(quote);
  }
}
