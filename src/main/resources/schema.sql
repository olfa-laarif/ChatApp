-- ─────────────────────────────────────────
-- USERS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id           CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    password     VARCHAR(255) NOT NULL UNIQUE,
    phone_number VARCHAR(20)  NOT NULL UNIQUE,
    email        VARCHAR(255) NOT NULL UNIQUE,
    username     VARCHAR(100) NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_users_phone_number (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─────────────────────────────────────────
-- FRIENDSHIPS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS friendships (
    id           CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    requester_id CHAR(36)     NOT NULL,
    receiver_id  CHAR(36)     NOT NULL,
    status       ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_friendship_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendship_receiver  FOREIGN KEY (receiver_id)  REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT no_self_friend          CHECK (requester_id <> receiver_id),
    CONSTRAINT one_pending_per_pair    UNIQUE (requester_id, receiver_id),

    INDEX idx_friendships_receiver_id (receiver_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─────────────────────────────────────────
-- CONVERSATIONS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS conversations (
    id              CHAR(36)  PRIMARY KEY DEFAULT (UUID()),
    user1_id        CHAR(36)  NOT NULL,
    user2_id        CHAR(36)  NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_conversation_user1        FOREIGN KEY (user1_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_user2        FOREIGN KEY (user2_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT no_self_conversation         CHECK (user1_id <> user2_id),
    CONSTRAINT one_conversation_per_pair    UNIQUE (user1_id, user2_id),

    INDEX idx_conversations_last_message_at (last_message_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─────────────────────────────────────────
-- MESSAGES
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS messages (
    id              CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    conversation_id CHAR(36)     NOT NULL,
    sender_id       CHAR(36)     NOT NULL,
    content         VARCHAR(500) NULL,
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_sender       FOREIGN KEY (sender_id)       REFERENCES users(id)         ON DELETE CASCADE,

    INDEX idx_messages_conversation_id (conversation_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─────────────────────────────────────────
-- MESSAGE EDIT HISTORY
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS message_edit_history (
    id               CHAR(36)     PRIMARY KEY DEFAULT (UUID()),
    message_id       CHAR(36)     NOT NULL,
    original_content VARCHAR(500) NOT NULL,
    new_content      VARCHAR(500) NOT NULL,
    edited_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_edit_history_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─────────────────────────────────────────
-- ATTACHMENTS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS attachments (
    id         CHAR(36)                  PRIMARY KEY DEFAULT (UUID()),
    message_id CHAR(36)                  NOT NULL UNIQUE,
    filename   VARCHAR(255)              NOT NULL,
    url        TEXT                      NOT NULL,
    mime_type  VARCHAR(100)              NOT NULL,
    type       ENUM('IMAGE', 'PDF')      NOT NULL,
    size_bytes INT                       NOT NULL,
    is_deleted BOOLEAN                   NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP                 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attachment_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT chk_attachment_size   CHECK (size_bytes <= 20971520)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─────────────────────────────────────────
-- MESSAGE LOGS (admin)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS message_logs (
    id         CHAR(36)                       PRIMARY KEY DEFAULT (UUID()),
    message_id CHAR(36)                       NOT NULL,
    user_id    CHAR(36)                       NOT NULL,
    action     ENUM('SENT', 'EDITED', 'DELETED') NOT NULL,
    created_at TIMESTAMP                      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_message_log_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_log_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,

    INDEX idx_message_logs_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ─────────────────────────────────────────
-- FILE LOGS (admin)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS file_logs (
    id            CHAR(36)                  PRIMARY KEY DEFAULT (UUID()),
    attachment_id CHAR(36)                  NOT NULL,
    user_id       CHAR(36)                  NOT NULL,
    action        ENUM('SENT', 'DELETED')   NOT NULL,
    created_at    TIMESTAMP                 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_file_log_attachment FOREIGN KEY (attachment_id) REFERENCES attachments(id) ON DELETE CASCADE,
    CONSTRAINT fk_file_log_user       FOREIGN KEY (user_id)       REFERENCES users(id)       ON DELETE CASCADE,

    INDEX idx_file_logs_attachment_id (attachment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;