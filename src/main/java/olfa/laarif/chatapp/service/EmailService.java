package olfa.laarif.chatapp.service;

import olfa.laarif.chatapp.dto.notification.FriendRequestAcceptedNotification;
import olfa.laarif.chatapp.dto.notification.FriendRequestNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    public void sendFriendRequestEmail(String toEmail, String toUsername, FriendRequestNotification notif) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("Nouvelle demande d'ami - ChatApp");
        message.setText(String.format(
                "Bonjour %s,%n%n%s vous a envoyé une demande d'ami.%n%nConnectez-vous pour y répondre.",
                toUsername, notif.getRequesterUsername()
        ));
        mailSender.send(message);
    }

    public void sendFriendRequestAcceptedEmail(String toEmail, String toUsername, FriendRequestAcceptedNotification notif) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("Demande d'ami acceptée - ChatApp");
        message.setText(String.format(
                "Bonjour %s,%n%n%s a accepté votre demande d'ami.%n%nConnectez-vous pour commencer à discuter !",
                toUsername, notif.getAccepterUsername()
        ));
        mailSender.send(message);
    }
}
