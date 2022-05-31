$(document).ready(function () {
  $.ajaxSetup({
    headers: {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
  });

  $("#createButton").click(function () {
    createAndSend();
  });


  let source = new EventSource("/events");
  source.onmessage = function(event) {
    refreshDatasets();
  };

  refreshDatasets();
});

function refreshDatasets() {
  $.getJSON("/demo/datasets", function (datasets) {
        printDatasetsTable(datasets);
  });
}

function printDatasetsTable(datasets) {
  const datasetsTableBody = $('#datasetsTableBody');
  datasetsTableBody.children().remove();
  datasets.forEach((dataset) => {
      const row = $('<tr/>').appendTo(datasetsTableBody);
      row.append($('<td style="text-align:right">' + dataset.problemId + '</td>'));
      row.append($('<td>' + dataset.lessons + ' lessons, ' + dataset.rooms + ' rooms</td>'));
      const state = dataset.solved ? 'solved' : 'solving';
      row.append($('<td>' + state + '</td>'));
      const score = dataset.score ? dataset.score : 'unknown';
      row.append($('<td>' + score + '</td>'));
  });
}

function createAndSend() {
  var lessons = $("#lessons").val().trim();

  if (isNaN(lessons) || lessons < 10 || lessons > 200) {
    const errorMessage = "The number of lessons (" + lessons + ") must be between 10 and 200.";
    showError(errorMessage, null);
    return;
  }
  $.post("/demo/" + lessons, function () {
    refreshDatasets();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Creating a dataset has failed." , xhr);
  });

}

function showError(title, xhr) {
  const serverErrorMessage = !xhr ? '' : !xhr.responseJSON ? `${xhr.status}: ${xhr.statusText}` : xhr.responseJSON.message;
  console.error(title + "\n" + serverErrorMessage);
  const notification = $(`<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 30rem"/>`)
    .append($(`<div class="toast-header bg-danger">
                 <strong class="mr-auto text-dark">Error</strong>
                 <button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">
                   <span aria-hidden="true">&times;</span>
                 </button>
               </div>`))
    .append($(`<div class="toast-body"/>`)
      .append($(`<p/>`).text(title))
      .append($(`<pre/>`)
        .append($(`<code/>`).text(serverErrorMessage))
      )
    );
  $("#notificationPanel").append(notification);
  notification.toast({delay: 30000});
  notification.toast('show');
}