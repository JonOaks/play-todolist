# Add User
 
# --- !Ups

ALTER TABLE task ADD author_login VARCHAR(255);

CREATE SEQUENCE task_user_id_seq;
CREATE TABLE task_user (
      id integer NOT NULL DEFAULT nextval('task_user_id_seq'),
      login varchar(255) NOT NULL,
      constraint pk_task_user PRIMARY KEY (login)

);

ALTER TABLE task ADD constraint fk_task_task_user_1 FOREIGN KEY (author_login) REFERENCES task_user (login) ON DELETE restrict ON UPDATE restrict;

INSERT into task_user (login) values ('McQuack');
INSERT into task_user (login) values ('Jonatan');

INSERT into task (label,author_login) values ('Launchpad','McQuack');
INSERT into task (label,author_login) values ('Threshold','McQuack');

# --- !Downs
ALTER TABLE task DROP author_login;

DROP TABLE task_user;
DROP SEQUENCE task_user_id_seq;
