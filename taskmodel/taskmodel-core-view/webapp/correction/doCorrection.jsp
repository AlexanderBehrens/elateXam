<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://displaytag.sf.net" prefix="display"%>

<html:html>
<head>
	<title>Korrektur</title>
	<link rel="stylesheet" href="<%= request.getContextPath() %>/format.css" type="text/css">
</head>

<body bgcolor="#FFFFFF" text="#000000">

<jsp:include page="../header.jsp" />

<p class="header">L&ouml;sung von ${Correction.userId}</p>

<form method="post" action="<html:rewrite action="/saveCorrection"/>">
							<input type="hidden" name="userId" value="${Correction.userId}"/>
							<input type="hidden" name="taskId" value="${Correction.taskId}"/>
							<input type="hidden" name="selectedSubTaskletNum" value="${Correction.subTasklet.virtualSubTaskletNumber}"/>

<table border="0" cellspacing="2" cellpadding="2" width="790">
  <tr bgcolor="#F2F9FF"> 
    <td colspan="2" valign="top"><img src="<%= request.getContextPath() %>/pics/exit.gif" width="20" height="16"> 
    	<html:link action="/tutorCorrectionOverview" paramId="taskId" paramName="Correction" paramProperty="taskId">Korrektur-Übersicht</html:link>
	</td>
  </tr>
  <tr> 
    <td width="155" valign="top" bgcolor="#F2F9FF"> <fieldset><legend>Aufgaben</legend> 
      <img src="<%= request.getContextPath() %>/pics/pixel.gif" width="155" height="1"><br>

	<jsp:include page="../dhtmlTree.jsp"/>

 </fieldset> </td>
    <td valign="top" bgcolor="#F2F9FF">
    
    	<c:if test="${Correction.subTasklet != null}">

			<fieldset class="complexTask"><legend>Korrektur - Aufgabe ${Correction.subTasklet.virtualSubTaskletNumber}</legend>

				<table width="100%" cellspacing="0" cellpadding="0">
					<tr>
						<td align="center" valign="top" class="ComplexTaskHint">
							<c:if test="${Correction.subTasklet.hint != null}">
								${Correction.subTasklet.hint}
							</c:if>
						</td>
						<td valign="top" align="right" class="ComplexTaskHint">
							${Correction.subTasklet.reachablePoints} Punkt<c:if test="${Correction.subTasklet.reachablePoints != 1}">e</c:if>
						</td>
					</tr>
				</table>
				<br>
				<br>
				<div class="problem">
					${Correction.subTasklet.problem}
				</div>
				
				<br><br>

				<c:choose>
					<c:when test="${Correction.subTasklet.correctionHTML != null}">
					        	
							${Correction.subTasklet.correctionHTML}
							
							<br><hr size="1" noshade><div align="right">
							<br>
							</div>
							
					</c:when>
					<c:otherwise>			
							${Correction.subTasklet.correctedHTML}
					</c:otherwise>				
				</c:choose>					

				<c:if test="${Correction.subTasklet.corrected}">
					<br><font color="red">Aufgabe korrigiert, erreichte Punkte:
						${Correction.subTasklet.points}
					</font>
				</c:if>
				
				<c:if test="${Correction.subTasklet.correctionHint != null}">
					<br><br><b>Lösungserwartung:</b><br>
				    ${Correction.subTasklet.correctionHint}
				    <br>				
				</c:if>				
				
				<br>
		</fieldset>
		<br>
	</c:if>

 </td>
  </tr>
  <tr> 
    <td colspan="2" valign="top" bgcolor="#F2F9FF">
	<table border="0" cellspacing="2" cellpadding="2">

  <tr>
          <td valign="top"><fieldset><legend>Korrektur-Bemerkungen</legend> 
          
        <div align="right">
                <textarea name="annotation" cols="50" rows="7" class="annotation">${Correction.annotation}</textarea>
          <br>
          <br>

        </div>
      </fieldset></td>
          <td valign="top"> <fieldset><legend>Daten</legend> 
            <table border="0" cellspacing="2" cellpadding="2">
              <tr> 
                <td>Status:</td>
                <td>${Correction.status}</td>

              </tr>
              <tr> 
                <td>Punkte:</td>
                <td>${Correction.points}</td>
              </tr>
              <tr>
                <td>Aktuell zugeordneter Korrektor:</td>
                <td>${Correction.correctorLogin}</td>
              </tr>
              <tr>
                <td>Korrektoren-History:</td>
                <td>${Correction.correctorHistory}</td>

              </tr>
            </table>
			</fieldset><br>
            <fieldset><legend>Speichern</legend>
            <div align="center">
	            <input type="submit" name="submit" value="Korrektur speichern">
            </div>
            <br>
            </fieldset> <br>

          </td>
  </tr>
</table>
	 
	  </td>
  </tr>
</table>

</form>

<jsp:include page="../footer.jsp" />

</body>

</html:html>
