-- PostgreSQL Database Creation and Seeding Scripts

-- Create Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    is_ldap BOOLEAN DEFAULT FALSE,
    is_locked BOOLEAN DEFAULT FALSE,
    is_enabled BOOLEAN DEFAULT TRUE,
    password_reset_required BOOLEAN DEFAULT FALSE,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Roles Table
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- User-Roles Join Table
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Create Phases Table
CREATE TABLE IF NOT EXISTS phases (
    id BIGSERIAL PRIMARY KEY,
    phase_number INT UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL
);

-- Create Document Deliverables Metadata Table
CREATE TABLE IF NOT EXISTS document_deliverables (
    id BIGSERIAL PRIMARY KEY,
    doc_id VARCHAR(50) UNIQUE NOT NULL, -- e.g. P001, P002
    name VARCHAR(255) NOT NULL,
    description TEXT,
    version VARCHAR(20) DEFAULT '1.0',
    doc_code VARCHAR(100),
    phase_id BIGINT REFERENCES phases(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Documents Processing Status and Submission Table
CREATE TABLE IF NOT EXISTS document_submissions (
    id BIGSERIAL PRIMARY KEY,
    doc_deliverable_id BIGINT REFERENCES document_deliverables(id) ON DELETE SET NULL,
    app_code VARCHAR(3) NOT NULL, -- 3-character application code e.g. APP, CRM
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(512),
    status VARCHAR(50) DEFAULT 'PENDING_APPROVAL', -- PENDING_APPROVAL, APPROVED, REJECTED
    maker_username VARCHAR(100) NOT NULL,
    checker_username VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP
);

-- INSERT SEED DATA FOR ROLES
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_MAKER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_CHECKER') ON CONFLICT (name) DO NOTHING;

-- INSERT DEFAULT USERS (Password is 'password' BCrypt hashed)
-- Admin
INSERT INTO users (username, password, email, is_ldap, is_locked, is_enabled)
VALUES ('admin', '$2a$12$R6lSgS9p53N.gshdImsaKOfO2Z1vS7D/Esh4vXwzK0W6T/K18W93i', 'admin@cth.com', FALSE, FALSE, TRUE)
ON CONFLICT (username) DO NOTHING;

-- Maker
INSERT INTO users (username, password, email, is_ldap, is_locked, is_enabled)
VALUES ('maker', '$2a$12$R6lSgS9p53N.gshdImsaKOfO2Z1vS7D/Esh4vXwzK0W6T/K18W93i', 'maker@cth.com', FALSE, FALSE, TRUE)
ON CONFLICT (username) DO NOTHING;

-- Checker (Approver)
INSERT INTO users (username, password, email, is_ldap, is_locked, is_enabled)
VALUES ('checker', '$2a$12$R6lSgS9p53N.gshdImsaKOfO2Z1vS7D/Esh4vXwzK0W6T/K18W93i', 'checker@cth.com', FALSE, FALSE, TRUE)
ON CONFLICT (username) DO NOTHING;

-- Map users to roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username='admin' AND r.name IN ('ROLE_ADMIN', 'ROLE_MAKER', 'ROLE_CHECKER')
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username='maker' AND r.name = 'ROLE_MAKER'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username='checker' AND r.name = 'ROLE_CHECKER'
ON CONFLICT DO NOTHING;

-- SEED THE 7 SDLC PHASES
INSERT INTO phases (phase_number, name) VALUES (1, 'Prioritisation') ON CONFLICT DO NOTHING;
INSERT INTO phases (phase_number, name) VALUES (2, 'Pre-project') ON CONFLICT DO NOTHING;
INSERT INTO phases (phase_number, name) VALUES (3, 'Analysis') ON CONFLICT DO NOTHING;
INSERT INTO phases (phase_number, name) VALUES (4, 'Design') ON CONFLICT DO NOTHING;
INSERT INTO phases (phase_number, name) VALUES (5, 'Testing') ON CONFLICT DO NOTHING;
INSERT INTO phases (phase_number, name) VALUES (6, 'Execution / SIT / UAT') ON CONFLICT DO NOTHING;
INSERT INTO phases (phase_number, name) VALUES (7, 'Deployment') ON CONFLICT DO NOTHING;
