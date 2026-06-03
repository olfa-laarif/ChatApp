-- Add the conversation name, used only for groups.
-- Nullable: DIRECT conversations (1-to-1) don't carry a name — the other
-- member is used as the UI identifier instead.
ALTER TABLE conversations
    ADD COLUMN name VARCHAR(100) NULL;