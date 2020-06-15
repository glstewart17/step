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

/** Class containing the author, content, timestamp, and image URL of a comment. */
public final class Comment {

  private final long id;
  private final String content;
  private final String author;
  private final long timestamp;
  private final String imageUrl;

  public Comment(long id, String content, String author, long timestamp, String imageUrl) {
    this.id = id;
    this.content = content;
    this.author = author;
    this.timestamp = timestamp;
    this.imageUrl = imageUrl;
  }

  public String getContent() {
    return content;
  }

  public String getAuthor() {
    return author;
  }

  public long getId() {
      return id;
  }

  public long getTimestamp() {
      return timestamp;
  }

  public String getImageUrl() {
    return imageUrl;
  }
}