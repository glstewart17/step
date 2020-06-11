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

package com.google.sps.data;

import com.google.sps.data.Comment;
import java.util.ArrayList;
import java.util.List;

/** Class containing a list of the comments being returned and the number of comments in datastore. */
public final class CommentResult {

  private final List<Comment> comments;
  private final int commentCount;
  private final String userName;
  private final String url;

  public CommentResult(List<Comment> comments, int commentCount, String userName, String url) {
    this.comments = comments;
    this.commentCount = commentCount;
    this.userName = userName;
    this.url = url;
  }
}