-- SQL Server (MS SQL) Database Creation and Seeding Scripts

-- Create Users Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[users]') AND type in (N'U'))
BEGIN
    CREATE TABLE users (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username VARCHAR(100) UNIQUE NOT NULL,
        password VARCHAR(255) NOT NULL,
        email VARCHAR(255),
        is_ldap BIT DEFAULT 0,
        is_locked BIT DEFAULT 0,
        is_enabled BIT DEFAULT 1,
        password_reset_required BIT DEFAULT 0,
        mfa_enabled BIT DEFAULT 0,
        created_at DATETIME DEFAULT GETDATE()
    );
END

-- Create Roles Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[roles]') AND type in (N'U'))
BEGIN
    CREATE TABLE roles (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name VARCHAR(50) UNIQUE NOT NULL
    );
END

-- User-Roles Join Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[user_roles]') AND type in (N'U'))
BEGIN
    CREATE TABLE user_roles (
        user_id BIGINT FOREIGN KEY REFERENCES users(id) ON DELETE CASCADE,
        role_id BIGINT FOREIGN KEY REFERENCES roles(id) ON DELETE CASCADE,
        PRIMARY KEY (user_id, role_id)
    );
END

-- Create Phases Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[phases]') AND type in (N'U'))
BEGIN
    CREATE TABLE phases (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        phase_number INT UNIQUE NOT NULL,
        name VARCHAR(100) NOT NULL
    );
END

-- Create Document Deliverables Metadata Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[document_deliverables]') AND type in (N'U'))
BEGIN
    CREATE TABLE document_deliverables (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        doc_id VARCHAR(50) UNIQUE NOT NULL, -- e.g. P001, P002
        name VARCHAR(255) NOT NULL,
        description NVARCHAR(MAX),
        version VARCHAR(20) DEFAULT '1.0',
        doc_code VARCHAR(100),
        phase_id BIGINT FOREIGN KEY REFERENCES phases(id) ON DELETE CASCADE,
        created_at DATETIME DEFAULT GETDATE()
    );
END

-- Create Documents Processing Status and Submission Table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[document_submissions]') AND type in (N'U'))
BEGIN
    CREATE TABLE document_submissions (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        doc_deliverable_id BIGINT FOREIGN KEY REFERENCES document_deliverables(id) ON DELETE SET NULL,
        app_code VARCHAR(3) NOT NULL, -- 3-character application code e.g. APP
        file_name VARCHAR(255) NOT NULL,
        file_path VARCHAR(512),
        status VARCHAR(50) DEFAULT 'PENDING_APPROVAL', -- PENDING_APPROVAL, APPROVED, REJECTED
        maker_username VARCHAR(100) NOT NULL,
        checker_username VARCHAR(100),
        created_at DATETIME DEFAULT GETDATE(),
        approved_at DATETIME
    );
END

-- INSERT SEED DATA FOR ROLES
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_ADMIN') INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_MAKER') INSERT INTO roles (name) VALUES ('ROLE_MAKER');
IF NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ROLE_CHECKER') INSERT INTO roles (name) VALUES ('ROLE_CHECKER');

-- INSERT DEFAULT USERS
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')
BEGIN
    INSERT INTO users (username, password, email, is_ldap, is_locked, is_enabled)
    VALUES ('admin', '$2a$12$R6lSgS9p53N.gshdImsaKOfO2Z1vS7D/Esh4vXwzK0W6T/K18W93i', 'admin@cth.com', 0, 0, 1);
END

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'maker')
BEGIN
    INSERT INTO users (username, password, email, is_ldap, is_locked, is_enabled)
    VALUES ('maker', '$2a$12$R6lSgS9p53N.gshdImsaKOfO2Z1vS7D/Esh4vXwzK0W6T/K18W93i', 'maker@cth.com', 0, 0, 1);
END

IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'checker')
BEGIN
    INSERT INTO users (username, password, email, is_ldap, is_locked, is_enabled)
    VALUES ('checker', '$2a$12$R6lSgS9p53N.gshdImsaKOfO2Z1vS7D/Esh4vXwzK0W6T/K18W93i', 'checker@cth.com', 0, 0, 1);
END

-- SEED THE 7 SDLC PHASES
IF NOT EXISTS (SELECT 1 FROM phases WHERE phase_number = 1) INSERT INTO phases (phase_number, name) VALUES (1, 'Prioritisation');
IF NOT EXISTS (SELECT 1 FROM phases WHERE phase_number = 2) INSERT INTO phases (phase_number, name) VALUES (2, 'Pre-project');
IF NOT EXISTS (SELECT 1 FROM phases WHERE phase_number = 3) INSERT INTO phases (phase_number, name) VALUES (3, 'Analysis');
IF NOT EXISTS (SELECT 1 FROM phases WHERE phase_number = 4) INSERT INTO phases (phase_number, name) VALUES (4, 'Design');
IF NOT EXISTS (SELECT 1 FROM phases WHERE phase_number = 5) INSERT INTO phases (phase_number, name) VALUES (5, 'Testing');
IF NOT EXISTS (SELECT 1 FROM phases WHERE phase_number = 6) INSERT INTO phases (phase_number, name) VALUES (6, 'Execution / SIT / UAT');
IF NOT EXISTS (SELECT 1 FROM phases WHERE phase_number = 7) INSERT INTO phases (phase_number, name) VALUES (7, 'Deployment');
