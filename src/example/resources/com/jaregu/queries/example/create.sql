-- person
create table person (id int, 
first_name varchar(100), 
last_name varchar(100),
PRIMARY KEY (id));

-- team
create table team (id int,
name varchar(100),
PRIMARY KEY(id));

-- team_person
create table team_person (id int,
team_id int,
person_id int,
PRIMARY KEY(id),
CONSTRAINT FK_team_person_team FOREIGN KEY (team_id) REFERENCES team (id),
CONSTRAINT FK_team_person_person FOREIGN KEY (person_id) REFERENCES person (id));
