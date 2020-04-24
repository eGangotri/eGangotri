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

    static void notify(File attachment) {
        notify(attachment.name,attachment.name, attachment.absolutePath)
    }

    static void notify(String subject, String msg, String attachment = null) {
        if (!MAILER_TO_EMAILS) {
            SettingsUtil.applyMailerSettings()
        }
        sendMail(MAILER_TO_EMAILS, subject, msg, attachment)
    }

    static void sendMail(List<String> to, String subject, String msg, String attachment = "") {
        to.forEach { email ->
            sendMail(email, subject, msg, attachment)
        }
    }

    static void sendMail(String to, String subject, String msg, String attachment = "") {
        if (!MAILER_USERNAME || !MAILER_PASSWORD || !MAILER_TO_EMAILS) {
            SettingsUtil.applyMailerSettings()
        }
        log.info("Sending eMail:\n  \tTo $to \n\tSubject: '$subject' \n\tMsg: '$msg' ${attachment ? '\n\tAttachment is ' + attachment : ''}")
        Properties props = createProps()
        Session session = createMailSession(props)

        try {
            //create a MimeMessage object
            Message mimeMessage = new MimeMessage(session)
            mimeMessage.setFrom(new InternetAddress(MAILER_USERNAME))
            mimeMessage.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to))
            mimeMessage.setSubject(subject)

            if (attachment) {
                Multipart multipart = generateMultiPart(msg, attachment)
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
