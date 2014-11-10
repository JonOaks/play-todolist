# Add Category
 
# --- !Ups

CREATE SEQUENCE category_id_seq;
CREATE TABLE category (
      id integer NOT NULL DEFAULT nextval('category_id_seq'),
      category varchar(255) NOT NULL,
      constraint pk_category PRIMARY KEY (category)
);

CREATE SEQUENCE user_category_id_seq;
CREATE TABLE user_category (
      id integer NOT NULL DEFAULT nextval('user_category_id_seq'),
      login varchar(255) NOT NULL,
      category varchar(255) NOT NULL,
      constraint pk_user_category PRIMARY KEY (login,category)
);

CREATE SEQUENCE task_category_id_seq;
CREATE TABLE task_category (
      id integer NOT NULL DEFAULT nextval('task_category_id_seq'),
      task_id varchar(255) NOT NULL,
      category varchar(255) NOT NULL,
      constraint pk_task_category PRIMARY KEY(task_id,category)
);

ALTER TABLE user_category ADD constraint fk_user_category_task_user FOREIGN KEY (login) REFERENCES task_user (login) ON DELETE restrict ON UPDATE restrict;
ALTER TABLE user_category ADD constraint fk_user_category_category FOREIGN KEY (category) REFERENCES category (category) ON DELETE restrict ON UPDATE restrict;

ALTER TABLE task_category ADD constraint fk_task_category_task FOREIGN KEY (task_id) REFERENCES task (id) ON DELETE restrict ON UPDATE restrict;
ALTER TABLE task_category ADD constraint fk_task_category_category FOREIGN KEY (category) REFERENCES category (category) ON DELETE restrict ON UPDATE restrict;

INSERT into category (category) values ('Adventure');
INSERT into user_category (login,category) values ('McQuack','Adventure');
INSERT into task (label,author_login) values ('Testing','McQuack');
INSERT into task_category (task_id,category) values (4,'Adventure');

# --- !Downs

DROP TABLE category;
DROP SEQUENCE category_id_seq;


DROP TABLE user_category;
DROP SEQUENCE user_category_id_seq;

DROP TABLE task_category;
DROP SEQUENCE task_category_id_seq;
