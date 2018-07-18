package com.softech.test.core.report;

import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.softech.test.core.util.Logger;

public class Emailer {

	private static String emailRelayHost = "imailrelay.viacom.com";
	private static String emailRelayPort = "25";
	private static Integer emailPriority = 3;
	private static String ccRecipients = null;
	private static String bccRecipients = null;

	private static String username = null;
	private static String password = null;

	/**********************************************************************************************
	 * Sets the mail priority from the default (normal) priority. Values: 1 = high,
	 * 3 = normal (default), 5 = low
	 * 
	 * @param Integer
	 *            mailPriority - {@link Integer} - The priority integer.
	 * @author Brandon Clark created March 9, 2016
	 * @version 1.0 March 9, 2016
	 ***********************************************************************************************/
	public static void setMailPriority(Integer mailPriority) {
		emailPriority = mailPriority;
	}

	/**********************************************************************************************
	 * Adds CC recipient(s) to the outgoing email.
	 * 
	 * @param String
	 *            ccRecipients - {@link String} - Comma delimited string of cc
	 *            recipients (no spaces).
	 * @author Brandon Clark created July 27, 2016
	 * @version 1.0 July 27, 2016
	 ***********************************************************************************************/
	public static void addCCRecipients(String ccRecipients) {
		Emailer.ccRecipients = ccRecipients;
	}

	/**********************************************************************************************
	 * Adds BCC recipient(s) to the outgoing email.
	 * 
	 * @param String
	 *            ccRecipients - {@link String} - Comma delimited string of bcc
	 *            recipients (no spaces).
	 * @author Brandon Clark created July 27, 2016
	 * @version 1.0 July 27, 2016
	 ***********************************************************************************************/
	public static void addBCCRecipients(String bccRecipients) {
		Emailer.bccRecipients = bccRecipients;
	}

	public static void setAuthCredentials(String username, String password) {
		Emailer.username = username;
		Emailer.password = password;
	}

	public static void setRelay(String relayHost, String relayPort) {
		emailRelayHost = relayHost;
		emailRelayPort = relayPort;
	}

	/**********************************************************************************************
	 * Sends an email on the internal viacom network.
	 * 
	 * @param String
	 *            fromAddress - {@link String} - The desired "from" address of the
	 *            email.
	 * @param String
	 *            toAddress - {@link String} - Comma separated list of recipients
	 *            for the email.
	 * @param String
	 *            subject - {@link String} - The email subject.
	 * @param String
	 *            htmlMessage - {@link String} - The html formatted message/body of
	 *            the email.
	 * @author Brandon Clark created February 1, 2016
	 * @version 1.0 February 1, 2016
	 ***********************************************************************************************/
	public static void sendEmail(String fromAddress, String toAddress, String subject, String htmlMessage) {
		try {
			Boolean authRequired = username != null ? true : false;
			Properties props = new Properties();
			props.put("mail.smtp.auth", authRequired);
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", emailRelayHost);
			props.put("mail.smtp.port", emailRelayPort);
			props.put("mail.smtp.ssl.trust", "*");

			Session session = Session.getInstance(props);
			if (username != null) {
				session = Session.getInstance(props, new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password);
					}
				});
			}

			Message message = new MimeMessage(session);

			message.setFrom(new InternetAddress(fromAddress));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));

			if (ccRecipients != null) {
				message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(ccRecipients));
			}

			if (bccRecipients != null) {
				message.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(bccRecipients));
			}

			message.setSubject(subject);
			message.setHeader("X-Priority", emailPriority.toString());

			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(htmlMessage, "text/html");

			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			messageBodyPart = new MimeBodyPart();

			message.setContent(multipart);
			Transport.send(message);
			Logger.logConsoleMessage("Successfully sent email to '" + toAddress + "' recipient(s).");
		} catch (Exception e) {
			Logger.logConsoleMessage("Failed to send report auto email.");
			e.printStackTrace();
		}
	}
}