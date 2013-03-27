<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
 <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<%!public long getCurrentTime() {
		return System.nanoTime();
	}%>

<c:set var="unique_page_id"><%=getCurrentTime()%></c:set>
<c:set var="tabContentId">tabContent-${unique_page_id}</c:set>
<c:set var="mainContentId">mainContent-${unique_page_id}</c:set>
<c:set var="childContentId">childContent-${unique_page_id}</c:set>

<script>
$(document).ready(function() {
	$("#${mainContentId}").find(".bloodTypingPrimaryTestsDoneSummaryTable")
												.dataTable({
									        "bJQueryUI" : true,
									        "sDom" : '<"H"T>t<"F"ip>T',
									        "oTableTools" : {
									          "sRowSelect" : "single",
									          "aButtons" : [ "print" ],
									          "fnRowSelected" : function(node) {
									 													  },
													"fnRowDeselected" : function(node) {
																							}
													}
									       });

	$("#${mainContentId}").find(".enterTestResultsForCollectionsButton")
												.button({icons: {primary :  'ui-icon-plusthick'}})
												.click(fetchBloodTypingForm);

	function fetchBloodTypingForm() {
	  showLoadingImage($("#${tabContentId}"));
    $.ajax({
      url: "bloodTypingWorksheetGenerator.html",
      data: {},
      type: "GET",
      success: function (response) {
        			 	 $("#${tabContentId}").replaceWith(response);
      				 },
      error:   function (response) {
								 showErrorMessage("Something went wrong. Please try again.");
      				 }
      
    });
	}

});
</script>

<div id="${tabContentId}">

	<div id="${mainContentId}">

		<div class="successBox ui-state-highlight">
			<img src="images/check_icon.png"
					 style="height: 30px; padding-left: 10px; padding-right: 10px;" />
			<span class="successText">
				Blood Typing tests added successfully for collections.
				<br />
				Please review results below. Perform confirmatory tests for collections as indicated.
				You may click the collections in the table below to enter secondary test results now.
			</span>
		</div>

		<br />
		<br />

		<button class="enterTestResultsForCollectionsButton">
			Enter blood typing tests for more collections
		</button>

		<br />
		<br />
		<br />

		<table class="bloodTypingPrimaryTestsDoneSummaryTable">
			<thead>
				<tr>
					<th>${collectionFields.collectionNumber.displayName}</th>
					<th>Blood Typing Status</th>
					<th>Pending tests</th>
					<th>Blood ABO determined</th>
					<th>Blood Rh determined</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="collectionEntry" items="${collections}">
					<c:set var="collection" value="${collectionEntry.value}" />
					<c:set var="bloodTypingOutputForCollection" value="${bloodTypingOutput[collection.id]}" />
					<tr>
						<td style="width: 140px;">
							${collection.collectionNumber}
						</td>
						<td style="text-align: center; width: 140px;">
							${bloodTypingOutputForCollection.bloodTypingStatus}
						</td>
						<td style="width: auto;">
							<ul>
								<c:forEach var="pendingTestId" items="${bloodTypingOutputForCollection.pendingTestsIds}">
									<c:set var="pendingTest" value="${advancedBloodTypingTests[pendingTestId]}" />
									<li>
										${pendingTest.testName}
									</li>
								</c:forEach>
							</ul>
						</td>
						<td style="text-align: center; width: 140px;">
							${bloodTypingOutputForCollection.bloodAbo}
						</td>
						<td style="text-align: center; width: 140px;">
							${bloodTypingOutputForCollection.bloodRh}
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>

	</div>

</div>
