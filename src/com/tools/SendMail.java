package com.tools;

import javax.mail.internet.*;
import javax.mail.*;
import java.util.*;

/**
 * Classe permettant d'envoyer un mail.
 */
public class SendMail {

  private final static String MAILER_VERSION = "Java";

  public static boolean envoyerMailSMTP(String serveur, int port, boolean debug) {

    boolean result = false;

    try {
      Properties prop = System.getProperties();
      prop.put("mail.transport.protocol", "smtp");
      prop.put("mail.transport.protocol", "smtps");
      prop.put("mail.smtp.host", serveur);
      prop.put("mail.smtp.port", port);
      prop.put("mail.smtp.username", "dev@ebusinessafrique.com");
      prop.put("mail.smtp.password", "wW+!8;G88hAV");
      prop.put("mail.smtp.auth", true); // properties.put("mail.smtp.auth","true");
      prop.put("mail.smtp.ssl.enable", true);// use the SSL authentication
      // prop.put("mail.smtp.starttls.enable", false); // Bypass the SSL authentication

      Authenticator authentication = new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(prop.getProperty("mail.smtp.username"), prop.getProperty("mail.smtp.password"));
			}
		};

      Session session = Session.getDefaultInstance(prop, authentication);

      Message message = new MimeMessage(session);
      // message.setFrom(new InternetAddress("moi@chez-moi.fr"));
      message.setFrom(new InternetAddress("dev@ebusinessafrique.com"));
      InternetAddress[] internetAddresses = new InternetAddress[3];
      // internetAddresses[0] = new InternetAddress("moi@chez-moifr");
      internetAddresses[0] = new InternetAddress("sedar.egnonce@ebusinessafrique.com");
      internetAddresses[1] = new InternetAddress("egnonse_sdar@yahoo.fr");
      internetAddresses[2] = new InternetAddress("mohrades@gmail.com");
      message.setRecipients(Message.RecipientType.TO, internetAddresses);
      message.setSubject("Test");
      message.setText("test mail");
      message.setHeader("X-Mailer", MAILER_VERSION);
      message.setSentDate(new Date());
      session.setDebug(debug);
      Transport.send(message);
      result = true;

    } catch (AddressException e) {
      e.printStackTrace();
    } catch (MessagingException e) {
      e.printStackTrace();
    }
    
    System.out.println(result);
    return result;
  }

  /**
   * DEBUG: setDebug: JavaMail version 1.6.1
  DEBUG: getProvider() returning javax.mail.Provider[TRANSPORT,smtp,com.sun.mail.smtp.SMTPTransport,Oracle]
  DEBUG SMTP: need username and password for authentication
  DEBUG SMTP: protocolConnect returning false, host=serflex.o2switch.net, user=Sedar, password=<null>
  DEBUG SMTP: useEhlo true, useAuth true
  DEBUG SMTP: trying to connect to host "serflex.o2switch.net", port 465, isSSL true
  220-serflex.o2switch.net ESMTP Exim 4.91 #1 Wed, 22 Aug 2018 10:53:05 +0200 
  220-We do not authorize the use of this system to transport unsolicited, 
  220 and/or bulk e-mail.
  DEBUG SMTP: connected to host "serflex.o2switch.net", port: 465
  EHLO MOHRADES.mshome.net
  250-serflex.o2switch.net Hello nacvpn.mtn.bj [41.79.216.41]
  250-SIZE 52428800
  250-8BITMIME
  250-PIPELINING
  250-AUTH PLAIN LOGIN
  250 HELP
  DEBUG SMTP: Found extension "SIZE", arg "52428800"
  DEBUG SMTP: Found extension "8BITMIME", arg ""
  DEBUG SMTP: Found extension "PIPELINING", arg ""
  DEBUG SMTP: Found extension "AUTH", arg "PLAIN LOGIN"
  DEBUG SMTP: Found extension "HELP", arg ""
  DEBUG SMTP: protocolConnect login, host=serflex.o2switch.net, user=dev@ebusinessafrique.com, password=<non-null>
  DEBUG SMTP: Attempt to authenticate using mechanisms: LOGIN PLAIN DIGEST-MD5 NTLM XOAUTH2 
  DEBUG SMTP: Using mechanism LOGIN
  DEBUG SMTP: AUTH LOGIN command trace suppressed
  DEBUG SMTP: AUTH LOGIN succeeded
  DEBUG SMTP: use8bit false
  MAIL FROM:<dev@ebusinessafrique.com>
  250 OK
  RCPT TO:<sedar.egnonce@ebusinessafrique.com>
  250 Accepted
  RCPT TO:<egnonse_sdar@yahoo.fr>
  250 Accepted
  RCPT TO:<mohrades@gmail.com>
  250 Accepted
  DEBUG SMTP: Verified Addresses
  DEBUG SMTP:   sedar.egnonce@ebusinessafrique.com
  DEBUG SMTP:   egnonse_sdar@yahoo.fr
  DEBUG SMTP:   mohrades@gmail.com
  DATA
  354 Enter message, ending with "." on a line by itself
  Date: Wed, 22 Aug 2018 09:53:02 +0100 (CAT)
  From: dev@ebusinessafrique.com
  To: sedar.egnonce@ebusinessafrique.com, egnonse_sdar@yahoo.fr,
  	mohrades@gmail.com
  Message-ID: <1582785598.0.1534927982298@MOHRADES.mshome.net>
  Subject: Test
  MIME-Version: 1.0
  Content-Type: text/plain; charset=us-ascii
  Content-Transfer-Encoding: 7bit
  X-Mailer: Java

  test mail
  .
  250 OK id=1fsOt0-0002OI-Lc
  DEBUG SMTP: message successfully delivered to mail server
  QUIT
  221 serflex.o2switch.net closing connection
  true
  */

  /**
   * DEBUG: setDebug: JavaMail version 1.6.1
  DEBUG: getProvider() returning javax.mail.Provider[TRANSPORT,smtp,com.sun.mail.smtp.SMTPTransport,Oracle]
  DEBUG SMTP: need username and password for authentication
  DEBUG SMTP: protocolConnect returning false, host=mail.ebusinessafrique.com, user=Sedar, password=<null>
  DEBUG SMTP: useEhlo true, useAuth true
  DEBUG SMTP: trying to connect to host "mail.ebusinessafrique.com", port 26, isSSL false
  220-serflex.o2switch.net ESMTP Exim 4.91 #1 Tue, 21 Aug 2018 17:13:20 +0200 
  220-We do not authorize the use of this system to transport unsolicited, 
  220 and/or bulk e-mail.
  DEBUG SMTP: connected to host "mail.ebusinessafrique.com", port: 26
  EHLO MOHRADES
  250-serflex.o2switch.net Hello MOHRADES [197.234.221.81]
  250-SIZE 52428800
  250-8BITMIME
  250-PIPELINING
  250-STARTTLS
  250 HELP
  DEBUG SMTP: Found extension "SIZE", arg "52428800"
  DEBUG SMTP: Found extension "8BITMIME", arg ""
  DEBUG SMTP: Found extension "PIPELINING", arg ""
  DEBUG SMTP: Found extension "STARTTLS", arg ""
  DEBUG SMTP: Found extension "HELP", arg ""
  DEBUG SMTP: use8bit false
  MAIL FROM:<sedar.egnonce@ebusinessafrique.com>
  250 OK
  RCPT TO:<sedar.egnonce@ebusinessafrique.com>
  250 Accepted
  DEBUG SMTP: Verified Addresses
  DEBUG SMTP:   sedar.egnonce@ebusinessafrique.com
  DATA
  354 Enter message, ending with "." on a line by itself
  Date: Tue, 21 Aug 2018 16:13:15 +0100 (CAT)
  From: sedar.egnonce@ebusinessafrique.com
  To: sedar.egnonce@ebusinessafrique.com
  Message-ID: <1582785598.0.1534864395546@MOHRADES>
  Subject: Test
  MIME-Version: 1.0
  Content-Type: text/plain; charset=us-ascii
  Content-Transfer-Encoding: 7bit
  X-Mailer: Java

  test mail
  .
  250 OK id=1fs8LR-0005RR-2P
  DEBUG SMTP: message successfully delivered to mail server
  QUIT
  221 serflex.o2switch.net closing connection
  true
  */
  public static void main(String[] args) {
    // TestMail.envoyerMailSMTP("10.10.50.8", true);
    SendMail.envoyerMailSMTP("serflex.o2switch.net", 465, true); // ssl enable
    // TestMail.envoyerMailSMTP("mail.ebusinessafrique.com", 26, true); // ssl enable
  }
}
