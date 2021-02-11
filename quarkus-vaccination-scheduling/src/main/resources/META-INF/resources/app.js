var autoRefreshIntervalId = null;
var vaccineCenterLeafletGroup = null;
var personLeafletGroup = null;

function refreshSolution() {
  $.getJSON("/vaccinationSchedule", function (scheduleVisualization) {
    refreshSolvingButtons(scheduleVisualization.solverStatus != null && scheduleVisualization.solverStatus !== "NOT_SOLVING");
    $("#score").text("Score: " + (scheduleVisualization.score == null ? "?" : scheduleVisualization.score));

    const vaccineTypesDiv = $("#vaccineTypes");
    vaccineTypesDiv.children().remove();
    scheduleVisualization.vaccineTypeList.forEach((vaccineType) => {
      const color = pickColor(vaccineType.name);
      vaccineTypesDiv.append($(`<div class="card" style="background-color: ${color}"/>`)
          .append($(`<div class="card-body p-2"/>`)
            .append($(`<h5 class="card-title mb-0"/>`).text(vaccineType.name))));
    });
    const vaccineTypeMap = new Map(scheduleVisualization.vaccineTypeList.map(vaccineType => [vaccineType.name, vaccineType]));

    const scheduleTable = $("#scheduleTable");
    scheduleTable.children().remove();
    const unassignedPeronsDiv = $("#unassignedPersons");
    unassignedPeronsDiv.children().remove();

    const thead = $("<thead>").appendTo(scheduleTable);
    const headerRow = $("<tr>").appendTo(thead);
    headerRow.append($("<th>Timeslot</th>"));
    scheduleVisualization.vaccinationCenterList.forEach((vaccinationCenter) => {
      for (lineIndex = 0; lineIndex < vaccinationCenter.lineCount; lineIndex++) {
        headerRow
          .append($("<th/>")
            .append($("<span/>").text(vaccinationCenter.name + (vaccinationCenter.lineCount <= 1 ? "" : " line " + lineIndex))));
      }
    });

    const appointmentMap = new Map(scheduleVisualization.appointmentList
      .map(appointment => [appointment.dateTime + "/" + appointment.vaccinationCenter.id + "/" + appointment.lineIndex, appointment]));

    const tbody = $(`<tbody>`).appendTo(scheduleTable);
    var previousParsedDateTime = null;
    scheduleVisualization.dateTimeList.forEach((dateTime) => {
      const row = $(`<tr>`).appendTo(tbody);
      var parsedDateTime = moment(dateTime, "YYYY,M,D,H,m");
      var showDate = (previousParsedDateTime == null || !parsedDateTime.isSame(previousParsedDateTime, "day"));
      row
        .append($(`<th class="align-middle"/>`)
          .append($(`<span style="float: right"/>`).text(showDate ? parsedDateTime.format("ddd MMM D HH:mm") : parsedDateTime.format("HH:mm"))));
      previousParsedDateTime = parsedDateTime;
      scheduleVisualization.vaccinationCenterList.forEach((vaccinationCenter) => {
        for (lineIndex = 0; lineIndex < vaccinationCenter.lineCount; lineIndex++) {
          var appointment = appointmentMap.get(dateTime + "/" + vaccinationCenter.id + "/" + lineIndex);
          if (appointment == null) {
            row.append($(`<td class="p-1"/>`));
          } else {
            const color = pickColor(appointment.vaccineType);
            var cardBody = $(`<div class="card-body pt-1 pb-1 pl-2 pr-2"/>`);
            if (appointment.person == null) {
              cardBody.append($(`<h5 class="card-title mb-0"/>`).text("Unassigned"));
            } else {
              cardBody.append($(`<h5 class="card-title mb-1"/>`)
                .text(appointment.person.name + " (" + appointment.person.age + ")"));
              const vaccineType = vaccineTypeMap.get(appointment.vaccineType);
              if (vaccineType.maximumAge != null && appointment.person.age > vaccineType.maximumAge) {
                cardBody.append($(`<p class="badge badge-danger mb-0"/>`).text(vaccineType.name + " maximum age is " + vaccineType.maximumAge));
              }
              if (!appointment.person.firstDoseInjected) {
                cardBody.append($(`<p class="card-text ml-2 mb-0"/>`).text("1st dose"));
              } else {
                var firstDoseDate = moment(appointment.person.firstDoseDate, "YYYY,M,D");
                if (appointment.vaccineType !== appointment.person.firstDoseVaccineType) {
                  cardBody.append($(`<p class="badge badge-danger ml-2 mb-0"/>`).text("1st dose was " + appointment.person.firstDoseVaccineType));
                }
                var appointmentDateTime = moment(appointment.dateTime, "YYYY,M,D,H,m");
                var secondDoseReadyDate = firstDoseDate.clone().add(vaccineType.secondDoseReadyDays, 'days');
                var readyDateDiff = appointmentDateTime.diff(secondDoseReadyDate, 'days');
                if (readyDateDiff < 0) {
                  cardBody.append($(`<p class="badge badge-danger ml-2 mb-0"/>`).text("2nd dose is " + (-readyDateDiff) + " days too early"));
                }
                var secondDoseIdealDate = firstDoseDate.clone().add(vaccineType.secondDoseIdealDays, 'days');
                var idealDateDiff = appointmentDateTime.diff(secondDoseIdealDate, 'days');
                cardBody.append($(`<p class="card-text ml-2 mb-0"/>`).text("2nd dose ("
                  + (idealDateDiff === 0 ? "ideal day"
                    : (idealDateDiff < 0 ? (-idealDateDiff) + " days too early"
                      : idealDateDiff + " days too late")) + ")"));
              }
            }
            row.append($(`<td class="p-1"/>`)
              .append($(`<div class="card" style="background-color: ${color}"/>`)
                .append(cardBody)));
          }
        }
      });
    });


    vaccineCenterLeafletGroup.clearLayers();
    scheduleVisualization.vaccinationCenterList.forEach((vaccinationCenter) => {
      L.marker(vaccinationCenter.location).addTo(vaccineCenterLeafletGroup);
    });

    personLeafletGroup.clearLayers();
    scheduleVisualization.personList.forEach((person) => {
      const vaccinationSlot = person.vaccinationSlot;
      const personColor = (vaccinationSlot == null ? "gray" : pickColor(vaccinationSlot.vaccineType));
      L.circleMarker(person.homeLocation, {radius: 4, color: personColor, weight: 2}).addTo(personLeafletGroup);
      if (vaccinationSlot != null) {
        L.polyline([person.homeLocation, vaccinationSlot.vaccinationCenter.location], {color: personColor, weight: 1}).addTo(personLeafletGroup);
      } else {
        unassignedPeronsDiv.append($(`<div class="card"/>`)
            .append($(`<div class="card-body pt-1 pb-1 pl-2 pr-2"/>`)
              .append($(`<h5 class="card-title mb-1"/>`).text(person.name + " (" + person.age + ")"))
              .append($(`<p class="card-text ml-2"/>`).text(
                person.firstDoseInjected
                ? "2nd dose (ideally " + moment(person.secondDoseIdealDate, "YYYY,M,D").format("ddd MMM D") + ")"
                : "1st dose"))));
      }
    });
  });
}

function solve() {
  $.post("/vaccinationSchedule/solve", function () {
    refreshSolvingButtons(true);
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Start solving failed.", xhr);
  });
}

function refreshSolvingButtons(solving) {
  if (solving) {
    $("#solveButton").hide();
    $("#stopSolvingButton").show();
    if (autoRefreshIntervalId == null) {
      autoRefreshIntervalId = setInterval(refreshSolution, 2000);
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

function stopSolving() {
  $.post("/vaccinationSchedule/stopSolving", function () {
    refreshSolvingButtons(false);
    refreshSolution();
  }).fail(function (xhr, ajaxOptions, thrownError) {
    showError("Stop solving failed.", xhr);
  });
}

function showError(title, xhr) {
  const serverErrorMessage = !xhr.responseJSON ? `${xhr.status}: ${xhr.statusText}` : xhr.responseJSON.message;
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
  notification.toast("show");
}

$(document).ready(function () {
  $.ajaxSetup({
    headers: {
      "Content-Type": "application/json",
      "Accept": "application/json"
    }
  });
  // Extend jQuery to support $.put() and $.delete()
  jQuery.each(["put", "delete"], function (i, method) {
    jQuery[method] = function (url, data, callback, type) {
      if (jQuery.isFunction(data)) {
        type = type || callback;
        callback = data;
        data = undefined;
      }
      return jQuery.ajax({
        url: url,
        type: method,
        dataType: type,
        data: data,
        success: callback
      });
    };
  });

  $("#refreshButton").click(function () {
    refreshSolution();
  });
  $("#solveButton").click(function () {
    solve();
  });
  $("#stopSolvingButton").click(function () {
    stopSolving();
  });

  const leafletMap = L.map("leafletMap", {doubleClickZoom: false})
    .setView([33.75, -84.40], 10);

  L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
  }).addTo(leafletMap);
  $(`a[data-toggle="tab"]`).on("shown.bs.tab", function (e) {
    leafletMap.invalidateSize();
  })

  vaccineCenterLeafletGroup = L.layerGroup();
  vaccineCenterLeafletGroup.addTo(leafletMap);
  personLeafletGroup = L.layerGroup();
  personLeafletGroup.addTo(leafletMap);

  refreshSolution();
});

// ****************************************************************************
// TangoColorFactory
// ****************************************************************************

const SEQUENCE_1 = [0x8AE234, 0xFCE94F, 0x729FCF, 0xE9B96E, 0xAD7FA8];
const SEQUENCE_2 = [0x73D216, 0xEDD400, 0x3465A4, 0xC17D11, 0x75507B];

var colorMap = new Map;
var nextColorCount = 0;

function pickColor(object) {
  let color = colorMap[object];
  if (color !== undefined) {
    return color;
  }
  color = nextColor();
  colorMap[object] = color;
  return color;
}

function nextColor() {
  let color;
  let colorIndex = nextColorCount % SEQUENCE_1.length;
  let shadeIndex = Math.floor(nextColorCount / SEQUENCE_1.length);
  if (shadeIndex === 0) {
    color = SEQUENCE_1[colorIndex];
  } else if (shadeIndex === 1) {
    color = SEQUENCE_2[colorIndex];
  } else {
    shadeIndex -= 3;
    let floorColor = SEQUENCE_2[colorIndex];
    let ceilColor = SEQUENCE_1[colorIndex];
    let base = Math.floor((shadeIndex / 2) + 1);
    let divisor = 2;
    while (base >= divisor) {
      divisor *= 2;
    }
    base = (base * 2) - divisor + 1;
    let shadePercentage = base / divisor;
    color = buildPercentageColor(floorColor, ceilColor, shadePercentage);
  }
  nextColorCount++;
  return "#" + color.toString(16);
}

function buildPercentageColor(floorColor, ceilColor, shadePercentage) {
  let red = (floorColor & 0xFF0000) + Math.floor(shadePercentage * ((ceilColor & 0xFF0000) - (floorColor & 0xFF0000))) & 0xFF0000;
  let green = (floorColor & 0x00FF00) + Math.floor(shadePercentage * ((ceilColor & 0x00FF00) - (floorColor & 0x00FF00))) & 0x00FF00;
  let blue = (floorColor & 0x0000FF) + Math.floor(shadePercentage * ((ceilColor & 0x0000FF) - (floorColor & 0x0000FF))) & 0x0000FF;
  return red | green | blue;
}
