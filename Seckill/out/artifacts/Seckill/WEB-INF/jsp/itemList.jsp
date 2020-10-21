<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>秒杀商品列表</title>
    <%--导入公用内容 Bootstrap jQuery ... --%>
    <%@include file="common/head.jsp" %>
    <style>
        #seckillItemsDiv{
            margin-top: 5%;
        }
    </style>
</head>
<body>
<div class="container">

    <div id="seckillItemsDiv" class="panel panel-danger">
        <!-- Default panel contents -->
        <div class="panel-heading">秒杀商品列表</div>

        <!-- Table -->
        <table class="table">
            <thead>
            <tr>
                <th>名称</th>
                <th>库存</th>
                <th>价格</th>
                <th>开始时间</th>
                <th>结束时间</th>
                <th>商品详情</th>
            </tr>
            </thead>
            <tbody>

            <c:forEach items="${seckillItemList}" var="item">
                <tr>
                    <td>${item.name}</td>
                    <td>${item.number}</td>
                    <td>${item.price}</td>
                    <td><fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${item.startTime}"></fmt:formatDate></td>
                    <td><fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${item.endTime}"></fmt:formatDate></td>
                    <td><a href="/Seckill/itemDetail/${item.id}">进入秒杀</a></td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
