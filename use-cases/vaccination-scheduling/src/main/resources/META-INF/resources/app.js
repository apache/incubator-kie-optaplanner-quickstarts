var autoRefreshIntervalId = null;
var vaccineCenterLeafletGroup = null;
var personLeafletGroup = null;

function refreshSolution() {
  $.getJSON("/vaccinationSchedule?page=0", function (schedule) {
    refreshSolvingButtons(schedule.solverStatus != null && schedule.solverStatus !== "NOT_SOLVING");
    $("#score").text("Score: " + (schedule.score == null ? "?" : schedule.score));

    const vaccineTypesDiv = $("#vaccineTypes");
    vaccineTypesDiv.children().remove();
    const vaccineTypeMap = new Map();
    schedule.vaccineTypeList.forEach((vaccineType) => {
      const color = pickColor(vaccineType.name);
      vaccineTypesDiv.append($(`<div class="card" style="background-color: ${color}"/>`)
          .append($(`<div class="card-body p-2"/>`)
            .append($(`<h5 class="card-title mb-0"/>`).text(vaccineType.name))));
      vaccineTypeMap.set(vaccineType.name, vaccineType);
    });

    const scheduleTable = $("#scheduleTable");
    scheduleTable.children().remove();
    vaccineCenterLeafletGroup.clearLayers();
    const unassignedPeronsDiv = $("#unassignedPersons");
    unassignedPeronsDiv.children().remove();

    if (schedule.appointmentList.size > 10000) {
      scheduleTable.append($(`<p/>`)
        .text("There are " + schedule.appointmentList.size + " appointments. Too much data to show a schedule."));
      return;
    }


    const vaccinationCenterMap = new Map(
      schedule.vaccinationCenterList.map(vaccinationCenter => [vaccinationCenter.id, vaccinationCenter]));

    const dateTimeSet = new Set();
    const dateTimeList = [];
    const vaccinationCenterIdToBoothIdSetMap = new Map(
      schedule.vaccinationCenterList.map(vaccinationCenter => [vaccinationCenter.id, new Set()]));
    schedule.appointmentList.forEach((appointment) => {
      const dateTime = moment(appointment.dateTime, "YYYY,M,D,H,m");
      const dateTimeString = dateTime.format("YYYY MMM D HH:mm")
      if (!dateTimeSet.has(dateTimeString)) {
        dateTimeSet.add(dateTimeString);
        dateTimeList.push(dateTime);
      }
      vaccinationCenterIdToBoothIdSetMap.get(appointment.vaccinationCenter).add(appointment.boothId);
    });
    dateTimeList.sort((a, b) => a.unix() - b.unix());

    const thead = $("<thead>").appendTo(scheduleTable);
    const headerRow = $("<tr>").appendTo(thead);
    headerRow.append($("<th>Time</th>"));
    schedule.vaccinationCenterList.forEach((vaccinationCenter) => {
      const boothIdSet = vaccinationCenterIdToBoothIdSetMap.get(vaccinationCenter.id);
      boothIdSet.forEach((boothId) => {
        headerRow
          .append($("<th/>")
            .append($("<span/>").text(vaccinationCenter.name + (boothIdSet.size <= 1 ? "" : " booth " + boothId))));
      });
    });

    const appointmentMap = new Map(schedule.appointmentList
      .map(appointment => [moment(appointment.dateTime, "YYYY,M,D,H,m") + "/" + appointment.vaccinationCenter + "/" + appointment.boothId, appointment]));
    if (schedule.appointmentList.length !== appointmentMap.size) {
      var conflicts = schedule.appointmentList.length - appointmentMap.size;
      scheduleTable.append($(`<p class="badge badge-danger">There are ${conflicts} double bookings.</span>`));
    }
    const appointmentToPersonMap = new Map();
    schedule.personList.forEach((person) => {
      if (person.appointment != null) {
        appointmentToPersonMap.set(moment(person.appointment.dateTime, "YYYY,M,D,H,m") + "/" + person.appointment.vaccinationCenter + "/" + person.appointment.boothId, person);
      }
    });

    const tbody = $(`<tbody>`).appendTo(scheduleTable);
    var previousDateTime = null;
    dateTimeList.forEach((dateTime) => {
      const row = $(`<tr>`).appendTo(tbody);
      var showDate = (previousDateTime == null || !dateTime.isSame(previousDateTime, "day"));
      row
        .append($(`<th class="align-middle"/>`)
          .append($(`<span style="float: right"/>`).text(showDate ? dateTime.format("ddd MMM D HH:mm") : dateTime.format("HH:mm"))));
      previousDateTime = dateTime;
      schedule.vaccinationCenterList.forEach((vaccinationCenter) => {
        const boothIdSet = vaccinationCenterIdToBoothIdSetMap.get(vaccinationCenter.id);
        boothIdSet.forEach((boothId) => {
          var appointment = appointmentMap.get(dateTime + "/" + vaccinationCenter.id + "/" + boothId);
          if (appointment == null) {
            row.append($(`<td class="p-1"/>`));
          } else {
            const color = pickColor(appointment.vaccineType);
            var cardBody = $(`<div class="card-body pt-1 pb-1 pl-2 pr-2"/>`);
            const person = appointmentToPersonMap.get(dateTime + "/" + vaccinationCenter.id + "/" + boothId);
            if (person == null) {
              cardBody.append($(`<h5 class="card-title mb-0"/>`).text("Unassigned"));
            } else {
              var appointmentDateTime = moment(appointment.dateTime, "YYYY,M,D,H,m");
              var birthdate = moment(person.birthdate, "YYYY,M,D");
              var age = appointmentDateTime.diff(birthdate, 'years')
              cardBody.append($(`<h5 class="card-title mb-1"/>`)
                .text(person.name + " (" + age + ")"));
              const vaccineType = vaccineTypeMap.get(appointment.vaccineType);
              if (vaccineType.maximumAge != null && age > vaccineType.maximumAge) {
                cardBody.append($(`<p class="badge badge-danger mb-0"/>`).text(vaccineType.name + " maximum age is " + vaccineType.maximumAge));
              }
              if (person.requiredVaccineType != null
                && appointment.vaccineType !== person.requiredVaccineType) {
                cardBody.append($(`<p class="badge badge-danger ml-2 mb-0"/>`).text("Required vaccine is " + person.requiredVaccineType));
              }
              if (person.preferredVaccineType != null
                && appointment.vaccineType !== person.preferredVaccineType) {
                cardBody.append($(`<p class="badge badge-warning ml-2 mb-0"/>`).text("Preferred vaccine is " + person.preferredVaccineType));
              }
              if (person.requiredVaccinationCenter != null
                && appointment.vaccinationCenter !== person.requiredVaccinationCenter) {
                const requiredVaccinationCenter = vaccinationCenterMap.get(person.requiredVaccinationCenter);
                cardBody.append($(`<p class="badge badge-danger ml-2 mb-0"/>`).text("Required vaccination center is " + requiredVaccinationCenter.name));
              }
              if (person.preferredVaccinationCenter != null
                && appointment.vaccinationCenter !== person.preferredVaccinationCenter) {
                const preferredVaccinationCenter = vaccinationCenterMap.get(person.preferredVaccinationCenter);
                cardBody.append($(`<p class="badge badge-warning ml-2 mb-0"/>`).text("Preferred vaccination center is " + preferredVaccinationCenter.name));
              }
              if (person.readyDate != null) {
                var readyDate = moment(person.readyDate, "YYYY,M,D");
                var readyDateDiff = appointmentDateTime.diff(readyDate, 'days');
                if (readyDateDiff < 0) {
                  cardBody.append($(`<p class="badge badge-danger ml-2 mb-0"/>`).text("Dose is " + (-readyDateDiff) + " days too early"));
                }
              }
              if (person.dueDate != null) {
                var dueDate = moment(person.dueDate, "YYYY,M,D");
                var dueDateDiff = appointmentDateTime.diff(dueDate, 'days');
                if (dueDateDiff > 0) {
                  cardBody.append($(`<p class="badge badge-danger ml-2 mb-0"/>`).text("Dose is " + (dueDateDiff) + " days too late"));
                }
              }
              var dosePrefix = person.doseNumber.toString() + ((person.doseNumber === 1) ? "st" : "nd");
              var doseSuffix = "";
              if (person.idealDate != null) {
                var idealDate = moment(person.idealDate, "YYYY,M,D");
                var idealDateDiff = appointmentDateTime.diff(idealDate, 'days');
                doseSuffix = " (" + (idealDateDiff === 0 ? "ideal day"
                    : (idealDateDiff < 0 ? (-idealDateDiff) + " days too early"
                    : idealDateDiff + " days too late")) + ")";
              }
              cardBody.append($(`<p class="card-text ml-2 mb-0"/>`).text(dosePrefix + " dose" + doseSuffix));
            }
            row.append($(`<td class="p-1"/>`)
              .append($(`<div class="card" style="background-color: ${color}"/>`)
                .append(cardBody)));
          }
        });
      });
    });


    schedule.vaccinationCenterList.forEach((vaccinationCenter) => {
      L.marker(vaccinationCenter.location).addTo(vaccineCenterLeafletGroup);
    });
    var assignedPersonCount = 0;
    var unassignedPersonCount = 0;
    personLeafletGroup.clearLayers();
    schedule.personList.forEach((person) => {
      const appointment = person.appointment;
      const personColor = (appointment == null ? "gray" : pickColor(appointment.vaccineType));
      L.circleMarker(person.homeLocation, {radius: 4, color: personColor, weight: 2}).addTo(personLeafletGroup);
      if (person.requiredVaccineType != null) {
        const requiredVaccineTypeColor = pickColor(person.requiredVaccineType);
        L.circleMarker(person.homeLocation, {radius: 3, color: requiredVaccineTypeColor, weight: 0, fillOpacity: 1.0}).addTo(personLeafletGroup);
      }
      if (appointment != null) {
        assignedPersonCount++;
        const vaccinationCenter = vaccinationCenterMap.get(appointment.vaccinationCenter);
        L.polyline([person.homeLocation, vaccinationCenter.location], {color: personColor, weight: 1}).addTo(personLeafletGroup);
      } else {
        unassignedPersonCount++;
        var firstDateTime = dateTimeList[0];
        var birthdate = moment(person.birthdate, "YYYY,M,D");
        var age = firstDateTime.diff(birthdate, 'years');

        var dosePrefix = person.doseNumber.toString() + ((person.doseNumber === 1) ? "st" : "nd");
        var doseSuffix = "";
        if (person.requiredVaccineType != null) {
          const vaccineType = vaccineTypeMap.get(person.requiredVaccineType);
          doseSuffix += " " + vaccineType.name;
        }
        if (person.idealDate != null) {
          doseSuffix += " (ideally " + moment(person.idealDate, "YYYY,M,D").format("ddd MMM D") + ")";
        }
        unassignedPeronsDiv.append($(`<div class="card"/>`)
            .append($(`<div class="card-body pt-1 pb-1 pl-2 pr-2"/>`)
              .append($(`<h5 class="card-title mb-1"/>`).text(person.name + " (" + age + ")"))
              .append($(`<p class="card-text ml-2"/>`).text(dosePrefix + " dose" + doseSuffix))));
      }
    });
    $("#assignedPersonCount").text(assignedPersonCount);
    $("#unassignedPersonCount").text(unassignedPersonCount);
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
