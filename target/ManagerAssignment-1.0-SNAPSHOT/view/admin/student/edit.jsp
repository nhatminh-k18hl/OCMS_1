<%-- 
    Document   : edit
    Created on : Jun 7, 2024, 12:16:16 PM
    Author     : HP
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:include page="../../commonAdmin/head.jsp"></jsp:include>
        <div class="container">
            <h2>Edit Student</h2>
            <c:if test="${param.error != null}">
                <div class="alert alert-danger" role="alert">
                    ${param.error}
                </div>
            </c:if>
            <c:if test="${param.success != null}">
                <div class="alert alert-success" role="alert">
                    ${param.success}
                </div>
            </c:if>
            <form action="EditStudentController" method="post">
                <input type="hidden" name="studentID" value="${student.studentID}">
                <div class="form-group">
                    <label for="name">Name:</label>
                    <input type="text" class="form-control" id="name" name="name" value="${student.name}" required>
                </div>
                <div class="form-group">
                    <label for="email">Email:</label>
                    <input type="email" class="form-control" id="email" name="email" value="${student.email}" required>
                </div>
                <div class="form-group">
                    <label for="phone">Phone:</label>
                    <input type="text" class="form-control" id="phone" name="phone" value="${student.phone}" required>
                </div>
                <div class="form-group">
                    <label for="status">Status:</label>
                    <select class="form-control" id="status" name="status">
                        <option value="1" ${student.status == 1 ? "selected" : ""}>Active</option>
                        <option value="0" ${student.status == 0 ? "selected" : ""}>Inactive</option>
                    </select>
                </div>
                <button type="submit" class="btn btn-primary">Update Student</button>
            </form>
        </div>
    <%@include file="../../commonAdmin/footer.jsp" %>