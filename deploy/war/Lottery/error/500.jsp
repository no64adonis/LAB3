<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Internal Server Error</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f4f4f4;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }
        .error-container {
            background-color: white;
            padding: 30px;
            border-radius: 5px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            text-align: center;
        }
        .error-container h1 {
            color: #e74c3c;
            margin-bottom: 20px;
        }
        .error-container p {
            color: #333;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <h1>500 - Internal Server Error</h1>
        <p>An unexpected error occurred while processing your request.</p>
        <p>Please try again later or contact the system administrator.</p>
        <a href="${pageContext.request.contextPath}/" class="btn lottery-btn">Go to Homepage</a>
    </div>
</body>
</html>