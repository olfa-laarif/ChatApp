-- Passage du modèle conversation 1-to-1 (user1_id / user2_id) au modèle groupe
-- (table conversation_members + colonne conversation_type).
--
-- ⚠️ Cette migration est destructive sur les conversations existantes :
-- les colonnes user1_id et user2_id sont supprimées sans data migration vers
-- conversation_members. Acceptable pour un projet en cours de développement.
-- En prod réelle, prévoir un script de bascule des données avant le DROP.

-- 1. Supprimer les contraintes liées à user1_id / user2_id sur conversations
ALTER TABLE conversations DROP FOREIGN KEY fk_conversation_user1;
ALTER TABLE conversations DROP FOREIGN KEY fk_conversation_user2;
ALTER TABLE conversations DROP CHECK no_self_conversation;
ALTER TABLE conversations DROP INDEX one_conversation_per_pair;

-- 2. Supprimer les colonnes user1_id et user2_id
ALTER TABLE conversations DROP COLUMN user1_id;
ALTER TABLE conversations DROP COLUMN user2_id;

-- 3. Ajouter le type de conversation (DIRECT par défaut pour rétro-compat)
ALTER TABLE conversations
    ADD COLUMN conversation_type ENUM('DIRECT', 'GROUP') NOT NULL DEFAULT 'DIRECT';

-- 4. Créer la nouvelle table conversation_members
CREATE TABLE IF NOT EXISTS conversation_members (
    id              CHAR(36)  PRIMARY KEY DEFAULT (UUID()),
    conversation_id CHAR(36)  NOT NULL,
    user_id         CHAR(36)  NOT NULL,
    joined_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_member_conversation   FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_user           FOREIGN KEY (user_id)         REFERENCES users(id)         ON DELETE CASCADE,
    CONSTRAINT unique_conversation_user UNIQUE (conversation_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;