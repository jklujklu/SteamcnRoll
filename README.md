# SteamcnRoll
蒸汽动力自动抽奖，日常减3

***请确保你的电脑安装了java环境，使用时请先通过Steamcn.test.jar进行邮件的测试***

***请确保你的电脑安装了java环境，使用时请先通过Steamcn.test.jar进行邮件的测试***

***请确保你的电脑安装了java环境，使用时请先通过Steamcn.test.jar进行邮件的测试***

------------

# 运行截图

[![无需验证码的登录](https://raw.githubusercontent.com/jklujklu/SteamcnRoll/master/screenshot/loginWithoutMisc.png "无需验证码的登录")](https://raw.githubusercontent.com/jklujklu/SteamcnRoll/master/screenshot/loginWithoutMisc.png "无需验证码的登录")

[![需要验证码的登录](https://raw.githubusercontent.com/jklujklu/SteamcnRoll/master/screenshot/loginWithMisc.png "需要验证码的登录")](https://raw.githubusercontent.com/jklujklu/SteamcnRoll/master/screenshot/loginWithMisc.png "需要验证码的登录")

[![邮件测试](https://raw.githubusercontent.com/jklujklu/SteamcnRoll/master/screenshot/emailTest.png "邮件测试")](https://raw.githubusercontent.com/jklujklu/SteamcnRoll/master/screenshot/emailTest.png "邮件测试")

[![账号无法进行抽奖](https://raw.githubusercontent.com/jklujklu/SteamcnRoll/master/screenshot/rollError.png "账号无法进行抽奖")](https://raw.githubusercontent.com/jklujklu/SteamcnRoll/master/screenshot/rollError.png "账号无法进行抽奖")

[![邮件内容](https://raw.githubusercontent.com/jklujklu/SteamcnRoll/master/screenshot/emailBox.png "邮件内容")](https://raw.githubusercontent.com/jklujklu/SteamcnRoll/master/screenshot/emailBox.png "邮件内容")

# 使用方法

- 获取QQ邮箱授权码

	不知道怎么获取的可以点击[这里](https://jingyan.baidu.com/article/4b07be3cb2f74148b380f3e4.html "QQ邮箱授权码的获取")

- 下载源码或[release]("https://github.com/jklujklu/SteamcnRoll/releases/tag/v1.0")并解压

- 打开out\artifacts\Steamcn_jar目录下的raws.txt，填写相关信息

|   参数名|   描述|
| ------------ | ------------ |
|  username |  蒸汽动力用户名 |
|password|密码|
|senderEmail|发送者邮箱（你获取了授权码的QQ邮箱）|
|receiveEmail|接收者邮箱（你的任意邮箱，请确保此邮箱可以收到邮件）|
|qq|QQ号（获取了授权码的QQ邮箱绑定的QQ，貌似用于QQ邮箱的登录验证，具体用途未知）|
|emailPassword|授权码|
|count|抽奖次数（默认为0）|

**PS: 请确保以上参数均不为空，否则可能导致未知的错误**

**PS2: 以上参数除最后一个为数值类型，其余均为字符串类型，请用英文状态下的""包裹**

- 测试邮箱是否有效

	命令行进入上述文件夹

```
cd ...\out\artifacts\Steamcn_jar
java -jar Steamcn.test.jar
```

[![测试邮件发送成功](https://raw.githubusercontent.com/jklujklu/SteamcnRoll/master/screenshot/emailTest.png "测试邮件发送成功")](https://raw.githubusercontent.com/jklujklu/SteamcnRoll/master/screenshot/emailTest.png "测试邮件发送成功")

- 若可以成功接收邮件，运行以下代码

```
java -jar Steamcn.jar
```

[![验证码登录](https://raw.githubusercontent.com/jklujklu/SteamcnRoll/master/screenshot/loginWithMisc.png "验证码登录")](https://raw.githubusercontent.com/jklujklu/SteamcnRoll/master/screenshot/loginWithMisc.png "验证码登录")

- 查看邮件，over

# 项目依赖
1. HTTP请求 ---> HttpClient
2. JSON解析 ---> FastJson
3. 邮件发送   ---> JavaMail

------------

# 相关接口

- ###  登录接口

```
https://keylol.com/member.php?mod=logging&action=login&loginsubmit=yes&inajax=1
```

此接口需要使用Post方法，并添加表单，表单所需参数分两种情况，各自如下

**无需验证码时的登陆表单**

|   参数名|   描述|
| ------------ | ------------ |
|   loginfield|   登录类型（本项目使用username，即凭账号密码登录）|
| username| 登录用户名|
|password|密码|

**需要验证码时的登陆表单**

|   参数名|   描述|
| ------------ | ------------ |
|auth|用于登录认证的cookie（若账号密码正确，可从第一次登录请求返回的Header中提取，dz_2132_auth）|
|seccodemodid| 固定值（member::logging）|
|seccodeverify|验证码|

**PS：需要验证码的登录请求应添加以下header**

```
//key的获取方法与auth一致，从第一次登录请求返回的Header中提取即可
cookie: dz_2132_auth={auth};dz_2132_saltkey={key}
```

------------

- ### 每日抽奖


```
https://keylol.com/plugin.php?id=steamcn_lottery:view&lottery_id=41
```

**PS: 验证码的获取同样需要添加上述header**

**PS2: 若当前可以抽奖，则可以在script标签内获取接口**


- ### 验证码获取接口

```
https://keylol.com/misc.php?mod=seccode
```

**PS: 验证码的获取无需添加cookie，但需要额外添加以下header**

```
//auth获取方法与上文一致
referer: https://keylol.com/member.php?mod=logging&action=login&auth={auth}
```


