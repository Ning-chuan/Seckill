<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>支付订单</title>
    <%--导入公用内容 Bootstrap jQuery ... --%>
    <%@include file="common/head.jsp" %>
    <style type="text/css">
        * {
            margin: 0;
            padding: 0;
        }

        #seckillItemDiv {
            margin-top: 3%;
        }

        #countdownBox {
            background-color: #D00414;
            line-height: 120px;
            margin: auto auto;
            text-align: center;
            border-radius: 10px;
        }

        .timeNum {
            color: #D00414;
            border-radius: 5px;
            border: 1px solid #440106;
            font-size: 45px;
            background-color: #440106;
        }

        .st {
            font-size: 18px;
            color: white;
        }
    </style>

    <script type="text/javascript">
        /*jQuery入口函数 网页中DOM结构加载完毕时执行*/
        $(function () {
            //函数：格式化倒计时并展示 参数：时间差
            function formatTimeAndShow(timeGap) {
                //定义 分 秒
                var m, s;
                m = Math.floor(timeGap / 1000 / 60 % 60);
                s = Math.floor(timeGap / 1000 % 60);
                //展示计算出的时间
                showCountDown("sec_min", m);
                showCountDown("sec_sec", s);
            }

            //函数: 展示时间 参数一:容纳时刻的元素 参数二:时刻
            function showCountDown(timeELe, value) {
                if (value < 10) {
                    value = "0" + value;
                }
                $("#" + timeELe).text(value);
            }


            //得到long类型的订单超时时间
            var endTime = ${order.createTime.time} + ${order.orderTimeout*1000};
            //ajax请求 获取服务器当前时间
            $.get("/Seckill/getServerTime", {}, function (result) {
                // result json
                // {"success":true,"data":1595944752610,"message":"now long"}
                //服务器的当前时间
                //console.log(result);
                var serverTime = result['data'];

                //时间检查:
                if (serverTime >= endTime) {
                    //过了失效时间,提示一下,然后直接返回
                    alert("该订单已经失效!");
                    return;
                }

                //计算当前时间和订单失效时间的时间差
                var timeGap = endTime - serverTime;
                //开启一个计时事件（订单超时/过期倒计时）
                var expireTimer = setInterval(function () {
                    if (timeGap <= 0) {
                        //此时订单失效
                        //清除 活动开始倒计时 定时事件
                        clearInterval(expireTimer)
                        // 设置支付按钮不可用
                        $("#payBtn").addClass("disabled");
                        //取消按钮的提交属性
                        $("#payBtn").removeAttr("type");
                        //返回当前函数,不再往后执行
                        return;
                    }
                    //timeGap > 0 说明活动还没有开始
                    //格式化开始倒计时并展示
                    formatTimeAndShow(timeGap);
                    //更新时间差
                    timeGap = timeGap - 1000;
                }, 1000);

            });



        });
    </script>
</head>
<body>
<div class="container">

    <div id="seckillItemDiv" class="panel panel-danger">
        <!-- Default panel contents -->
        <div class="alert panel-heading">商品名称：${seckillItem.name}</div>
        <div class="alert alert-info" role="alert">秒杀价格：${seckillItem.price} &nbsp;元</div>

        <%-- 展示订单失效时间 --%>
        <div id="countdownBox">
            <span class="st">当前订单</span>

            <span id="sec_min" class="timeNum">00</span>
            <span class="st">分</span>
            <span id="sec_sec" class="timeNum">00</span>

            <span id="afterText" class="st">后失效</span>
        </div>
        <div style="text-align:center">
            <form action="/Seckill/payOrder" method="get">
                <input type="hidden" name="orderCode" value="${order.orderCode}">
                <button id="payBtn" class="btn btn-success" type="submit">支付</button>
            </form>
        </div>


    </div>
</div>
</body>
</html>
