create table if not exists address(
    id serial primary key,
    street text not null,
    neighborhood text not null,
    complement text not null,
    number int not null
);

create table if not exists agency(
    id serial primary key,
    name text not null,
    company_name text not null,
    cnpj text not null,
    address_id int references address
);