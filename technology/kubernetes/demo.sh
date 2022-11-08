#!/bin/bash

readonly ARTEMIS_CLOUD_VERSION=1.0.5
readonly KEDA_VERSION=2.8.0
readonly KEDA_NAMESPACE=keda
readonly DEMO_NAMESPACE=demo
readonly DEMO_APP_DEPLOYMENT_TIMEOUT_SECONDS=300
# If OptaPlanner Operator needs to run in a different namespace, change the optaplanner-operator.yml accordingly.
readonly OPTAPLANNER_OPERATOR_NAMESPACE=optaplanner-operator

function check_directory_exists() {
  local _directory=$1
  if [ ! -d "$_directory" ]; then
    echo "The directory $_directory does not exist. Maybe build the project by running \"mvn clean install\"."
    exit 1
  fi
}

function assert_oc_tools() {
  if ! oc &> /dev/null ; then
    echo "The OpenShift Client (oc) was not detected. Please make sure that the OpenShift Client (oc) has been properly installed."
    exit 1
  fi
}

function install_keda_if_needed() {
  if  oc get crd scaledobjects.keda.sh && oc get namespace "$KEDA_NAMESPACE" ; then
    return 0
  fi

  oc apply -f "https://github.com/kedacore/keda/releases/download/v$KEDA_VERSION/keda-$KEDA_VERSION.yaml" -n "$KEDA_NAMESPACE"
}

function install_artemis_cloud() {
  local temp_directory=/tmp/artemiscloud
  rm -rf $temp_directory
  mkdir $temp_directory
  local archive_file="$temp_directory/activemq-artemis-operator-$ARTEMIS_CLOUD_VERSION.zip"
  wget -q -O $archive_file "https://github.com/artemiscloud/activemq-artemis-operator/archive/refs/tags/v$ARTEMIS_CLOUD_VERSION.zip"
  unzip -q -d $temp_directory $archive_file
  local artemis_cloud_directory="$temp_directory/activemq-artemis-operator-$ARTEMIS_CLOUD_VERSION"

  # deploy the operator
  oc apply -f "$artemis_cloud_directory/deploy/service_account.yaml"
  oc apply -f "$artemis_cloud_directory/deploy/role.yaml"
  oc apply -f "$artemis_cloud_directory/deploy/role_binding.yaml"
  oc apply -f "$artemis_cloud_directory/deploy/election_role_binding.yaml"
  oc apply -f "$artemis_cloud_directory/deploy/election_role.yaml"
  oc apply -f "$artemis_cloud_directory/deploy/crds/*.yaml"
  oc apply -f "$artemis_cloud_directory/deploy/operator.yaml"
  # start a broker
  oc apply -f artemis-broker.yaml
}

function install_optaplanner_operator_if_needed() {
  if  oc get crd solvers.org.optaplanner.solver && oc get namespace "$OPTAPLANNER_OPERATOR_NAMESPACE" ; then
    return 0
  fi

  local operator_distribution_directory_local=$1

  oc create namespace "$OPTAPLANNER_OPERATOR_NAMESPACE"
  oc apply -f "$operator_distribution_directory_local/crd-solver.yml" -n "$OPTAPLANNER_OPERATOR_NAMESPACE"
  oc apply -f "$operator_distribution_directory_local/optaplanner-operator.yml" -n "$OPTAPLANNER_OPERATOR_NAMESPACE"
}

function deploy_demo_app() {
  local demo_app_directory=$1
  local target_namespace=$2
  local broker_amqp_host_local=$3
  (cd "$demo_app_directory" && mvn -q clean package -DskipTests -Dopenshift -Dquarkus.openshift.namespace="$target_namespace" -Dquarkus.kubernetes-client.trust-certs=true -Dquarkus.openshift.env.vars.amqp-host="$broker_amqp_host_local")
}

function wait_for_route() {
  local route=$1
  local namespace=$2
  local timeout_seconds=$3
  local increment=1
  local spent=0

  echo "Waiting up to ${timeout_seconds} seconds for a route ${route} to become available."
  while [[ ${spent} -lt ${timeout_seconds} ]]
  do
    if oc get route "$route" -n "$namespace" > /dev/null 2>&1 ; then
      break
    fi
    sleep ${increment}
    spent=$((spent + increment))
  done
}

function wait_for_url() {
  local application_url=$1
  local timeout_seconds=$2
  local increment=1

  local spent=0
  echo "Waiting up to ${timeout_seconds} seconds for ${application_url} to become available."
  while [[ "200" != $(curl -LI "${application_url}" -o /dev/null -w '%{http_code}' -s) && ${spent} -lt ${timeout_seconds} ]]
  do
    sleep ${increment}
    spent=$((spent + increment))
  done
}

function setup_demo() {
  cd $(dirname "$0") || exit 1

  readonly school_timetabling_module=school-timetabling
  readonly demo_app_module=demo-app

  check_directory_exists $school_timetabling_module/target
  check_directory_exists $demo_app_module/target

  assert_oc_tools

  readonly operator_distribution_directory="extra/optaplanner-operator"
  operator_distribution_path="../../../../$operator_distribution_directory"
  if [ ! -d "$operator_distribution_path" ]; then
    readonly subdirectories=("$1"/"$operator_distribution_directory")
    operator_distribution_path="${subdirectories[0]}"
    if [ ! -d "$operator_distribution_path" ]; then
      echo "Provide a path to the OptaPlanner distribution directory."
      exit 1
    fi
  fi

  # Prepare the demo namespace.
  if oc get namespace $DEMO_NAMESPACE ; then
    read -p "The namespace ($DEMO_NAMESPACE) already exists. The script will erase and re-create it. Do you want to continue [y/n]?" -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]
    then
      echo "Aborting..."
      exit 1
    fi
  fi

  oc delete namespace --wait=true --ignore-not-found=true "$DEMO_NAMESPACE"
  oc create namespace "$DEMO_NAMESPACE"
  oc project $DEMO_NAMESPACE

  # Install the required operators.
  install_keda_if_needed
  install_artemis_cloud "$DEMO_NAMESPACE"
  install_optaplanner_operator_if_needed "$operator_distribution_path"
  readonly broker_amqp_host="ex-aao-amqp-0-svc.$DEMO_NAMESPACE.svc.cluster.local"
  readonly broker_management_host="ex-aao-hdls-svc.$DEMO_NAMESPACE.svc.cluster.local"

  # Deploy and wait for the Demo app.
  deploy_demo_app "$demo_app_module" "$DEMO_NAMESPACE" "$broker_amqp_host"
  wait_for_route demo-app $DEMO_NAMESPACE $DEMO_APP_DEPLOYMENT_TIMEOUT_SECONDS
  readonly demo_app_host=$(oc get route demo-app -n "$DEMO_NAMESPACE" -o custom-columns=HOST/PORT:.spec.host --no-headers=true)
  readonly demo_app_url="http://$demo_app_host"
  wait_for_url "$demo_app_url" $DEMO_APP_DEPLOYMENT_TIMEOUT_SECONDS

  echo ""
  echo "******************************************************************************"
  echo "Demo application available at $demo_app_url."
  echo "Artemis AMQP host: $broker_amqp_host"
  echo "Artemis Management host: $broker_management_host"
  echo "******************************************************************************"
}

function delete_demo() {
  assert_oc_tools

  readonly  prompt="The following namespaces are going to be deleted: ($DEMO_NAMESPACE, $OPTAPLANNER_OPERATOR_NAMESPACE, $KEDA_NAMESPACE). Do you want to continue [y/n]?"
  read -p "$prompt" -n 1 -r
  echo
  if [[ ! $REPLY =~ ^[Yy]$ ]]
  then
    echo "Aborting..."
    exit 1
  fi

  oc delete namespace "$DEMO_NAMESPACE"
  oc delete namespace "$OPTAPLANNER_OPERATOR_NAMESPACE"
  # Required to delete the KEDA namespace.
  oc delete apiservices v1beta1.external.metrics.k8s.io
  oc delete namespace "$KEDA_NAMESPACE"
}

function print_help() {
  echo "Usage: ./demo.sh [setup [path] | delete]"
  echo
  echo "Examples:"
  echo "./demo.sh setup [path]  Installs the required operators and deploys the Demo application."
  echo "                        If you run the script from the OptaPlanner distribution, execute './demo.sh setup'."
  echo "                        Otherwise, provide also a path to the OptaPlanner distribution (1)."
  echo
  echo "./demo.sh delete        Removes all the resources created by 'demo.sh setup' from the OpenShift cluster."
  echo
  echo "(1) Download the distribution from https://www.optaplanner.org, unzip the archive and provide a path to the resulting directory."
}

if [[ $1 == "delete" ]]; then
  delete_demo
elif [[ $1 == "setup" ]]; then
  setup_demo "$2"
else
  print_help
  exit 1
fi

