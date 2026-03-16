CREATE TABLE roles (
                       id UUID NOT NULL,
                       name VARCHAR(255) NOT NULL UNIQUE,
                       PRIMARY KEY (id)
);

CREATE TABLE users (
                       id UUID NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255),
                       first_name VARCHAR(255),
                       last_name VARCHAR(255),
                       active BOOLEAN NOT NULL,
                       created_at TIMESTAMP(6) WITH TIME ZONE,
                       updated_at TIMESTAMP(6) WITH TIME ZONE,
                       PRIMARY KEY (id)
);

CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id UUID NOT NULL,
                            PRIMARY KEY (role_id, user_id),
                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
                            CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

INSERT INTO roles (id, name) VALUES (gen_random_uuid(), 'ROLE_ADMIN');
INSERT INTO roles (id, name) VALUES (gen_random_uuid(), 'ROLE_COURIER');
INSERT INTO roles (id, name) VALUES (gen_random_uuid(), 'ROLE_USER');