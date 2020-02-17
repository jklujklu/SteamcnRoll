import com.alibaba.fastjson.JSON;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Properties;

public class EmailUtils {

    //发送sa者邮箱（QQ邮箱）
    private String senderEmail;
    //接收者邮箱
    private String receiveEmail;
    //qq号
    private String qq;
    //qq邮箱授权码
    private String emailPassword;

    private EmailUtils() {
        //1. 读取raws源文件
        String raws = null;
        try {
            raws = FileUtils.getInstance().parseRaws();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //2. 解析json，读取与email相关的参数
        if (raws != null) {
            String sender = JSON.parseObject(raws).getString("senderEmail");
            String receiver = JSON.parseObject(raws).getString("receiveEmail");
            String qq = JSON.parseObject(raws).getString("qq");
            String password = JSON.parseObject(raws).getString("emailPassword");
            if (!sender.equals("") && !receiver.equals("") && !qq.equals("") && !password.equals("")) {
                this.senderEmail = sender;
                this.receiveEmail = receiver;
                this.qq = qq;
                this.emailPassword = password;
            } else {
                System.out.println("请设置邮箱各项参数，确保其不为空");
                System.exit(-1);
            }
        }
    }

    public static EmailUtils getInstance() {
        return new EmailUtils();
    }

    public void sendEmail(String content) {
        // 1. 创建参数配置, 用于连接邮件服务器的参数配置
        Properties props = new Properties();                    // 参数配置
        props.setProperty("mail.transport.protocol", "smtp");   // 使用的协议（JavaMail规范要求）
        props.setProperty("mail.smtp.host", "smtp.qq.com");   // 发件人的邮箱的 SMTP 服务器地址
        props.setProperty("mail.smtp.auth", "true");            // 需要请求认证
        props.setProperty("mail.smtp.port", "465");
        //设置socketfactory
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        //只处理SSL的连接, 对于非SSL的连接不做处理
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        // 2. 根据配置创建会话对象, 用于和邮件服务器交互
        Session session = Session.getInstance(props);
        session.setDebug(true);  // 设置为debug模式, 可以查看详细的发送 log

        try {
            // 3. 创建一封邮件
            MimeMessage message = createMimeMessage(session, senderEmail, receiveEmail, content);
            // 4. 根据 Session 获取邮件传输对象
            Transport transport = null;
            // 5. 使用 邮箱账号 和 密码 连接邮件服务器, 这里认证的邮箱必须与 message 中的发件人邮箱一致, 否则报错
            //
            //    PS_01: 成败的判断关键在此一句, 如果连接服务器失败, 都会在控制台输出相应失败原因的 log,
            //           仔细查看失败原因, 有些邮箱服务器会返回错误码或查看错误类型的链接, 根据给出的错误
            //           类型到对应邮件服务器的帮助网站上查看具体失败原因。
            //
            //    PS_02: 连接失败的原因通常为以下几点, 仔细检查代码:
            //           (1) 邮箱没有开启 SMTP 服务;
            //           (2) 邮箱密码错误, 例如某些邮箱开启了独立密码;
            //           (3) 邮箱服务器要求必须要使用 SSL 安全连接;
            //           (4) 请求过于频繁或其他原因, 被邮件服务器拒绝服务;
            //           (5) 如果以上几点都确定无误, 到邮件服务器网站查找帮助。
            transport = session.getTransport();
            transport.connect(qq, emailPassword);

            // 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
            transport.sendMessage(message, message.getAllRecipients());

            // 7. 关闭连接
            transport.close();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MimeMessage createMimeMessage(Session session, String sendMail, String receiveMail, String content) throws Exception {
        // 1. 创建一封邮件
        MimeMessage message = new MimeMessage(session);

        message.setHeader("Content-Transfer-Encoding", "base64");
        // 2. From: 发件人（昵称有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改昵称）
        message.setFrom(new InternetAddress(sendMail, "发送者", "UTF-8"));

        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
        //    CC:抄送人，BCC:密送
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail, "接收者", "UTF-8"));

        // 4. Subject: 邮件主题（标题有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改标题）

        message.setSubject(URLDecoder.decode("SteamCn蒸汽抽奖", "utf-8"), "UTF-8");

        // 5. Content: 邮件正文（可以使用html标签）（内容有广告嫌疑，避免被邮件服务器误认为是滥发广告以至返回失败，请修改发送内容）
        message.setContent(content, "text/html;charset=utf-8");

        // 6. 设置发件时间
        message.setSentDate(new Date());

        // 7. 保存设置
        message.saveChanges();

        return message;
    }
}
