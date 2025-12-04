# Process Examples 

This document provides an overview of the main process templates and how to trigger and control them via REST API.

> **Note:** Make sure all required services are started before trying out the process examples. This ensures the API calls work as expected.

For the shown example endpoints, see the Swagger UI:
`http://localhost:8082/jme-process-context-app-service/swagger-ui.html`

The PCS UI can be accessed at:
`http://localhost:8080/process-context/startpage`

---

## Race Process Example

This example demonstrates the `raceProcess.json` template, which models a race with multiple stages, events, and relations.

To test this process, open the Swagger UI and select the 'Race Process API'. Follow these steps:

1) **Create a race process**
```http
POST /jme-process-context-app-service/api/raceprocess/{processId}/createProcess?processCreationType=REST

{
  "raceCarNumber": "{processId}"
}
```
This creates a new race process instance. The process will be in the STARTED state.

2) **Start the race**
```http
POST /jme-process-context-app-service/api/raceprocess/{processId}/raceStarted?weatherAlertSubject={processId}
```
This triggers the race start event and completes the 'Start Race' task.

3) **Pass control points**
```http
POST /jme-process-context-app-service/api/raceprocess/{processId}/raceControlpointPassed?controlPoint=Bern
POST /jme-process-context-app-service/api/raceprocess/{processId}/raceControlpointPassed?controlPoint=Brig
POST /jme-process-context-app-service/api/raceprocess/{processId}/raceControlpointPassed?controlPoint=Chur
```
Each call marks a control point as passed and creates a relation between the race car and the control point. You can view these relations in the Process Context Service UI under 'Relations'.

4) **Validate the race**
```http
POST /jme-process-context-app-service/api/raceprocess/{processId}/raceValidated
```
Completes the 'Validate Race' task.

5) **Reach the destination**
```http
POST /jme-process-context-app-service/api/raceprocess/{processId}/raceDestinationReached
```
Completes the 'Reach Destination' milestone and triggers a process snapshot.

6) **Complete refuelling**
```http
POST /jme-process-context-app-service/api/raceprocess/{processId}/carRefuellingCompleted
```
Completes the 'Car Refuelling' task.

7) **Process completion**
   The process will automatically complete when all required tasks and milestones are finished. Another snapshot is created at completion. You can view the process state and snapshots in the UI.

**Relations:**
- Each control point passed creates a relation between the race car and the control point.
- The process context UI will show these relations under the 'Relations' section.

**Snapshots:**
- Snapshots are created when the destination is reached and when the process is completed.

---

## Doping Process Example

This example demonstrates the `dopingProcess.json` template, which is automatically triggered when a race process reaches its destination.

To test this process, use the Swagger UI and follow these steps:

1) **Trigger the doping process**
```http
POST /jme-process-context-app-service/api/raceprocess/{processId}/raceDestinationReached
```
The doping process is instantiated by the event `JmeRaceDestinationReachedEvent`.

2) **Doping process instantiation**
   A new doping process instance is created and linked to the originating race process via a relation (see 'processRelationToRaceProcess' in the template). You can view this relation in the Process Context Service UI under 'Relations'.

3) **Process context UI**
   Open the process context UI and view the doping process. Under 'Relations', you will see the link to the race process, with the defined roles and visibility.

**Notes:**
- The doping process does not require manual task completion; it is mainly for tracking and relation.
- Visibility and roles are defined in the process template.

---

## Document Review Process

The Document Review Process documents the review and approval of documents. The process definition can be found in `documentReviewProcess.json`.

### REST API Calls

**Create process:**
```http
POST /document-api/{processId}/createProcess
Content-Type: application/json

{
  "documentId": "myDocumentId",
  "title": "The Lord of the Rings",
  "author": "JRR Tolkien"
}
```

**Create document version:**
```http
POST /document-api/{processId}/createDocumentVersion
Content-Type: application/json

{
  "versionId": "v1",
  "versionNumber": "1",
  "changes": "first version"
}
```

**Create document version review:**
```http
POST /document-api/{processId}/createDocumentVersionReview
Content-Type: application/json

{
  "reviewId": "rev1",
  "versionId": "v1",
  "versionNumber": "1",
  "status": "OK",
  "comments": "Good book"
}
```

---

## 'JoinType' example: Document Review Process

This example demonstrates the 'joinType' property on RelationPatterns with a new process template (documentReviewProcess.json).

It is recommended to test it out locally, as it requires Process Template modifications.

In order to try it out, you can open the Swagger UI and select the 'Document Review API' in the top-right corner. Then follow the next steps:

1) Start by creating a process with:
```http
POST /document-api/{processId}/createProcess 

{
  "documentId": "myDocumentId",
  "title": "The Lord of the Rings",
  "author": "JRR Tolkien"
}
```

2) Create two versions of the document with :
```http
POST /createDocumentVersion and payloads

{
  "versionId": "v1",
  "versionNumber": "1",
  "changes": "first version"
}
```
```http
POST /createDocumentVersion and payloads

{
  "versionId": "v2",
  "versionNumber": "2",
  "changes": "second version"
}
```
The `versionNumber` properties will be mapped to the 'Role' of ProcessData and will be used to in the `joinType` clause.

3) Now we can demonstrate how the Process Template join clause (`"joinType": "byRole"`) works by creating the first
document version review. Execute:

```http
POST /createDocumentVersionReview

{
  "reviewId": "rev1",
  "versionId": "v1",
  "versionNumber": "1",
  "status": "OK",
  "comments": "Good book"
}
```
Again, the `versionNumber` will be mapped to Role and will be used for the join. Once the request has completed, you can 
navigate to the Process Context Service UI and open the corresponding process. When you expand the 'Relations' section,
you will only see a Relation with SubjectId:'rev1' and ObjectId:'v1'.

That relation has been created when the document review event was received. As the RelationPattern has been configured 
to be joined by role, then only one Relation has been created for rev1 and v1, because both have the Role value '1'.

In this use case, it seems logical that a review points only to a version. But what would have happened without the 
joinType clause? Let's see it in next step.

4) Now remove the `joinType` line in `documentReviewProcess.json` and do not forget the comma of the previous line. Restart PCS. 

Create a **new** process and perform the same REST requests as in previous steps. Once done, you will 
notice two created relations in PCS UI. Since we removed the `joinType` configuration, PCS created the Relations with 
the default behaviour (cartesian product), generating an additional entry that shouldn't be there as part of the use case.
