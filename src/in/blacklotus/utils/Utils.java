package in.blacklotus.utils;

import java.awt.AWTException;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import in.blacklotus.YahooFinanceApp;

public class Utils {

	public static final List<Long> times = Arrays.asList(TimeUnit.DAYS.toMillis(365), TimeUnit.DAYS.toMillis(30),
			TimeUnit.DAYS.toMillis(1), TimeUnit.HOURS.toMillis(1), TimeUnit.MINUTES.toMillis(1),
			TimeUnit.SECONDS.toMillis(1));

	public static final List<String> timesString = Arrays.asList("year", "month", "day", "hour", "minute", "second");

	public static String toDuration(long duration) {

		long seconds = duration / 1000;
		
		long minutes = seconds / 60;
		
		long hours = minutes / 60;
		
		long days = hours / 24;
		
		hours = hours % 24;
		
		minutes = minutes % 60;
		
		seconds = seconds % 60;
		
		StringBuffer sb = new StringBuffer();
		
		if(days > 0) {
		
			sb.append(days).append(days == 1 ? " day " : " days ");
		} 
		
		if(hours > 0) {
		
			sb.append(hours).append(hours == 1 ? " hour " : " hours ");
		}
		
		if(minutes > 0) {
			
			sb.append(minutes).append(minutes == 1 ? " minute " : " minutes ");
		}
		
		if(seconds > 0) {
			
			sb.append(seconds).append(seconds == 1 ? " second " : " seconds ");
		}
		
		return sb.toString(); 
	}

	public static void sendEmail(String body) {

		String to = "gopiparimi@gmail.com";

		Properties props = new Properties();

		props.put("mail.smtp.host", "smtp.gmail.com");

		props.put("mail.smtp.socketFactory.port", "465");

		props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

		props.put("mail.smtp.auth", "true");

		props.put("mail.smtp.port", "465");

		Session session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {

			protected PasswordAuthentication getPasswordAuthentication() {

				return new PasswordAuthentication("nadopicks@gmail.com", "IpikTradePi*");
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
		// Image image =
		// Toolkit.getDefaultToolkit().createImage(getClass().getResource("icon.png"));

		TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");

		trayIcon.setImageAutoSize(true);

		trayIcon.setToolTip("System tray icon demo");

		tray.add(trayIcon);

		trayIcon.displayMessage("Hello, World", "notification demo", MessageType.INFO);
	}

	public static void displayTray(String[] headers, String[][] data) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					YahooFinanceApp window = new YahooFinanceApp();
					window.show(headers, data);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static double round(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
	
	public static String formattedNumber(long number) {
		return NumberFormat.getNumberInstance(Locale.US).format(number);
	}
	
	public static String formattedVolume(long volume) {
		
		if(volume > 999999) {
		
			return NumberFormat.getNumberInstance(Locale.US).format(round(volume * 1.00 / 1000000)) + "M";
		
		} else {
		
			return NumberFormat.getNumberInstance(Locale.US).format(volume);
		}
	}

}
