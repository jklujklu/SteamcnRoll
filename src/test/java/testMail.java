public class testMail {
    public static void main(String[] args) {
        EmailUtils.getInstance().sendEmail("哈哈哈");
        System.out.println("\n邮件发送完毕，请检查是否接受成功\n");
    }
}
