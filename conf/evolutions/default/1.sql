# --- First database schema

# --- !Ups

set ignorecase true;

create table website (
  id                        bigint auto_increment,
  views                      bigint not null,
  constraint pk_website primary key (id))
;

# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists website;

SET REFERENTIAL_INTEGRITY TRUE;
