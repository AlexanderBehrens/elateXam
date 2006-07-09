<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html:html>
<head>
	<title>Aufgaben abgegeben</title>
	<link rel="stylesheet" href="<%= request.getContextPath() %>/format.css" type="text/css">
</head>

<body bgcolor="#FFFFFF" text="#000000">

<jsp:include page="header.jsp" />

<table border="0" cellspacing="6" cellpadding="16">
  <tr>
    <td><html:img page="/pics/info.gif" /></td>
    <td>Ihre L�sung wurde erfolgreich abgegeben.
    	<c:if test="${ReturnURL != null}">
   		    <br/><br/>
    		Zur�ck zur <a href="${ReturnURL}">�bersicht</a>.
    	</c:if>
    </td>
  </tr>
</table>


<jsp:include page="footer.jsp" />

</body>

</html:html>
