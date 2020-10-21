<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>商品秒杀</title>
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
            //格式化倒计时并展示 参数：事件差
            function formatTimeAndShow(timeGap) {
                //定义 天 时 分 秒
                var d, h, m, s;
                d = Math.floor(timeGap / 1000 / 60 / 60 / 24);
                h = Math.floor(timeGap / 1000 / 60 / 60 % 24);
                m = Math.floor(timeGap / 1000 / 60 % 60);
                s = Math.floor(timeGap / 1000 % 60);
                //展示计算出的时间
                showCountDown("sec_day", d);
                showCountDown("sec_hour", h);
                showCountDown("sec_min", m);
                showCountDown("sec_sec", s);
            }

            //展示时间的方法: 参数一:容纳时刻的元素 参数二:时刻
            function showCountDown(timeELe, value) {
                if (value < 10) {
                    value = "0" + value;
                }
                $("#" + timeELe).text(value);
            }

            function startSeckill() {
                //alert("秒杀开始!");
                //获取秒杀url
                var itemId = ${seckillItem.id};
                $.post("/Seckill/getSeckillUrl/" + itemId, {}, function (result) {
                    //获取响应结果中的seckillUrl
                    var seckillUrl = result['data'];
                    console.log(result);
                    if (seckillUrl['enable']) {
                        //此时seckillUrl可用
                        //调整秒杀按钮样式（可用）：
                        $("#seckillBtn").removeClass("disabled");
                        $("#seckillBtn").removeClass("btn-default");
                        $("#seckillBtn").addClass("btn-primary");
                        //添加一次点击事件
                        $("#seckillBtn").one('click', function () {
                            //点击开始抢购按钮之后，开始秒杀
                            //调整秒杀按钮样式（不可用）：
                            $("#seckillBtn").addClass("disabled");
                            //alert("开始秒杀！")
                            //发送秒杀商品请求：(md5用于后端验证)
                            $.post("/Seckill/startSeckill/"+itemId+"/"+seckillUrl['md5'],{},function (result) {
                                // console.log('result==========>'+result);
                                // console.log('=====================================');
                                // console.log('result[data]==========>'+result['data'])
                                var seckillOrder = result['data'];
                                if (seckillOrder){
                                    //此时订单成功生成，跳转到支付页：
                                    window.location.href = "/Seckill/toOrderPage/"+seckillOrder['orderCode'];
                                }else{
                                    //订单没有生成，说明没抢到，或是多次请求，或是没有该订单等等，提示用户
                                    console.log("很遗憾，没能抢到商品哦。");
                                }

                            });

                        });
                    } else {
                        //seckillUrl为不可用，说明要么没有这个商品，要么不再活动期间
                        alert("亲，不再活动期间哦！")
                    }
                });


            }


            //得到long类型的起始、结束时间
            var startTime = ${seckillItem.startTime.time};
            var endTime = ${seckillItem.endTime.time};
            // console.log(startTime);
            // console.log(endTime);
            //ajax请求 获取服务器当前时间
            $.get("/Seckill/getServerTime", {}, function (result) {
                // result json
                // {"success":true,"data":1595944752610,"message":"now long"}
                //服务器的当前时间
                //console.log(result);
                var serverTime = result['data'];

                //时间检查:
                if (serverTime >= endTime) {
                    //过了结束之间,提示一下,然后直接返回
                    $('#afterText').text("后结束抢购");
                    alert("该商品的秒杀已然结束!");
                    return;
                }

                //计算当前时间和开始时间的时间差
                var timeGap = startTime - serverTime;
                //开启一个计时事件（活动开始倒计时）
                var toStartTimer = setInterval(function () {
                    if (timeGap <= 0) {
                        //此时活动已经开始
                        //清除 活动开始倒计时 定时事件
                        clearInterval(toStartTimer)
                        //开始秒杀
                        startSeckill();
                        //准备开启另一个计时事件（活动结束倒计时）
                        // 修改展示文字：
                        $('#afterText').text("后结束抢购");
                        // 计算此刻距离活动结束的时间差，需要重新获取当前时间，依然从服务器获取：
                        $.get("/Seckill/getServerTime", {}, function (result) {
                            serverTime = result['data'];
                            timeGap = endTime - serverTime;
                            //开启倒计时：
                            var toStopTimer = setInterval(function () {
                                if (timeGap <= 0) {
                                    //此时活动结束，清除结束倒计时
                                    clearInterval(toStopTimer);
                                    alert("活动结束！");
                                    //返回当前函数，不再继续执行
                                    return;
                                }
                                //格式化结束倒计时并展示
                                formatTimeAndShow(timeGap);
                                //更新时间差
                                timeGap = timeGap - 1000;
                            }, 1000);
                        });
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
        <div class="alert alert-info" role="alert">库存数量：${seckillItem.number}</div>
        <div class="alert alert-info" role="alert">
            秒杀开始时间：<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${seckillItem.startTime}"></fmt:formatDate>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            秒杀结束时间：<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${seckillItem.endTime}"></fmt:formatDate>
        </div>

        <%-- 当前场次 00 天 00 时 00 分 00 后结束抢购 --%>
        <div id="countdownBox">
            <span class="st">当前场次</span>

            <span id="sec_day" class="timeNum">00</span>
            <span class="st">天</span>
            <span id="sec_hour" class="timeNum">00</span>
            <span class="st">时</span>
            <span id="sec_min" class="timeNum">00</span>
            <span class="st">分</span>
            <span id="sec_sec" class="timeNum">00</span>

            <span id="afterText" class="st">后开始抢购</span>
        </div>
        <div style="text-align:center">
            <button id="seckillBtn" class="btn btn-default disabled">开始抢购</button>
        </div>


    </div>
</div>
</body>
</html>
