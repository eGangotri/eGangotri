package com.egangotri.mail

import com.egangotri.upload.util.SettingsUtil
import groovy.util.logging.Slf4j

import javax.mail.*
import javax.mail.internet.*
import static com.egangotri.mail.MailUtil.*

@Slf4j
class Mailer {

    static main(args) {
        notify("testSub", "testMsg", "D:\\tmp\1.txt")
    }

    static void notify(String subject, String msg, String attachmentPath = "") {
        if (!MAILER_TO_EMAILS) {
            SettingsUtil.applyMailerSettings()
        }
        sendMail(MAILER_TO_EMAILS, subject, msg, attachmentPath)
    }

    static void sendMail(List<String> to, String subject, String msg, String attachmentPath = "") {
        to.forEach { email ->
            sendMail(email, subject, msg, attachmentPath)
        }
    }

    static void sendMail(String to, String subject, String msg, String attachmentPath = "") {
        if (!MAILER_USERNAME || !MAILER_PASSWORD || !MAILER_TO_EMAILS) {
            SettingsUtil.applyMailerSettings()
        }
        log.info("sendMail  To $to Subject: '$subject' Msg: '$msg' ${attachmentPath ? '\nAttachment is ' + attachmentPath : ''}")
        Properties props = createProps()
        Session session = createMailSession(props)

        try {
            //create a MimeMessage object
            Message mimeMessage = new MimeMessage(session)
            mimeMessage.setFrom(new InternetAddress(MAILER_USERNAME))
            mimeMessage.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to))
            mimeMessage.setSubject(subject)

            if (attachmentPath) {
                Multipart multipart = generateMultiPart(msg, attachmentPath)
                mimeMessage.setContent(multipart)
            } else {
                mimeMessage.setText(msg)
            }

            Transport.send(mimeMessage)
        }
        catch (Exception e) {
            log.info("You may need to turn on Less secure app accces by going to ", "https://myaccount.google.com/u/4/lesssecureapps?pli=1&pageId=none")
            log.info("Error", e)
        }
    }


}
