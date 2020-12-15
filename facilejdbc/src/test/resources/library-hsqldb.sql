CREATE TABLE book(
    id BIGINT IDENTITY PRIMARY KEY,
	published_at DATE,
	title VARCHAR(255) NOT NULL,
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
    book_id BIGINT REFERENCES book(id),
    author_id BIGINT REFERENCES author(id),

    CONSTRAINT c_book_author_id UNIQUE (book_id, author_id)
);

CREATE TABLE test_table(
    id BIGINT IDENTITY PRIMARY KEY,
    string_value VARCHAR(255),
    int_value INT,
    float_value DOUBLE
);
