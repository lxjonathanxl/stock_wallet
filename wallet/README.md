# WalletApi

## Table of Contents

- [Entities](#entities)
- [Controllers and Endpoints](#controllers-and-endpoints)

## Entities
Take a look at the entities diagram that describes their interactions.
![DBDiagram drawio](https://github.com/user-attachments/assets/2b7f078b-16f9-4b12-88c6-8a9c011d7213)

### Users

The `Users` entity represents the users of the application. Each user has a unique ID, a username, a password, an email, and a cash balance. The `Users` class implements `UserDetails` to integrate with Spring Security for authentication and authorization purposes.

- **ID**: Unique identifier for the user.
- **Username**: The name used for login.
- **Password**: Hashed password for authentication.
- **Email**: User's email address.
- **Cash**: User's available cash balance, defaulting to 10000.00.

### Stocks

The `Stocks` entity represents the stocks owned by users. Each stock has a unique stock ID, is associated with a user, and has a quantity and name.

- **Stock ID**: Unique identifier for the stock.
- **User**: The user who owns the stock.
- **Quant**: The quantity of stocks owned.
- **Name**: The name of the stock.

### History

The `History` entity records the history of stock transactions made by users. Each history entry has a unique ID, is associated with a user, and includes details such as the action performed, the quantity of stocks, the name of the stock, the date of the transaction, and the price.

- **ID**: Unique identifier for the history entry.
- **User**: The user who performed the action.
- **Action**: The action performed (e.g., "buy" or "sell").
- **Quant**: The quantity of stocks involved in the action.
- **Name**: The name of the stock.
- **Date**: The date the action was performed.
- **Price**: The price of the stock at the time of the action.

## Controllers and Endpoints

### RegistrationController

The `RegistrationController` handles the registration of new users. It provides endpoints for both displaying the registration page and handling form submissions.

- **GET /register**: Displays the registration form.
  - **Response**: Returns the registration HTML page.

- **POST /register**: Handles the submission of the registration form.
  - **Request Body**: Accepts a `RegistrationRequest` object containing the username, email, password, and password confirmation.
    ```form
    {
        "username": "exampleUser",
        "email": "user@example.com",
        "password": "Password123!",
        "confirmation": "Password123!"
    }
    ```
  - **Response**: Redirects to the registration page on error with an appropriate message, or redirects to the login page on successful registration.

The `RegistrationRequest` DTO used in the controller contains the following fields:
- **Username**: Must not be empty.
- **Email**: Must be a valid email format and not empty.
- **Password**: Must meet the specified criteria (at least one uppercase letter, one lowercase letter, one number, one special character, and be at least 8 characters long).
- **Confirmation**: Must match the password.

Validation is performed on the server side, and any errors result in appropriate feedback to the user.
