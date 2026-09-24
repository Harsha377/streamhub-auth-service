CREATE TABLE  users(
    id BIGINT GENERATED ALWAYS AS IDENTITY  PRIMARY KEY ,
    mobile_number varchar(15) NOT NULL UNIQUE ,
    full_name VARCHAR(100),
    email VARCHAR(255),
    date_of_birth DATE,
    created_at TIMESTAMP NOT NULL ,
    updated_at TIMESTAMP NOT NULL
);