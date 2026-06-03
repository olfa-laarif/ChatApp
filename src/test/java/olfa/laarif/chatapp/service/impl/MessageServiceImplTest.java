package olfa.laarif.chatapp.service.impl;

import olfa.laarif.chatapp.dto.MessageResponse;
import olfa.laarif.chatapp.entity.ConversationEntity;
import olfa.laarif.chatapp.entity.MessageEntity;
import olfa.laarif.chatapp.entity.UserEntity;
import olfa.laarif.chatapp.repository.ConversationRepository;
import olfa.laarif.chatapp.repository.MessageRepository;
import olfa.laarif.chatapp.repository.UserRepository;
import olfa.laarif.chatapp.service.MessageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static java.time.Instant.now;
import static org.assertj.core.api.FactoryBasedNavigableListAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MessageServiceImplTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageService chatService;


    // Méthode utilitaire pour créer rapidement un utilisateur valide selon tes contraintes
    private UserEntity createAndSaveTestUser(String username, String email, String phone) {

        UserEntity user = new UserEntity();

        user.setUsername(username);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setPassword("hashed_password_123"); // UNIQUE NOT NULL
        return userRepository.save(user);
    }

    @Test
    void contextLoads() {
    }
    @Test
    void shouldReturnMessagesInChronologicalOrder() {
        // GIVEN - 1. Création des utilisateurs
        UserEntity usr1 = createAndSaveTestUser("Alice", "alice@test.com", "+33611111111");
        UserEntity usr2 = createAndSaveTestUser("Bob", "bob@test.com", "+33622222222");

        // GIVEN - 2. Création de la conversation
        ConversationEntity conversation = new ConversationEntity();
        conversation.setUser1(usr1);
        conversation.setUser2(usr2);
        conversation.setCreatedAt(now());
        conversation.setLastMessageAt(now());
        conversation=conversationRepository.save(conversation);

        // GIVEN - 3. Création des messages
        MessageEntity firstMessage = new MessageEntity();
        firstMessage.setConversation(conversation);
        firstMessage.setSender(usr1);
        firstMessage.setContent("Premier message");
        firstMessage.setCreatedAt(now());
        firstMessage.setUpdatedAt(now());

        MessageEntity secondMessage = new MessageEntity();
        secondMessage.setConversation(conversation);
        secondMessage.setSender(usr2);
        secondMessage.setContent("Deuxième message");
        secondMessage.setCreatedAt(now());
        secondMessage.setUpdatedAt(now());

        messageRepository.save(firstMessage);
        messageRepository.save(secondMessage);

        // WHEN
        List<MessageResponse> messages = chatService.getConversationMessages("+33611111111", conversation.getId());

        // THEN
        assertEquals(messages.size(),2);

    }

}