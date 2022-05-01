DROP TABLE IF EXISTS board;
DROP TABLE IF EXISTS game;

create table game
(
    id              int auto_increment
        primary key,
    room_name varchar(30) not null,
    password varchar(100) not null,
    state           varchar(30) not null
);

create table board
(
    id       int auto_increment
        primary key,
    symbol   varchar(30) not null,
    team     varchar(30) not null,
    position varchar(30) not null,
    game_id  int         not null,
    constraint game___fk
        foreign key (game_id) references game (id)
            on update cascade on delete cascade
);

insert into game (id, room_name, password, state) values (1, 'test', 'password', 'WHITE');
insert into board (symbol, team, position, game_id) values ('PAWN', 'WHITE', 'a2', 1);
