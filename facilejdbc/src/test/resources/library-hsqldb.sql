CREATE TABLE book(
    id BIGINT IDENTITY PRIMARY KEY,
	published_at DATE,
	title VARCHAR(255) NOT NULL,
	language VARCHAR(255),
	isbn VARCHAR(255),
	pages INT
);

CREATE TABLE author(
	id BIGINT IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    birth_day DATE,

    CONSTRAINT c_author_name UNIQUE (name)
);

CREATE TABLE book_author(
    book_id BIGINT NOT NULL REFERENCES book(id),
    author_id BIGINT NOT NULL REFERENCES author(id),

    CONSTRAINT c_book_author_id UNIQUE (book_id, author_id)
);

CREATE TABLE location(
	id BIGINT IDENTITY PRIMARY KEY,

	lat DOUBLE NOT NULL,
	lon DOUBLE NOT NULL,

	ele DOUBLE,
	created_at TIMESTAMP(3),
	speed DOUBLE,
	magvar DOUBLE,
	geoidheight DOUBLE,
	name VARCHAR(255),
	cmt VARCHAR(255),
	dscr VARCHAR(2024),
	src VARCHAR(255),
	sym VARCHAR(255),
	type VARCHAR(255),
	fix VARCHAR(10),
	sat INT,
	hdop DOUBLE,
	vdop DOUBLE,
	pdop DOUBLE,
	ageofdgpsdata INT,
	dgpsid INT,
	course DOUBLE,
	extensions VARCHAR(2024)
);
