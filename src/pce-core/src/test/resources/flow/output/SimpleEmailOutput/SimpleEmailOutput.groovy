package flow.output.SimpleEmailOutput

import groovy.util.logging.Log4j2
import org.pce.core.Node
import org.springframework.util.Assert

import javax.mail.Address
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 */
@Log4j2
class SimpleEmailOutput extends Node {
    String from = "kmc@kingsoft.com"
    String pw = ""
    String host = "smtp.kingsoft.com"

    @Override
    def handle(Map globalMemory) {
        log.info(this.getClass().getName())

        List<Map> userInfo = globalMemory["userInfo"] as List<Map>
        Assert.notNull(userInfo)

        Properties props = new Properties()
        // 开启debug调试
        props.setProperty("mail.debug", "true")
        // 发送服务器需要身份验证
        props.setProperty("mail.smtp.auth", "true")
        // 设置邮件服务器主机名
        props.setProperty("mail.host", host)
        // 发送邮件协议名称
        props.setProperty("mail.transport.protocol", "smtp")

        log.debug("props={}", props)

        // 设置环境信息
        Session session = Session.getInstance(props)

        // 创建邮件对象
        Message mailMsg = new MimeMessage(session)
        mailMsg.setSubject("错误报警")
        // 设置邮件内容
        mailMsg.setText("发生错误！\n${globalMemory.get("msg")}")
        // 设置发件人
        mailMsg.setFrom(new InternetAddress(from))

        userInfo.each {
            String email = it["email"]
            Assert.notNull(email)
            log.debug("email={}", email)

            Transport transport = session.getTransport()
            // 连接邮件服务器
            transport.connect(from, pw)
            // 发送邮件
            transport.sendMessage(mailMsg, [new InternetAddress(email)] as Address[])
            // 关闭连接
            transport.close()
        }

        if (this.next != null)
            this.next.handle(this, globalMemory)//调用下一个MessageHandler
        return null
    }
}
