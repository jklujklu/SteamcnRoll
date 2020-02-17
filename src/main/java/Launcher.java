import com.alibaba.fastjson.JSON;

import java.util.*;

public class Launcher {

    public static void main(String[] args) throws Exception {
        //获取源文件内容
        String raws = FileUtils.getInstance().parseRaws();
        //json解析出用户名、密码、抽奖次数
        String user = JSON.parseObject(raws).getString("username");
        String pwd = JSON.parseObject(raws).getString("password");
        int count = JSON.parseObject(raws).getIntValue("count");
        if (user.equals("") || pwd.equals("") || count == 0) {
            System.out.println("请确保账号名、密码、执行次数不为空！");
            return;
        }
        //邮箱工具类初始化，解析与email相关的参数
        EmailUtils emailUtils = EmailUtils.getInstance();

        HttpUtils utils = new HttpUtils();
        //创建登录表单
        Map<String, String> map = new HashMap<>();
        map.put("loginfield", "username");
        map.put("username", user);
        map.put("password", pwd);
        //执行登录
        utils.doLogin(map);
        //判断是否登录成功，若成功则进行抽奖
        if (utils.isLogin()) {
            String temp = "";
            for (int i = 0; i < count; i++) {
                //获得抽奖结果，若抽奖未执行，返回值为空，说明账户已达限制，此时退出循环
                String result = utils.doRoll();
                if (!result.equals("")) {
                    temp = temp + result + "<br/>";
                } else {
                    break;
                }
            }
            //发送邮件
            if (!temp.equals("")) {
                System.out.println("\n开始发送邮件\n");
                emailUtils.sendEmail(temp);
            }
        }
    }

}
