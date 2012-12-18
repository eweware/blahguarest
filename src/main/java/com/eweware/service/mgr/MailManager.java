package main.java.com.eweware.service.mgr;

import main.java.com.eweware.service.base.error.SystemErrorException;
import main.java.com.eweware.service.base.mgr.ManagerState;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.ws.WebServiceException;
import java.net.InetAddress;
import java.util.Properties;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/10/12 Time: 3:54 PM
 *         TODO eventually, it will talk to a tracking service
 *         TODO add monitors for mutating variables or lists with mutating elements
 *         TODO add transaction-level management (rollbacks)
 */
public class MailManager implements main.java.com.eweware.service.base.mgr.ManagerInterface {

    private static MailManager singleton;

    public static MailManager getInstance() throws main.java.com.eweware.service.base.error.SystemErrorException {
        if (MailManager.singleton == null) {
            throw new main.java.com.eweware.service.base.error.SystemErrorException("mail manager not initialized");
        }
        return MailManager.singleton;
    }

    private main.java.com.eweware.service.base.mgr.ManagerState state = main.java.com.eweware.service.base.mgr.ManagerState.UNINITIALIZED;
    private Properties props = new Properties();
    private Session session;

    private final Boolean doNotActivate;
    private final String authorized;
    private final String tls;
    private final String hostname;
    private final String port;
    private final String account;
    private final String password;
    private final String replyTo;

    public MailManager(Boolean doNotActivate, String authorized, String tls, String hostname,
                       String port, String account, String password, String replyTo) {
        this.doNotActivate = doNotActivate;
        this.authorized = authorized;
        this.tls = tls;
        this.hostname = hostname;
        this.port = port;
        this.account = account;
        this.password = password;
        this.replyTo = replyTo;
        props.put("mail.smtp.auth", authorized);
        props.put("mail.smtp.starttls.enable", tls);
        props.put("mail.smtp.host", hostname);
        props.put("mail.smtp.port", port);
        printConfig();
        MailManager.singleton = this;
        state = ManagerState.INITIALIZED;
        System.out.println("*** MailManager initialized ***");
    }

    private void printConfig() {
        System.out.println("*** Start MailManager Properties ***");
        System.out.println(props);
        System.out.println("Account: " + account);
        ;
        System.out.println("Password: " + password);
        System.out.println("ReplyTo: " + replyTo);
        System.out.println("*** End of MailManager Properties ***");
    }

    public Session getSession() {
        return session;
    }

    public String getReplyToEmailAddress() {
        return replyTo;
    }

    /**
     * send method should really queue request to an smtp service *
     */
    public void send(String recipient, String subject, String body) throws SendFailedException, MessagingException {
        if (state != ManagerState.STARTED || recipient == null || subject == null || body == null) {
            System.out.println("WARNING: MailManager not sending");
            return;
        }
        final MimeMessage message = new MimeMessage(session);
        final MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        message.setReplyTo(new InternetAddress[]{new InternetAddress(getReplyToEmailAddress())});
        message.setRecipients(Message.RecipientType.TO, recipient);
        message.setSubject(subject);
        helper.setText(body);
        Transport.send(message);
    }

    private void test() throws SystemErrorException {
        try {
            System.out.println("TESTING MailManager...");
            final String host = InetAddress.getLocalHost().getHostName();
            send("rk@eweware.com", "Server at '" + host + "' Started", "This host has just started running...");
            System.out.println("... MailManager test succeeded.");
        } catch (SendFailedException e) {
        } catch (Exception e) {
            throw new SystemErrorException(e);
        }
    }

    @Override
    public void start() {

        System.out.print("*** MailManager ");
        if (doNotActivate) {
            System.out.println("Disabled ***");
            return;
        }
        System.out.println("Enabled ***");
        session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(account, password);
                    }
                });
        state = ManagerState.STARTED;
        System.out.println("*** MailManager started ***");

        try {
            test();
        } catch (SystemErrorException e) {
            throw new WebServiceException(e);
        }
    }

    @Override
    public void shutdown() {
        session = null;
        state = ManagerState.SHUTDOWN;
        System.out.println("*** MailManager shut down ***");
    }
}
