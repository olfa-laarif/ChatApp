-- ─────────────────────────────────────────
-- USERS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
    id           CHAR(36)     PRIMARY KEY,
    password     VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20)  NOT NULL UNIQUE,
    email        VARCHAR(255) NOT NULL UNIQUE,
    username     VARCHAR(100) NOT NULL,
    role         VARCHAR(20)  NOT NULL DEFAULT 'USER', -- Remplacement ENUM
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_users_phone_number (phone_number)
    );

-- ─────────────────────────────────────────
-- FRIENDSHIPS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS friendships (
    id           CHAR(36)     PRIMARY KEY,
    requester_id CHAR(36)     NOT NULL,
    receiver_id  CHAR(36)     NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING', -- Remplacement de ENUM par VARCHAR pour la portabilité H2
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_friendship_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendship_receiver  FOREIGN KEY (receiver_id)  REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT no_self_friend          CHECK (requester_id <> receiver_id),
    CONSTRAINT one_pending_per_pair    UNIQUE (requester_id, receiver_id),

    INDEX idx_friendships_receiver_id (receiver_id, status)
    );

-- ─────────────────────────────────────────
-- CONVERSATIONS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS conversations (
    id              CHAR(36)  PRIMARY KEY,
    conversation_type         ENUM('DIRECT', 'GROUP') NOT NULL DEFAULT 'DIRECT',
    name            VARCHAR(100) NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_message_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_conversations_last_message_at (last_message_at DESC)
    );


CREATE TABLE IF NOT EXISTS conversation_members (
    id              CHAR(36)  PRIMARY KEY,
    conversation_id CHAR(36)  NOT NULL,
    user_id         CHAR(36)  NOT NULL,
    joined_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_member_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_user         FOREIGN KEY (user_id)         REFERENCES users(id)         ON DELETE CASCADE,
    CONSTRAINT unique_conversation_user UNIQUE (conversation_id, user_id)
    );

-- ─────────────────────────────────────────
-- MESSAGES
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS messages (
    id              CHAR(36)     PRIMARY KEY,
    conversation_id CHAR(36)     NOT NULL,
    sender_id       CHAR(36)     NOT NULL,
    content         VARCHAR(500) NULL,
    is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_message_conversation FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_sender       FOREIGN KEY (sender_id)       REFERENCES users(id)         ON DELETE CASCADE,

    INDEX idx_messages_conversation_id (conversation_id, created_at DESC)
    );

-- ─────────────────────────────────────────
-- MESSAGE EDIT HISTORY
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS message_edit_history (
    id               CHAR(36)     PRIMARY KEY,
    message_id       CHAR(36)     NOT NULL,
    original_content VARCHAR(500) NOT NULL,
    new_content      VARCHAR(500) NOT NULL,
    edited_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_edit_history_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE
    );

-- ─────────────────────────────────────────
-- ATTACHMENTS
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS attachments (
    id         CHAR(36)                  PRIMARY KEY,
    message_id CHAR(36)                  NOT NULL UNIQUE,
    filename   VARCHAR(255)              NOT NULL,
    url        TEXT                      NOT NULL,
    mime_type  VARCHAR(100)              NOT NULL,
    type       VARCHAR(20)               NOT NULL, -- Remplacement ENUM
    size_bytes INT                       NOT NULL,
    is_deleted BOOLEAN                   NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP                 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attachment_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT chk_attachment_size   CHECK (size_bytes <= 20971520)
    );

-- ─────────────────────────────────────────
-- MESSAGE LOGS (admin)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS message_logs (
    id         CHAR(36)                       PRIMARY KEY,
    message_id CHAR(36)                       NOT NULL,
    user_id    CHAR(36)                       NOT NULL,
    action     VARCHAR(20)                    NOT NULL, -- Remplacement ENUM
    created_at TIMESTAMP                      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_message_log_message FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_log_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,

    INDEX idx_message_logs_message_id (message_id)
    );

-- ─────────────────────────────────────────
-- FILE LOGS (admin)
-- ─────────────────────────────────────────
CREATE TABLE IF NOT EXISTS file_logs (
    id            CHAR(36)                  PRIMARY KEY,
    attachment_id CHAR(36)                  NOT NULL,
    user_id       CHAR(36)                  NOT NULL,
    action        VARCHAR(20)               NOT NULL, -- Remplacement ENUM
    created_at    TIMESTAMP                 NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_file_log_attachment FOREIGN KEY (attachment_id) REFERENCES attachments(id) ON DELETE CASCADE,
    CONSTRAINT fk_file_log_user       FOREIGN KEY (user_id)       REFERENCES users(id)       ON DELETE CASCADE,

    INDEX idx_file_logs_attachment_id (attachment_id)
    );