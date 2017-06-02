<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<title>Namaz App Admin Girişi</title>
<head>

</head>
<body>
<c:forEach items="${captures}" var="capture">
    <a href="${capture.href}">${capture.timeFrame} - Move-Rate = ${capture.moveRate}</a><<br>
</c:forEach>
</body>
</html>

