-- Ajout du champ role pour le système d'autorisation admin / user.
-- Tous les comptes existants sont initialisés à USER (DEFAULT).
ALTER TABLE users
    ADD COLUMN role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER';