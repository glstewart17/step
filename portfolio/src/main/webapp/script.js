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
 * Fetches a quote from the Office from the server and adds it to the DOM.
 */
function getOfficeQuote() {
  fetch('/data').then(response => response.json()).then((data) => {
    
    // data is json of Comment, so reference its attributes
    const quoteContainer = document.getElementById('quote-container');
    quoteContainer.innerText = "\"" + data.content + "\" - " + data.author;
  }).catch((error) => {
    console.log(error)
  });
}
