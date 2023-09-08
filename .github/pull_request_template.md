<!--
Thank you for submitting this pull request.

*Do NOT use the default branch `stable` to create a pull request,
use the branch `development` instead. The latter uses SNAPSHOT versions.*

Please provide all relevant information as outlined below. Feel free to delete
a section if that type of information is not available.

Any changes to school-timetabling must be synced across its quarkus, kotlin-quarkus, and spring-boot variants, 
and also the external https://github.com/quarkusio/quarkus-quickstarts/tree/main/optaplanner-quickstart.
-->

### JIRA

<!-- Add a JIRA ticket link if it exists. -->
<!-- Example: https://issues.redhat.com/browse/PLANNER-1234 -->

### Referenced pull requests

<!-- Add URLs of all referenced pull requests if they exist. This is only required when making
changes that span multiple apache repositories and depend on each other. -->
<!-- Example:
- https://github.com/kiegroup/droolsjbpm-build-bootstrap/pull/1234
- https://github.com/apache/drools/pull/3000
- https://github.com/apache/optaplanner/pull/899
- etc.
-->

<details>
<summary>
How to retest this PR or trigger a specific build:
</summary>

- for <b>pull request checks</b>  
  Please add comment: <b>Jenkins retest this</b>

- for a <b>specific pull request check</b>  
  please add comment: <b>Jenkins (re)run [optaplanner-quickstarts] tests</b>

- for a <b>full downstream build</b>  
  please add the label `run_fdb`

- for <b>quarkus branch checks</b>  
  Run checks against Quarkus current used branch  
  Please add comment: <b>Jenkins run quarkus-branch</b>

- for a <b>quarkus branch specific check</b>  
  Run checks against Quarkus current used branch  
  Please add comment: <b>Jenkins (re)run [optaplanner-quickstarts] quarkus-branch</b>

- for <b>quarkus main checks</b>  
  Run checks against Quarkus main branch  
  Please add comment: <b>Jenkins run quarkus-main</b>

- for a <b>specific quarkus main check</b>  
  Run checks against Quarkus main branch  
  Please add comment: <b>Jenkins (re)run [optaplanner-quickstarts] quarkus-branch</b>

- for <b>quarkus lts checks</b>  
  Run checks against Quarkus lts branch  
  Please add comment: <b>Jenkins run quarkus-lts</b>

- for a <b>specific quarkus lts check</b>  
  Run checks against Quarkus lts branch  
  Please add comment: <b>Jenkins (re)run [optaplanner-quickstarts] quarkus-lts</b>

- for <b>native checks</b>  
  Run native checks  
  Please add comment: <b>Jenkins run native</b>

- for a <b>specific native check</b>  
  Run native checks 
  Please add comment: <b>Jenkins (re)run [optaplanner-quickstarts] native</b>

- for <b>mandrel checks</b>  
  Run native checks against Mandrel image
  Please add comment: <b>Jenkins run mandrel</b>

- for a <b>specific mandrel check</b>  
  Run native checks against Mandrel image  
  Please add comment: <b>Jenkins (re)run [optaplanner-quickstarts] mandrel</b>

- for <b>mandrel lts checks</b>  
  Run native checks against Mandrel image and quarkus lts branch
  Please add comment: <b>Jenkins run mandrel-lts</b>

- for a <b>specific mandrel lts check</b>  
  Run native checks against Mandrel image and quarkus lts branch
  Please add comment: <b>Jenkins (re)run [optaplanner-quickstarts] mandrel-lts</b>
</details>

<details>
<summary>
How to backport a pull request to a different branch?
</summary>

In order to automatically create a **backporting pull request** please add one or more labels having the following format `backport-<branch-name>`, where `<branch-name>` is the name of the branch where the pull request must be backported to (e.g., `backport-7.67.x` to backport the original PR to the `7.67.x` branch).

> **NOTE**: **backporting** is an action aiming to move a change (usually a commit) from a branch (usually the main one) to another one, which is generally referring to a still maintained release branch. Keeping it simple: it is about to move a specific change or a set of them from one branch to another.

Once the original pull request is successfully merged, the automated action will create one backporting pull request per each label (with the previous format) that has been added.

If something goes wrong, the author will be notified and at this point a manual backporting is needed.

> **NOTE**: this automated backporting is triggered whenever a pull request on `main` branch is labeled or closed, but both conditions must be satisfied to get the new PR created.
</details>
