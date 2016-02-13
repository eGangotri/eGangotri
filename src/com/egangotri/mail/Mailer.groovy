package com.egangotri.mail

import org.slf4j.LoggerFactory

/*
@Grapes([
        @Grab(group = 'javax.mail', module = 'mail', version = '1.4',classLoader='groovy.lang.GroovyClassLoader')
        ])
*/

import javax.mail.*
import javax.mail.internet.*
import static javax.mail.Message.RecipientType.TO
/**
 * Created by user on 2/7/2016.
 */

class Mailer {
    final static org.slf4j.Logger Log = LoggerFactory.getLogger(this.class);

    private static String username= "indicjournals@gmail.com"
    private static String password= "a@hBmsep123"
    private static String host = "smtp.gmail.com"

    static main(args) {
        Log.info "Hi"
        //sendMail("pandey78@gmail.com", "Test", "Hello")
    }

    public static void sendMail(String to, String subject, String message) {
        def props = new Properties()
        props.put "mail.smtps.auth", "true"

        def session = Session.getDefaultInstance(props, null)

        def msg = new MimeMessage(session)

        msg.setSubject(subject)
        msg.setText(message)
        msg.addRecipient(TO, new InternetAddress(to, to))

        def transport = session.getTransport("smtps")

        try {
            transport.connect(host, username, password)
            transport.sendMessage(msg, msg.getAllRecipients())
        }
        catch (Exception e) {
            Log.info "Error"
        }
    }
}
