<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
<title>Namaz App Admin Girişi</title>
<head>

</head>
<body>
<form action="${serverAppRoot}/admin/authenticate" method="post">
    <input name="username" value="username">
    <input type="password" name="password" value="password">
    <input type="submit">
</form>
</body>
</html>

