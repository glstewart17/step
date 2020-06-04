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
 * Set slide index to next and call show.
 */
function nextSlide() {
    index = (index + 1) % 4;
    showSlide();
}

/**
 * Update index, clear timer, and call show.
 */
function currentSlide(n) {
  index = n;
  clearTimeout(timer);
  showSlide();
}

/**
 * Display correct image and comment, and reset timer.
 */
function showSlide() {
  let i;
  let slides = document.getElementsByClassName("slide");
  let dots = document.getElementsByClassName("dot");

  // Comments corresponding to the images.
  const comments = ['Selfie with the Nittany Lion at Penn State',
    'High School graduation photo at the podium', 
    'Convert to Code team photo',
    'PIAA runner-up in volleyball'];

  // Add comment to the div under the image.
  const commentContainer = document.getElementById('comment-container');
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
  timer = window.setTimeout(nextSlide, TIMEOUT_MILLISECONDS);
}

/**
 * Fetches all the comments from the server and add them to comment-list.
 */
function getComments() {

  const DEFAULT_COMMENT_COUNT = '5';
  let commentCount = document.getElementById('comment-count').value;

  // If comment count is an empty string, set to default.
  if (commentCount === "") {
    commentCount = DEFAULT_COMMENT_COUNT;
  }

  const url = '/data?count=' + commentCount;
  console.log(url)
  fetch(url).then(response => response.json()).then((comments) => {
    
    // Empty the list that will recieve the comments.
    const commentList = document.getElementById('comment-list');
    commentList.innerHTML="";

    // For each comment, create and append a list element.
    comments.forEach((comment) => {
      commentList.appendChild(createCommentElement(comment));
    })
  }).catch((error) => {
    console.log(error)
  });
}

/**
 * Creates a list element that represents each comment.
 */ 
function createCommentElement(comment) {
  const commentElement = document.createElement('li');

  const contentElement = document.createElement('p');
  contentElement.innerText = comment.content;

  const authorElement = document.createElement('p');
  authorElement.innerText = "- " + comment.author;

  commentElement.appendChild(contentElement);
  commentElement.appendChild(authorElement);
  return commentElement;
}