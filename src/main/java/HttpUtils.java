import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpUtils {

    public static final String ROLL_BOX[] = {"我全都要（奖品一份）", "都行（返还1蒸汽）", "可以（返还1蒸汽）", "随你（返还1蒸汽）",
            "没关系（返还1蒸汽）", "要放下（返还1蒸汽）", "会忍耐（返还1蒸汽）", "看淡了（返还1蒸汽）",
            "就这样吧（返还1蒸汽）", "一切随缘（返还1蒸汽）", "大彻大悟（返还9蒸汽）"};

    public static final String URL_LOGIN = "https://keylol.com/member.php?mod=logging&action=login&loginsubmit=yes&inajax=1";
    public static final String URL_ROLL = "https://keylol.com/plugin.php?id=steamcn_lottery:view&lottery_id=41";
    public static final String URL_MISC = "https://keylol.com/misc.php?mod=seccode";

    //以下两参数的组合， 用于访问抽奖界面
    //用于登录验证的cookie
    private String auth;
    //用途未知的Cookie
    private String key;
    //是否成功登录
    private boolean isLogin;
    //执行抽奖的url
    private String rollUrl;

    public HttpUtils() {
        this.auth = "";
        this.key = "";
        this.rollUrl = "";
        this.isLogin = false;
    }

    /**
     * 请求登录
     *
     * @param params 登录表单
     */
    public void doLogin(Map<String, String> params) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost(URL_LOGIN);
        if (!auth.equals("") && !key.equals("")) {
            post.addHeader("cookie", "dz_2132_saltkey" + "=" + key + ";" + "dz_2132_auth" + "=" + auth);
        }
        post.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36");
        List<NameValuePair> list = new ArrayList<>();
        //添加表单
        for (Map.Entry<String, String> entry : params.entrySet()) {
            NameValuePair valuePair = new BasicNameValuePair(entry.getKey(), entry.getValue());
            list.add(valuePair);
        }
        try {
            //执行Post操作
            post.setEntity(new UrlEncodedFormEntity(list, "utf-8"));
            //获取响应
            CloseableHttpResponse response = httpClient.execute(post);
            //提取Cookie中两项必要参数，key和auth
            initAuthKey(response.getHeaders("Set-Cookie"));

            //获取相应内容
            String temp = EntityUtils.toString(response.getEntity());
            System.out.println("\n+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            System.out.println(temp);
            System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++\n");
            //正则表达式，提取登录信息
            String pattern = "CDATA\\[([^<]+)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(temp);
            String msg = "";
            if (m.find()) {
                msg = m.group(1);
            }

            System.out.println(msg);
            //登录需要验证码，该方法还未测试
            if (msg.contains("验证码")) {
                loginAgain();
                //登陆成功
            } else if (msg.equals("")) {
                //提取用户信息
                Map<String, String> map = regexCookie(temp, "'(\\w+)':'([0-9a-zA-Z]+)'");
                System.out.println("----------------------------------------------------------------------");
                System.out.println("登陆成功！登录账号信息如下：");
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    System.out.println(entry.getKey() + " ---> " + entry.getValue());
                }
                System.out.println("----------------------------------------------------------------------");
                isLogin = true;
            }
            response.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 提取执行抽奖的url
     */
    public void getRollUrl() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet(URL_ROLL);
        get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36");
        String cookie = "dz_2132_auth=" + auth + ";" + "dz_2132_saltkey=" + key;
        get.addHeader("cookie", cookie);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(get);
            String temp = EntityUtils.toString(response.getEntity());
            String pattern = "getJSON\\('([^']+)";

            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(temp);
            if (m.find()) {
                rollUrl = m.group(1);
            } else {
                rollUrl = "";
            }
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 抽奖
     *
     * @return 抽奖结果
     */
    public String doRoll() {
        //获取执行抽奖的URL
        getRollUrl();
        String result = "";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        if (rollUrl.equals("")) {
            System.out.println("请检查账号是否满足抽奖条件，或已达到最大抽奖次数！");
            return result;
        }
        HttpGet get = new HttpGet(rollUrl);
        get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36");
        String cookie = "dz_2132_auth=" + auth + ";" + "dz_2132_saltkey=" + key;
        get.addHeader("cookie", cookie);
        try {
            CloseableHttpResponse response = httpClient.execute(get);
            String temp = EntityUtils.toString(response.getEntity());
            JSONObject object = null;
            try {
                object = JSON.parseObject(temp);
            } catch (JSONException e) {
                System.out.println("当前不可进行抽奖，请检查账号是否满足抽奖条件！");
                return result;
            }
            int id = object.getIntValue("id");
            System.out.println("\n");
            System.out.println("----------------------------------------------------------------------");
            System.out.println(ROLL_BOX[id]);
            System.out.println("----------------------------------------------------------------------");
            System.out.println("\n");
            result = ROLL_BOX[id];
            System.out.println("/////////////////////////////////////////////////////////////////////////////////");
            System.out.println(temp);
            System.out.println("/////////////////////////////////////////////////////////////////////////////////\n");
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * 返回登录状态
     *
     * @return 登陆状态
     */
    public boolean isLogin() {
        return isLogin;
    }

    /**
     * 获取验证码
     *
     * @return
     */
    private boolean getMisc() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet(URL_MISC);
        get.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36");

        if (!auth.equals("")) {
            get.addHeader("referer", "https://keylol.com/member.php?mod=logging&action=login&auth=" + auth);
        } else {
            System.out.println("未找到dz_2132_auth参数，请检查登陆是否有效");
            return false;
        }
        try {
            CloseableHttpResponse response = httpClient.execute(get);
            InputStream is = response.getEntity().getContent();
            FileUtils.getInstance().writeFile(is, "misc.png");
            response.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * 根据验证码重新登录
     */
    private void loginAgain() {
        if (getMisc()) {
            Scanner scan = new Scanner(System.in);
            // 从键盘接收数据
            System.out.println("验证码图片已下载，请检查（当前jar所在路径/misc.png）");
            System.out.println("Please input the Misc code: ");
            String miscCode = scan.next();
            scan.close();
            if (!miscCode.isEmpty()) {
                Map<String, String> map = new HashMap<>();
                map.put("auth", auth);
                map.put("seccodemodid", "member::logging");
                map.put("seccodeverify", miscCode);
                System.out.println("重新尝试登录");
                doLogin(map);
            }
        }
    }

    /**
     * 从Cookie中提取dz_2132_saltkey和dz_2132_auth
     */
    private void initAuthKey(Header[] headers) {
        for (Header h : headers) {
            if (!key.equals("") && !auth.equals("")) {
                break;
            }
            String str = h.getValue();
            String pattern = "(\\w+)=([^;]+)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(str);
            if (m.find()) {
                String name = m.group(1);
                String value = m.group(2);
                if (name.equals("dz_2132_saltkey")) {
                    if (!value.equals("deleted")) {
                        key = value;
                    }
                } else if (name.equals("dz_2132_auth")) {
                    if (!value.equals("deleted")) {
                        auth = value;
                    }
                } else {
                    continue;
                }
            }
        }
    }

    /**
     * 登陆成功时使用
     * 正则表达式提取用户信息
     *
     * @param msg     源数据
     * @param pattern 正则表达式
     * @return 用户信息
     */
    private Map<String, String> regexCookie(String msg, String pattern) {
        Map<String, String> result = new HashMap<>();
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(msg.replace(" ", ""));
        while (m.find()) {
            result.put(m.group(1), m.group(2));
        }
        return result;
    }

}
