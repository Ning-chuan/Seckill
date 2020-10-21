<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <!-- 上述3个meta标签*必须*放在最前面，任何其他内容都*必须*跟随其后！ -->
    <title>秒杀系统首页</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@3.3.7/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        #title{
            text-align: center;
            margin: 5% auto;
        }
    </style>
</head>
<body>

    <form class="form-horizontal" action="/Seckill/login" method="post">
        <h1 id="title" class="form-group col-sm-12">欢迎进入秒杀抢购系统</h1>
        <div class="form-group">
            <label for="nameInput" class="col-sm-2 control-label">用户名：</label>
            <div class="col-sm-8">
                <input type="text" class="form-control" id="nameInput" name="username" placeholder="用户名" required>
            </div>
        </div>
        <div class="form-group">
            <label for="passwordInput" class="col-sm-2 control-label">密&nbsp;&nbsp;码：</label>
            <div class="col-sm-8">
                <input type="password" class="form-control" id="passwordInput" name="password" placeholder="密码" required>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <button type="submit" class="btn btn-primary">登录</button>
                <button type="button" class="btn btn-primary">注册</button>
            </div>
        </div>
    </form>





    <!-- jQuery (Bootstrap 的所有 JavaScript 插件都依赖 jQuery，所以必须放在前边) -->
    <script src="https://cdn.jsdelivr.net/npm/jquery@1.12.4/dist/jquery.min.js"></script>
    <!-- 加载 Bootstrap 的所有 JavaScript 插件。你也可以根据需要只加载单个插件。 -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@3.3.7/dist/js/bootstrap.min.js"></script>

</body>
</html>
