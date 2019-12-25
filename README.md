# Money transfer REST API


## To run app

1) mvn package
2) java -jar target\RvHome-jar-with-dependencies.jar

It will run on port 4567.

## API description

GET /accounts - to get all accounts

GET /accounts/:id - to get account detail by id in path variable

POST /accounts - to create account.

>Query params:
>- owner
>- balance (must be numeric)
>- currency (ISO 4217 format)

GET /transactions - to get all transactions list

GET /transactions/:id - to get transaction detail by id in path variable

GET /transactions/account/:id - to get all transactions of specified account by id in path variable

POST /transactions - to create transaction

>Query params (all must be numeric):
>- debitAccountId
>- creditAccountId
>- amount
