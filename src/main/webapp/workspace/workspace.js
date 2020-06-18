let currentEditor;

function createFirepad(ref, parent, language) {
  const editor = monaco.editor.create(parent, {
    language: language,
    theme: "vs-dark"
  });

  Firepad.fromMonaco(ref, editor);
  currentEditor = editor;
}

function getFirebaseRef() {
  const workspaceID = getParam("workspaceID");
  if (workspaceID !== null) {
    return firebase.database().ref().child(workspaceID);
  } else {
    // If we were not given a workspace ID redirect to the home page.
    window.location.href = "/";
  }
}

document.addEventListener("DOMContentLoaded", () => {
  const config = {
    apiKey: 'AIzaSyA1r_PfVDCXfTgoUNisci5Ag2MKEEwsZCE',
    databaseURL: "https://fulfillment-deco-step-2020.firebaseio.com",
    projectId: "fulfillment-deco-step-2020",
  };
  firebase.initializeApp(config);

  const ref = getFirebaseRef();

  require.config({ paths: {'vs': 'https://unpkg.com/monaco-editor@latest/min/vs'}});
  require(['vs/editor/editor.main'], function() {
    createFirepad(ref, document.getElementById("firepad-container"), "javascript");
  });
});

window.onresize = () => {
  currentEditor.layout();
};