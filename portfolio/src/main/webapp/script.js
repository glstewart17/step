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

  hideForms();

  // Make get request to get commentCount number of comments.
  $.get("/data", { count: countEntry, page: pageEntry }, function(data, textStatus, jqXHR) {

    // Empty the list that will receive the comments.
    const commentList = document.getElementById("comment-list");
    commentList.innerHTML="";

    // For each comment, create and append a list element.
    data.comments.forEach((comment) => {
      commentList.appendChild(createCommentElement(comment, data.userName));
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

    // Handle login status.
    checkLogin(data.userName, data.userImage, data.url);
  }).catch((error) => {
    console.log(error)
  });
}

/**
 * Creates a list element that represents each comment.
 */ 
function createCommentElement(comment, userName) {

  // Create a commentElement that will be placed in commentList. 
  const commentElement = document.createElement("li");

  // Create the row, which will hold the columns in the same row.
  const row = document.createElement("div");
  row.className = "row";

  // Create a column for the icon div and icon of the author.
  const columnIcon = document.createElement("div");
  columnIcon.className = "column-20";
  const iconDiv = document.createElement("div");
  iconDiv.className = "icon";
  const img = document.createElement("img");
  img.src = comment.image;
  iconDiv.appendChild(img);
  columnIcon.appendChild(iconDiv);

  // Create a column for content and a delete button, which will take up 80% of the row.
  const columnContent = document.createElement("div");
  const columnDelete = document.createElement("div");

  if (comment.author == userName) {

    // Split the reamain percent of the row and assign classes.
    columnContent.className = "column-60";
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

    // Add delete button to the column.
    columnDelete.append(deleteButton);
  }
  else {

    // Create a column for the comment info and do not create a button.
    columnContent.className = "column-80";
  }

  // Create the paragraphs that the comment and author go in and add to the content column.
  const contentElement = document.createElement("p");
  contentElement.innerText = comment.content;
  const authorElement = document.createElement("p");
  authorElement.innerText = "- " + comment.author;
  columnContent.appendChild(contentElement);
  columnContent.appendChild(authorElement);

  // Append all columns to the row they exist in.
  row.appendChild(columnIcon);
  row.appendChild(columnContent);
  if (comment.author == userName) {
    row.appendChild(columnDelete);
  }

  // Add the row to comment element and return.
  commentElement.appendChild(row);
  return commentElement;
}

/**
 * Add a comment using the author and content field.
 */
function addComment() {
  let content = $("#comment-content").val();

  // If a field is empty, alert the user and do not post.
  if (content === "") {
    alert("All fields must be filled before submission.");
    return;
  }

  // Make post request to submit new comment and get comments after.
  $.post("/data", { content: content } );
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
  $("#update-image").click(function() {
    updateImage();
  });
  $("#update-name").click(function() {
    updateName();
  });

  // Disable file submission button initially.
  $("#update-image").prop("disabled", true);
});

/**
 * Get a blobstore URL and add to the button, display the form when done.
 */
function fetchBlobstoreUrlAndEnableButton() {
  fetch('/blobstore-upload-url').then((response) => {
    return response.text();
  }).then((imageUploadUrl) => {
    $("#update-image").val(imageUploadUrl);
    $("#update-image").prop("disabled", false);
  });
}

/**
 * Add a comment using the author and content field.
 */
function updateImage() {

  // Create a FormData object and add the file.
  var data = new FormData();
  data.append("file", $("#file").prop("files")[0]);

  // Disable the image submit button and empty the result div.
  $("#update-image").prop("disabled", true);

  // Post the file to the blobstore URL.
  $.ajax({
    type: "POST",
    enctype: 'multipart/form-data',
    url: $("#update-image").val(),
    data: data,
    processData: false,
    contentType: false,
    cache: false,
    timeout: 600000,
    success: function(data, status, jqXHR) {

      // Make post request to submit new comment and update page after.
      $.post("/user", { image: data } );
      fetchBlobstoreUrlAndEnableButton();
      getComments();
    },
    error: function(error) {
      
      // Fetch another blobstore URL and enable the submit button, and log error.
      fetchBlobstoreUrlAndEnableButton();
      console.log(error.responseText);
    }
  });
};

/**
 * Check if user is logged in and provide the correct forms.
 */
function checkLogin(userName, userimage, url) {
  
  // Get and empty the login div.
  const loginDiv = document.getElementById("login-form");
  loginDiv.innerHTML="";

  // If no userName, setup for login, otherwise setup forms and logout.
  if (userName == "") {

    // Create the row, which will hold the columns in the same row.
    const row = document.createElement("div");
    row.className = "row";

    // Make the columns that will hold the comments and button, taking 80% and 20% of the row.
    const columnLabel = document.createElement("div");
    columnLabel.className = "column-80";
    const columnLogin = document.createElement("div");
    columnLogin.className = "column-20";

    // Remove the comment and call to delete when the button is pressed.
    const loginButton = document.createElement("button");
    loginButton.className = "login"
    loginButton.innerText = "Login";
    loginButton.addEventListener("click", () => {
    window.location.href=url;
    });

    const message = document.createElement("label");
    message.innerText = "Login to comment";

    columnLogin.append(loginButton);
    columnLabel.appendChild(message);
    row.appendChild(columnLabel);
    row.appendChild(columnLogin);
    loginDiv.appendChild(row);
  }
  else {

    // Create the row, which will hold the columns in the same row.
    const row = document.createElement("div");
    row.className = "row";

    // Make the columns that will hold the comments and button, taking 80% and 20% of the row.
    const columnIcon = document.createElement("div");
    columnIcon.className = "column-20";
    const columnLabel = document.createElement("div");
    columnLabel.className = "column-60";
    const columnLogout = document.createElement("div");
    columnLogout.className = "column-20";

    // Create a div and image for icon and append them to the icon column.
    const iconDiv = document.createElement("div");
    iconDiv.className = "icon";
    const img = document.createElement("img");
    img.src = userimage;
    iconDiv.appendChild(img);
    columnIcon.appendChild(iconDiv);

    // Create a logout button with a function and add to the logout column.
    const logoutButton = document.createElement("button");
    logoutButton.className = "login"
    logoutButton.innerText = "Logout";
    logoutButton.addEventListener("click", () => {
      window.location.href=url;
    });
    columnLogout.append(logoutButton);
    
    // Create a user name label and add to the label column 
    const message = document.createElement("label");
    message.innerText = "User: " + userName;
    columnLabel.appendChild(message);

    // Append all labels to the row and add row to the login div.
    row.appendChild(columnIcon);
    row.appendChild(columnLabel);
    row.appendChild(columnLogout);
    loginDiv.appendChild(row);

    // Show the user forms.
    showForms();
  }
}

/**
 * Hide the user forms.
 */
function hideForms() {
  $("#update-user-form").css({"display":"none"});
  $("#comment-form").css({"display":"none"});
}

/**
 * Show user forms, if there is a user.
 */
function showForms() {
  $("#update-user-form").css({"display":"unset"});
  $("#comment-form").css({"display":"unset"});
}

/**
 * Change the user's name and update comments.
 */
function updateName() {
  
  // Get name, alert user if the field is empty.
  let name = $("#name").val();
  if (name === "") {
    alert("Name must be filled before submission.");
    return;
  }

  // Make post request to submit new comment and get comments after.
  $.post("/user", { name: name } );
  getComments();
}