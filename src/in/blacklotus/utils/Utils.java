package in.blacklotus.utils;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Utils {

	public static final List<Long> times = Arrays.asList(TimeUnit.DAYS.toMillis(365), TimeUnit.DAYS.toMillis(30),
			TimeUnit.DAYS.toMillis(1), TimeUnit.HOURS.toMillis(1), TimeUnit.MINUTES.toMillis(1),
			TimeUnit.SECONDS.toMillis(1));

	public static final List<String> timesString = Arrays.asList("year", "month", "day", "hour", "minute", "second");

	public static String toDuration(long duration) {

		StringBuffer res = new StringBuffer();

		for (int i = 0; i < times.size() - 1; i++) {

			Long current = times.get(i);

			long temp = duration / current;

			duration = duration % current;

			if (temp > 0) {

				res.append(temp).append(" ").append(timesString.get(i))
						.append(duration > 0 ? " " + duration / 1000 + " seconds " : "");

				break;

			}
		}

		if ("".equals(res.toString()))

			return duration / 1000 + " seconds ";

		else

			return res.toString();
	}
	
	public static void sendEmail(String body) {
		
		String to = "ktdaiict@gmail.com";

		Properties props = new Properties();
		
		props.put("mail.smtp.host", "smtp.gmail.com");
		
		props.put("mail.smtp.socketFactory.port", "465");
		
		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		
		props.put("mail.smtp.auth", "true");
		
		props.put("mail.smtp.port", "465");

		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
		
			protected PasswordAuthentication getPasswordAuthentication() {
			
				return new PasswordAuthentication("teja.tangaturi@gmail.com", "Teja@123");
			}
		});

		try {
			
			MimeMessage message = new MimeMessage(session);
			
			message.setFrom(new InternetAddress("teja.tangaturi@gmail.com"));
			
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			
			message.setSubject("Hello");
			
			message.setText(body);

			Transport.send(message);

			System.out.println("message sent successfully");

		} catch (MessagingException e) {
			
			throw new RuntimeException(e);
		}
	}
	
	public static void displayTray() throws AWTException, MalformedURLException {
		
        SystemTray tray = SystemTray.getSystemTray();

        Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
      //Image image = Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

        TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
        
        trayIcon.setImageAutoSize(true);
        
        trayIcon.setToolTip("System tray icon demo");
        
        tray.add(trayIcon);

        trayIcon.displayMessage("Hello, World", "notification demo", MessageType.INFO);
    }

}
