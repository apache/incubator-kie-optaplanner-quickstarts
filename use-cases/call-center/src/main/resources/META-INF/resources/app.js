const skillToColorMap = new Map([
  ['EN', '#edd400'],
  ['ES', '#ef2929'],
  ['DE', '#e9b96e'],
  ['Car insurance', '#ad7fa8'],
  ['Property insurance', '#729fcf'],
  ['Life insurance', '#73d216']
]);

const pinnedCallColor = '#ebfadc';
const waitingCallColor = 'White';

var autoRefreshIntervalId = null;
var solving = false;

const fetchHeaders = {
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  },
};

function refresh() {
  $.getJSON("/call-center", (callCenterData) => {
    solving = callCenterData.solving;
    refreshSolvingButtons();
    $("#score").text("Score: " + (callCenterData.score == null ? "?" : callCenterData.score));
    printCallTable(callCenterData);
  });
}

function printCallTable(callCenterData) {
  const callTable = $('#callTable');
  callTable.children().remove();
  printHeader(callTable, 10);
  const tableBody = $('<tbody>').appendTo(callTable);
  callCenterData.agents.forEach((agent) => {
    printAgent(tableBody, agent);
  });
}

function printHeader(callTable, calls) {
  const thead = $('<thead>').appendTo(callTable);
  const headerRow = $('<tr>').appendTo(thead);
  headerRow.append($('<th style="width:10%;"><h2>Agent</h2></th>'));
  headerRow.append($('<th colspan="' + calls + '" style="text-align:center"><h2>Incoming calls</h2></th>'))
}

function printAgent(tableBody, agent) {
  const tableRow = $('<tr class="agent-row">').appendTo(tableBody);
  const td = $('<td>').appendTo(tableRow);
  const agentCard = $('<div class="card" style="background-color:#f7ecd5">').appendTo(td);
  const agentCardBody = $('<div class="card-body p-1">').appendTo(agentCard);
  const agentCardRow = $(`<div class="row flex-nowrap">
                <div class="col-4">
                    <i class="fas fa-user-alt"></i>
                </div>
                <div class="col-8">
                    <span style="font-size:1.2em">${agent.name}</span>
                </div>
        </div>`).appendTo(agentCardBody);

  printSkills(agentCardBody, agent.skills);

  const callsTd = $('<td style="flex-flow:row; display: flex;">').appendTo(tableRow);

  agent.calls.forEach((call) => {
    printCall(callsTd, call);
  });
}

function printCall(callsTd, call) {
  const callColor = (call.pinned) ? pinnedCallColor : waitingCallColor;

  const callCard = $(`<div class="card" style="float:left; width: 14rem; background-color: ${callColor}"/>`).appendTo(callsTd);
  const pinIcon = (call.pinned) ? '<i class="fas fa-thumbtack"></i>' : '';

  const callCardContainer = $('<div class="container">').appendTo(callCard);
  callCardContainer
    .append($('<div class="row flex-nowrap" style="padding-top:10px">')
      .append($(`<div class="col-1">${pinIcon}</div>`))
      .append($(`<div class="col-8" style="padding-right:0px">
                           <span class="card-title">${call.phoneNumber}</span>
                       </div>`))
      .append($('<div class="col-xs-2">')
        .append($(`<button class="btn call-btn btn-sm">
                             <i class="fas fa-phone-slash"></i>
                         </button>`)
          .click(() => removeCall(call)))
      )
    );

  printTimes(callCardContainer, call);
  printSkills(callCardContainer, call.requiredSkills);
}

function printTimes(callCardContainer, call) {
  const LocalTime = JSJoda.LocalTime;
  const Duration = JSJoda.Duration;

  const startedTime = LocalTime.parse(call.startTime);
  if (call.pinned) {
    const pickedUpTime = LocalTime.parse(call.pickUpTime);
    const waitingTillPickedUpTime = formatDuration(Duration.between(startedTime, pickedUpTime));
    const inProgressTime = formatDuration(Duration.between(LocalTime.parse(call.pickUpTime), LocalTime.now()));
    $(`<span style="font-size:0.8em">Waiting: ${waitingTillPickedUpTime}</span><br/>`).appendTo(callCardContainer);
    $(`<span style="font-size:0.8em">In progress: ${inProgressTime}</span>`).appendTo(callCardContainer);

    callCardContainer.append($(`<button class="btn btn-sm btn-dark float-right" style="height:1.1rem; padding:0px 4px 0px 4px">
                                       <span style="font-size:0.8em">+ 1m</span>
                                    </button>`).click(() => prolongCall(call)));
  } else {
    const waiting = formatDuration(Duration.between(startedTime, LocalTime.now()));
    const estimatedWaiting = formatDuration(Duration.ofSeconds(Math.floor(call.estimatedWaiting)));
    $(`<span style="font-size:0.8em">Waiting: ${waiting}</span><br/>`).appendTo(callCardContainer);
    $(`<span style="font-size:0.8em">Estimated waiting: ${estimatedWaiting}</span>`)
      .appendTo(callCardContainer);
  }
}

function formatDuration(duration) {
  const hours = Math.floor(duration.seconds() / 3600);
  const minutes = Math.floor((duration.seconds() % 3600) / 60);
  const seconds = duration.seconds() % 60;
  var formattedDuration = '';
  if (hours > 0) {
    formattedDuration += hours + 'h ';
  }
  formattedDuration += minutes + 'm ' + seconds + 's';
  return formattedDuration;
}

function printSkills(container, skills) {
  const skillRow = $('<div class="row" style="margin:4px 2px 4px 0px">');
  container.append(skillRow);
  skills.forEach((skill) => {
    let color = skillToColorMap.get(skill);
    skillRow.append($(`
            <div class="col-xs-1 card" style="background-color:${color};margin:2px;padding:2px">
                <span style="font-size:0.8em">${skill}</span>
            </div>`)
    );
  });
}

function solve() {
  fetch('/call-center/solve', { ...fetchHeaders, method: 'POST' })
    .then((response) => {
      if (!response.ok) {
        return handleErrorResponse('Start solving failed.', response);
      } else {
        solving = true;
        refreshSolvingButtons();
      }
    })
    .catch((error) => handleClientError('Failed to process response.', error));
}

function stopSolving() {
  fetch('/call-center/stop', { ...fetchHeaders, method: 'POST' })
    .then((response) => {
      if (!response.ok) {
        return handleErrorResponse('Stop solving failed', response);
      } else {
        solving = false;
        refreshSolvingButtons();
        refresh();
      }
    })
    .catch((error) => handleClientError('Failed to process response.', error));
}

function refreshSolvingButtons() {
  if (solving) {
    $("#solveButton").hide();
    $("#stopSolvingButton").show();
    if (autoRefreshIntervalId == null) {
      autoRefreshIntervalId = setInterval(refresh, 1000);
    }
  } else {
    $("#solveButton").show();
    $("#stopSolvingButton").hide();
    if (autoRefreshIntervalId != null) {
      clearInterval(autoRefreshIntervalId);
      autoRefreshIntervalId = null;
    }
  }
}

function removeCall(call) {
  fetch("/call/" + call.id, { ...fetchHeaders, method: 'DELETE' })
    .then((response) => {
      if (!response.ok) {
        return handleErrorResponse('Cancelling a call (' + call.phoneNumber + ') failed.', response);
      } else {
        refresh();
      }
    })
    .catch((error) => handleClientError('Failed to process response.', error));
}

function prolongCall(call) {
  fetch("/call/" + call.id, { ...fetchHeaders, method: 'PUT' })
    .then((response) => {
      if (!response.ok) {
        return handleErrorResponse('Prolonging a call (' + call.phoneNumber + ') failed.', response);
      } else {
        refresh();
      }
    })
    .catch((error) => handleClientError('Failed to process response.', error));
}

function removeAgent(agent) {
  fetch('/agent/' + agent.id, { ...fetchHeaders, method: 'DELETE' })
    .then((response) => {
      if (!response.ok) {
        return handleErrorResponse('Deleting an agent (' + agent.name + ') failed.', response);
      } else {
        refresh();
      }
    })
    .catch((error) => handleClientError('Failed to process response.', error));
}

function restartSimulation(frequency, duration) {
  fetch('/simulation', { 
    ...fetchHeaders, 
    method: 'PUT',
    body: JSON.stringify({
      'frequency': frequency,
      'duration': duration
    })
  })
  .then((response) => {
    if (!response.ok) {
      return handleErrorResponse('Updating simulation parameters (frequency:' + frequency + ', duration:' + duration + ') failed.', response);
    }
  })
  .catch((error) => handleClientError('Failed to process response.', error));
}

const formatErrorResponseBody = (body) => {
  // JSON must not contain \t (Quarkus bug)
  const json = JSON.parse(body.replace(/\t/g, '  '));
  return `${json.details}\n${json.stack}`;
};

const handleErrorResponse = (title, response) => {
  return response.text()
    .then((body) => {
      const message = `${title} (${response.status}: ${response.statusText}).`;
      const stackTrace = body ? formatErrorResponseBody(body) : '';
      showError(message, stackTrace);
    });
};

const handleClientError = (title, error) => {
  console.error(title + "\n" + error);
  showError(`${title}.`,
    // Stack looks differently in Chrome and Firefox.
    error.stack.startsWith(error.name)
      ? error.stack
      : `${error.name}: ${error.message}\n    ${error.stack.replace(/\n/g, '\n    ')}`);
};

function showError(message, stackTrace) {
  const notification = $(`<div class="toast" role="alert" aria-live="assertive" aria-atomic="true" style="min-width: 30rem"/>`)
    .append($(`<div class="toast-header bg-danger">
                 <strong class="mr-auto text-dark">Error</strong>
                 <button type="button" class="ml-2 mb-1 close" data-dismiss="toast" aria-label="Close">
                   <span aria-hidden="true">&times;</span>
                 </button>
               </div>`))
    .append($(`<div class="toast-body"/>`)
      .append($(`<p/>`).text(message))
      .append($(`<pre/>`)
        .append($(`<code/>`).text(stackTrace))
      )
    );
  $('#notificationPanel').append(notification);
  notification.toast({ delay: 30000 });
  notification.toast('show');
}

$(document).ready(function () {
  $('#solveButton').click(solve);

  $('#stopSolvingButton').click(function () {
    stopSolving();
  });

  const callFrequencyRange = $('#callFrequencyRange');
  const callFrequencyValue = $('#callFrequencyValue');
  const callLengthRange = $('#callLengthRange');
  const callLengthValue = $('#callLengthValue');

  callFrequencyRange.on('change', function () {
    callFrequencyValue.html(callFrequencyRange.val());
    restartSimulation(callFrequencyRange.val(), callLengthRange.val());
  });
  callFrequencyRange.on('input', function () {
    callFrequencyValue.html(callFrequencyRange.val());
  });
  callFrequencyValue.html(callFrequencyRange.val());

  callLengthRange.on('change', function () {
    callLengthValue.html(callLengthRange.val());
    restartSimulation(callFrequencyRange.val(), callLengthRange.val());
  });
  callLengthRange.on('input', function () {
    callLengthValue.html(callLengthRange.val());
  });
  callLengthValue.html(callLengthRange.val());

  // Make sure the values are propagated to the server.
  restartSimulation(callFrequencyRange.val(), callLengthRange.val());

  refresh();
});