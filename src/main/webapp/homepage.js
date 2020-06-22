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

// Display sign-in options on landing page
function loadSignIn() {
  // web app's Firebase configuration
  var firebaseConfig = {
    apiKey: "AIzaSyA1r_PfVDCXfTgoUNisci5Ag2MKEEwsZCE",
    authDomain: "fulfillment-deco-step-2020.firebaseapp.com",
    databaseURL: "https://fulfillment-deco-step-2020.firebaseio.com",
    projectId: "fulfillment-deco-step-2020",
    storageBucket: "fulfillment-deco-step-2020.appspot.com",
    messagingSenderId: "7165833112",
    appId: "1:7165833112:web:3b4af53c5de6aa73b7c5ed"
  };
  // Initialize Firebase
  firebase.initializeApp(firebaseConfig);

  // Initialize the FirebaseUI Widget using Firebase
  var ui = new firebaseui.auth.AuthUI(firebase.auth());

  ui.start('#firebaseui-auth-container', {
    // Provide sign in options for user to select from
    signInOptions : [
      {
        provider: firebase.auth.GoogleAuthProvider.PROVIDER_ID, // Sign in w/ Google Account
        customParameters: {
          prompt: 'select_account'
        }
      },
    ],
    signInSuccessUrl: "/enterClass.html",
  });
}

// Log out and provide indication of user status
function logout() {
  firebase.auth().signOut().then(function() {
  console.log('Signed Out');
  fetch("/sign-out");
  }, function(error) {
  console.error('Sign Out Error', error);
  });
}