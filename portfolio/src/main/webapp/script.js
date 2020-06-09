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

let index = 0;
let timer;

/**
 * Update index, clear timer, and call show.
 */
function currentSlide(n) {
  index = n;
  clearTimeout(timer);
  showSlide();
}

/**
 * Display the correct image and comment, and reset timer.
 */
function showSlide() {
  let i;
  let slides = document.getElementsByClassName("slide");
  let dots = document.getElementsByClassName("dot");

  // Comments corresponding to the images.
  const comments = ["Selfie with the Nittany Lion at Penn State",
    "High School graduation photo at the podium", 
    "Convert to Code team photo",
    "PIAA runner-up in volleyball"];

  // Add comment to the div under the image.
  const commentContainer = document.getElementById("comment-container");
  commentContainer.innerText = comments[index];

  // Set all slides to not display, and remove active from all dots.
  for (i = 0; i < slides.length; i++) {
    slides[i].style.display = "none";
    dots[i].className = dots[i].className.replace(" active", ""); 
  }

  // Set correct dot to active and show correct slide.
  slides[index].style.display = "block";  
  dots[index].className += " active";

  // Show next slide in 5 seconds.
  const TIMEOUT_MILLISECONDS = 5000;
  index = (index + 1) % slides.length;
  timer = window.setTimeout(showSlide, TIMEOUT_MILLISECONDS);      
}

/**
 * Fetches all the comments from the server and add them to comment-list.
 */
function getComments() {

  const countEntry = $("#comment-count").val();
  const pageEntry = $("#page-number").val();

  // Make get request to get commentCount number of comments.
  $.get("/data", { count: countEntry, page: pageEntry }, function(data, textStatus, jqXHR) {
    
    console.log(data);
    // Empty the list that will receive the comments.
    const commentList = document.getElementById("comment-list");
    commentList.innerHTML="";

    // For each comment, create and append a list element.
    data.comments.forEach((comment) => {
      commentList.appendChild(createCommentElement(comment));
    })
    
    // Create new page number options based on new get comments.
    $("#page-number").empty();
    $("#page-number").append("<option selected='selected' value='1'>1</option>");
    
    // Add page number options for all pages after 1 that there are enough comments for.
    let key = 2;
    while(data.commentCount > (key - 1) * countEntry) {
      $("#page-number").append($("<option></option>").val(key).text(key));
      key += 1;
    }

    // Restore old page number option.
    $("#page-number").val(pageEntry);

  }).catch((error) => {
    console.log(error)
  });
}

/**
 * Creates a list element that represents each comment.
 */ 
function createCommentElement(comment) {
  
  // Create a commentElement that will be placed in commentList. 
  const commentElement = document.createElement("li");

  // Create the row, which will hold the columns in the same row.
  const row = document.createElement("div");
  row.className = "row";

  // Make the columns that will hold the comments and button, taking 80% and 20% of the row.
  const columnContent = document.createElement("div");
  columnContent.className = "column-80";
  const columnDelete = document.createElement("div");
  columnDelete.className = "column-20";

  // Remove the comment and call to delete when the button is pressed.
  const deleteButton = document.createElement("button");
  deleteButton.className = "delete"
  deleteButton.innerText = "Delete";
  deleteButton.addEventListener("click", () => {
    
    // If only one element, go to the previous page, if not 1.
    if ($("#comment-list").children().length == 1 && $("#page-number").val() != 1 ) {
      $("#page-number").val($("#page-number").val() - 1);
    }

    // Delete the comment with this id.
    deleteComments(comment.id);
  });

  // Create the paragraphs that the comment and author go in.
  const contentElement = document.createElement("p");
  contentElement.innerText = comment.content;
  const authorElement = document.createElement("p");
  authorElement.innerText = "- " + comment.author;

  // Append all elements to the larger elements they exist in.
  columnDelete.append(deleteButton);
  columnContent.appendChild(contentElement);
  columnContent.appendChild(authorElement);
  row.appendChild(columnContent);
  row.appendChild(columnDelete);
  commentElement.appendChild(row);
  return commentElement;
}

/**
 * Add a comment using the author and content field.
 */
function addComment() {
  let author = $("#comment-author").val();
  let content = $("#comment-content").val();

  if (author === "" || content === "") {
    alert("All fields must be filled before submission.");
    return;
  }

  // Make post request to submit new comment and get comments after.
  $.post("/data", { author: author, content: content } );
  getComments();
};

/**
 * Delete comment based on id or all comments, then get comments.
 */
function deleteComments(commentId) {
  $.post("/delete-data", { id: commentId }, function(data, textStatus, jqXHR) {
    getComments();
  }); 
}

/**
 * When count changes, set page-number to 1 and get comments.
 */
function countChange() {
  $("#page-number").val(1);
  getComments();
}

/**
 * Call the corresponding function when clicked or changed.
 */
$(document).ready(function() {
  $("#add-comment").click(function() {
    addComment();
  });
  $("#delete-all").click(function() {
    $("#page-number").val(1);
    deleteComments(1);
  });
  $("#limit-comments").click(function() {
    getComments();
  });
  $("#comment-count").change(function() {
    countChange();
  });
  $("#page-number").change(function() {
    getComments();
  });
  $("#upload-button").click(function() {
    filePost();
  });
});

function fetchBlobstoreUrlAndShowForm() {
  fetch('/blobstore-upload-url')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        const messageForm = document.getElementById('my-form');
        messageForm.action = imageUploadUrl;
        messageForm.classList.remove('hidden');
      });
}

/**
 * Add a comment using the author and content field.
 */
function filePost() {
  const messageForm = document.getElementById('my-form');
  let imageUploadUrl = messageForm.action;
  
  console.log(imageUploadUrl);
};
