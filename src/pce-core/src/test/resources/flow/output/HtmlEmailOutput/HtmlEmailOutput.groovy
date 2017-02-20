package flow.output.HtmlEmailOutput

import com.ksyun.mc.utils.GroovyExcutorUtils
import groovy.util.logging.Log4j2
import org.pce.core.Node
import org.springframework.util.Assert

import javax.activation.DataHandler
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

/**
 */
@Log4j2
class HtmlEmailOutput extends Node {
    String from = "kmc@kingsoft.com"
    String pw = ""
    String host = "smtp.kingsoft.com"

    @Override
    def handle(Map globalMemory) {
        log.info(this.getClass().getName())

        //得到用户信息
        List<Map> userInfo = globalMemory["userInfo"] as List<Map>
        Assert.notNull(userInfo)

        //得到模板，并进行赋值
        String templet = super.params["templet"] as String
        String msg = globalMemory.get("msg")
        Map<String, Object> param = ["msg": msg]
        templet = GroovyExcutorUtils.evaluate(templet, param)


        //拼装邮件所需要的信息
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
        mailMsg.setSubject("${globalMemory.("projectName")} 异常告警!")
        // 设置发件人
        mailMsg.setFrom(new InternetAddress(from))
        mailMsg.setSentDate(new Date())// 发送日期
        Multipart mp = new MimeMultipart("related")// related意味着可以发送html格式的邮件
        // 设置邮件内容
        /** *************************************************** */
        BodyPart bodyPart = new MimeBodyPart()// 正文
        bodyPart.setDataHandler(new DataHandler(templet, "text/html;charset=GBK"))// 网页格式
        mp.addBodyPart(bodyPart)
        /** *************************************************** */
//        BodyPart attachBodyPart = new MimeBodyPart()// 普通附件
//        FileDataSource fds = new FileDataSource("c:/boot.ini")
//        attachBodyPart.setDataHandler(new DataHandler(fds))
//        attachBodyPart.setFileName("=?GBK?B?"
//                + new sun.misc.BASE64Encoder().encode(fds.getName().getBytes())
//                + "?=")// 解决附件名中文乱码
//        mp.addBodyPart(attachBodyPart)
        /** *************************************************** */
//        MimeBodyPart imgBodyPart = new MimeBodyPart() // 附件图标
//        byte[] bytes = readFile("C:/button.gif")
//        ByteArrayDataSource fileds = new ByteArrayDataSource(bytes,
//                "application/octet-stream")
//        imgBodyPart.setDataHandler(new DataHandler(fileds))
//        imgBodyPart.setFileName("button.gif")
//        imgBodyPart.setHeader("Content-ID", "<img1></img1>")// 在html中使用该图片方法src="cid:IMG1"
//        mp.addBodyPart(imgBodyPart)

        mailMsg.setContent(mp)// 设置邮件内容对象  

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
