package com.egangotri.mail

import com.egangotri.upload.util.UploadUtils
import groovy.util.logging.Slf4j

import javax.activation.DataHandler
import javax.activation.DataSource
import javax.activation.FileDataSource
import javax.mail.BodyPart
import javax.mail.Multipart
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart

@Slf4j
class MailUtil {
    static String MAILER_USERNAME = ""
    static String MAILER_PASSWORD = ""
    static String MAILER_HOST = "smtp.gmail.com"
    static List<String> MAILER_TO_EMAILS = []

    static generateMultiPart(String msg, String attachmentPath) {
        BodyPart messageBodyPart = new MimeBodyPart()
        messageBodyPart.setText(msg)

        Multipart multipart = new MimeMultipart()
        multipart.addBodyPart(messageBodyPart)

        messageBodyPart = new MimeBodyPart()
        DataSource source = new FileDataSource(attachmentPath)
        messageBodyPart.setDataHandler(new DataHandler(source))
        messageBodyPart.setFileName(UploadUtils.stripFilePath(attachmentPath))
        multipart.addBodyPart(messageBodyPart)
        return multipart
    }

    static Properties createProps() {
        Properties props = new Properties()
        props.put("mail.smtp.auth", "true")
        props.put("mail.smtp.starttls.enable", "true")
        props.put("mail.smtp.host", MAILER_HOST)
        props.put("mail.smtp.port", "587")
        return props
    }

    static Session createMailSession(Properties props) {
        return Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(MAILER_USERNAME, MAILER_PASSWORD)
                    }
                })
    }
}
