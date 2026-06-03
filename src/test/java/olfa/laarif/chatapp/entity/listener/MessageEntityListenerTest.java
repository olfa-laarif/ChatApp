package olfa.laarif.chatapp.entity.listener;

import olfa.laarif.chatapp.entity.ConversationEntity;
import olfa.laarif.chatapp.entity.MessageEntity;
import olfa.laarif.chatapp.entity.MessageLogEntity;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.enums.MessageAction;
import olfa.laarif.chatapp.repository.ConversationRepository;
import olfa.laarif.chatapp.repository.MessageLogRepository;
import olfa.laarif.chatapp.repository.MessageRepository;
import olfa.laarif.chatapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test") // Utilise ton application-test.yml (avec H2)
@Transactional // Annule les insertions en BDD après chaque test
class MessageEntityListenerTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageLogRepository messageLogRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConversationRepository conversationRepository;
    private UserEntity usr1;
    private UserEntity usr2;
    private ConversationEntity conversation;
    MessageEntity message;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @BeforeEach
    void setUp() {
        // GIVEN - 1. Création des utilisateurs
        usr1 = createAndSaveTestUser("Alice", "alice@test.com", "+33611111111");
        usr2 = createAndSaveTestUser("Bob", "bob@test.com", "+33622222222");

        // GIVEN - 2. Création de la conversation
        conversation = new ConversationEntity();
        conversation.setUser1Id(usr1.getId());
        conversation.setUser2Id(usr2.getId());
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setLastMessageAt(LocalDateTime.now());
        conversation = conversationRepository.save(conversation);


    }

    private UserEntity createAndSaveTestUser(String username, String email, String phone) {

        UserEntity user = new UserEntity();

        user.setUsername(username);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setPassword("hashed_password_123"); // UNIQUE NOT NULL
        return userRepository.save(user);
    }


    @Test
    void  userSendANewMessage(){
        // GIVEN - 3. Création des messages
        message = new MessageEntity();
        message.setConversationId(conversation.getId());
        message.setSenderId(usr1.getId());
        message.setContent("Premier message");
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());
        message=messageRepository.save(message);
        // 2. Publication de l'événement
        eventPublisher.publishEvent(new MessageActionEvent(message, MessageAction.SENT));
        // THEN - On vérifie qu'un log d'action SENT a été créé
        List<MessageLogEntity> logs = messageLogRepository.findAll();

        assertThat(logs).hasSize(1);
        MessageLogEntity log = logs.get(0);
        assertThat(log.getMessageId()).isEqualTo(message.getId());
        assertThat(log.getUserId()).isEqualTo(usr1.getId());
        assertThat(log.getCreatedAt()).isNotNull();
    }

/*    @Test
    void shouldCreateLogWithEditedActionWhenMessageContentIsUpdated() {
        // GIVEN - Un message déjà existant en BDD
        MessageEntity message = new MessageEntity();
        message.setContent("Texte original");
        message.setSenderId(usr1.getId());
        message.setDeleted(false);
        message = messageRepository.saveAndFlush(message);

        // On vide le log de création (SENT) pour ne tester que le log de modification
        messageLogRepository.deleteAll();

        // WHEN - On modifie le contenu du message (déclenche @PostUpdate)
        message.setContent("Texte modifié !");
        messageRepository.saveAndFlush(message);

        // THEN - On vérifie qu'un log d'action EDITED a été généré
        List<MessageLogEntity> logs = messageLogRepository.findAll();

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getAction()).isEqualTo(MessageAction.EDITED);
        assertThat(logs.get(0).getMessageId()).isEqualTo(message.getId());
    }

    @Test
    void shouldCreateLogWithDeletedActionWhenMessageIsSoftDeleted() {
        // GIVEN - Un message existant en BDD
        MessageEntity message = new MessageEntity();
        message.setContent("Message à supprimer");
        message.setSenderId(usr1.getId());
        message.setDeleted(false);
        message = messageRepository.saveAndFlush(message);

        messageLogRepository.deleteAll();

        // WHEN - On passe le flag isDeleted à true (déclenche @PostUpdate)
        message.setDeleted(true);
        messageRepository.saveAndFlush(message);

        // THEN - On vérifie qu'un log d'action DELETED a été généré
        List<MessageLogEntity> logs = messageLogRepository.findAll();

        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getAction()).isEqualTo(MessageAction.DELETED);
    }*/
}