<%@ include file="/common/taglibs.jsp"%>

<head>
    <title><fmt:message key="mainMenu.title"/></title>
    <content tag="heading">Hallo <authz:authentication operation="fullName"/>!</content>
    <meta name="menu" content="MainMenu"/>
</head>

<p>In der folgenden Liste finden Sie alle verf�gbaren Aufgaben zur Korrektur. Klicken Sie auf den Titel der Aufgabe, um zur Korrektur-�bersicht zu gelangen.</p>

	<display:table partialList="false" name="TaskDefs" uid="row" pagesize="30" sort="list" class="table">
		<display:column title="Name&nbsp;&nbsp;&nbsp;" sortable="true">
			<html:link action="/CorrectorFactory" paramId="taskId" paramName="row" paramProperty="id"><c:out value="${row.title}"/></html:link>
		</display:column>
		<display:column property="type" title="Typ&nbsp;&nbsp;&nbsp;" sortable="true"/>
		<display:column property="shortDescription" title="Kurzbeschreibung&nbsp;&nbsp;&nbsp;" sortable="true"/>
	</display:table>