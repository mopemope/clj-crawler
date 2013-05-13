CREATE EXTENSION pg_trgm;

DROP TABLE bbs;

CREATE TABLE bbs (
    url varchar(256) primary key,
    title varchar(256) not null
);

DROP TABLE threads;
DROP INDEX threads_idx_1;
DROP INDEX threads_idx_2;

CREATE TABLE threads (
    url varchar(256) primary key,
    board_url varchar(256) not null,
    title varchar(256) not null,
    res_count smallint not null
);

CREATE INDEX threads_idx_1 ON threads (board_url);
CREATE INDEX threads_idx_2 ON threads USING gin (title gin_trgm_ops);

DROP TABLE comments;
DROP INDEX comments_idx_1;
DROP INDEX comments_idx_2;

CREATE TABLE comments (
    no smallint not null,
    url varchar(256) not null,
    board_url varchar(256) not null,
    title varchar(256) not null,
    handle varchar(256) not null,
    mailto varchar(256) not null,
    date varchar(256) not null,
    comment text not null,
    constraint comments_pkey primary key (no, url)
);

CREATE INDEX comments_idx_1 ON comments (board_url);
CREATE INDEX comments_idx_2 ON comments USING gin (comment gin_trgm_ops);

