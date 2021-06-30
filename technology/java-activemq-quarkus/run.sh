#!/bin/bash

trap ctrl_c INT

function ctrl_c() {
    terminate "$PID_CLIENT" "$PID_SOLVER" "$PID_DOCKER_AMQ"
    exit 0
}

function terminate() {
  for pid in "$@"
  do
    echo "Stopping a process ($pid)"
    kill "$pid"
  done
}

function wait_for_url() {
  local _application_url=$1
  local _timeout_seconds=$2
  local _increment=1

  local _spent=0
  echo "Waiting up to ${_timeout_seconds} seconds for ${_application_url} to become available."
  while [[ "200" != $(curl -LI "${_application_url}" -o /dev/null -w '%{http_code}' -s) && ${_spent} -lt ${_timeout_seconds} ]]
  do
    sleep ${_increment}
    _spent=$((_spent + _increment))
  done
}

function check_directory_exists() {
  local _directory=$1
  local _hint=$2
  if [ ! -d "$_directory" ]; then
  echo "The directory $_directory does not exist. $_hint"
  exit 1
fi
}

readonly solverModule=solver
readonly clientModule=client

check_directory_exists $solverModule/target "Maybe build the project by running \"mvn clean install\"."
check_directory_exists $clientModule/target "Maybe build the project by running \"mvn clean install\"."

if [ ! -d target ]; then
  mkdir target
fi

# Run docker-compose to start ActiveMQ broker.
docker-compose up > target/activemq.log 2>&1 &
readonly PID_DOCKER_AMQ=$!
echo "Running the ActiveMQ broker via docker."

# Run the solver.
mvn clean quarkus:dev -f $solverModule -Dquarkus.http.port="$SOLVER_PORT" -Ddebug=8180 > target/solver.log 2>&1 &
readonly PID_SOLVER=$!
echo "Running the solver."

# Run the client.
mvn clean quarkus:dev -f $clientModule > target/client.log 2>&1 &
readonly PID_CLIENT=$!
echo "Running the client."

# Wait for the application to start up.
readonly URL="http://localhost:8080"
wait_for_url "$URL" 60
echo "Application available at $URL"
echo "Press [Ctrl+C] to exit."
wait
