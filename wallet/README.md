# WalletApi

## Table of Contents

- [Entities](#entities)
- [Controllers and Endpoints](#controllers-and-endpoints)
- [Kafka](#kafka)

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

### LoginController

The `LoginController` handles user login operations. It provides an endpoint for displaying the login page.

- **GET /login**: Displays the login form.
  - **Response**: Returns the login HTML form (`login.html`).

### HomeController

The `HomeController` manages the user's main dashboard, allowing them to view their stocks and perform buy/sell transactions. This controller relies on both the `TransactionService` and `UsersService` to handle stock data and user-specific information.

#### Endpoints

- **GET /**: Displays the home page with the user's current stocks, total investment value, and available cash balance.

  - **Parameters**:
    - **Principal principal**: Provides the current logged-in user's details.
    - **Model model**: Used to add attributes like stocks, total value, and cash to the view.
    - **RedirectAttributes redirectAttributes**: Used to pass feedback messages to the view.
    - **HttpSession session**: Used to store the user's stock data temporarily for validation in subsequent operations.

  - **Response**: Renders the `index.html` page with the populated model attributes.

- **POST /homeBuyConfirm**: Processes a buy transaction request initiated from the home page.

  - **Request Parameters**:
    - **TransactionRequest request**: A data transfer object containing the details of the transaction.
      ```json
      {
          "symbol": "AAPL",
          "shares": 10,
          "price": 150.00
      }
      ```
    - **Principal principal**: Provides the current logged-in user's details.
    - **Errors errors**: Captures validation errors during the transaction process.
    - **HttpSession session**: Retrieves stock information to verify the buy request against the user's session data.
    - **RedirectAttributes redirectAttributes**: Used to pass success or error messages back to the view.

  - **Response**: Redirects to the home page (`/`) with a message indicating success or failure.

  - **Validation and Logging**: 
    - Validates the request data and ensures it matches the user's session data.
    - Logs warnings for invalid requests and info messages for successful transactions.

- **POST /homeSellConfirm**: Processes a sell transaction request from the home page.

  - **Request Parameters**:
    - **TransactionRequest request**: A data transfer object containing the details of the transaction.
      ```json
      {
          "symbol": "AAPL",
          "shares": 5,
          "price": 150.00
      }
      ```
    - **Principal principal**: Provides the current logged-in user's details.
    - **Errors errors**: Captures validation errors during the transaction process.
    - **HttpSession session**: Retrieves stock information to verify the sell request against the user's session data.
    - **RedirectAttributes redirectAttributes**: Used to pass success or error messages back to the view.

  - **Response**: Redirects to the home page (`/`) with a message indicating success or failure.

  - **Validation and Logging**: 
    - Validates the request data and ensures it matches the user's session data.
    - Logs warnings for invalid requests and info messages for successful transactions.

#### Services

The `HomeController` uses the following services:

- **TransactionService**: Provides methods for handling buy and sell transactions and retrieving stock data.
- **UsersService**: Retrieves user-specific data, such as cash balance.

#### DTO: TransactionRequest

The `TransactionRequest` DTO encapsulates the details of a stock transaction.

- **Attributes**:
  - **String symbol**: The stock symbol (must not be blank).
  - **BigDecimal shares**: The number of shares to buy or sell (must not be null and must be a valid number).
  - **BigDecimal price**: The price per share (must not be null and must be a valid number).

- **Validation**:
  - Uses annotations like `@NotBlank`, `@NotNull`, and `@Digits` to ensure data integrity.

- **Equality**:
  - Overrides the `equals` method to compare `TransactionRequest` objects based on `symbol`, `shares`, and `price`.

### ProfileController

The `ProfileController` handles user profile management, including updating the username, email, password, and adding cash to the user's account. This controller ensures secure handling of user data and relies on `UsersService` for business logic operations related to user profile updates.

#### Endpoints

- **GET /profile**: Displays the user's profile page.

  - **Parameters**:
    - **Principal principal**: Provides the current logged-in user's details.
    - **Model model**: Used to add the username attribute to the view.

  - **Response**: Renders the `profile.html` page with the current username displayed.

- **POST /profileUsername**: Allows the user to change their username.

  - **Request Parameters**:
    - **String newUsername**: The new username to be set.
    - **String password**: The user's current password for authentication.
    - **Principal principal**: Provides the current logged-in user's details.
    - **RedirectAttributes redirectAttributes**: Used to pass feedback messages to the view.
    - **HttpServletRequest request**: The HTTP request object.
    - **HttpServletResponse response**: The HTTP response object.

  - **Response**: 
    - Redirects to `/profile` with a message if the username change fails.
    - Redirects to `/login` if the username change is successful and logs the user out.

  - **Logging**: 
    - Logs warnings if the username change process fails.

- **POST /profileEmail**: Allows the user to change their email address.

  - **Request Parameters**:
    - **ChangeEmailRequest changeEmailRequest**: A data transfer object containing the new email and current password.
      ```json
      {
          "email": "newemail@example.com",
          "password": "currentPassword"
      }
      ```
    - **Errors errors**: Captures validation errors during the email change process.
    - **Principal principal**: Provides the current logged-in user's details.
    - **RedirectAttributes redirectAttributes**: Used to pass feedback messages to the view.

  - **Response**: 
    - Redirects to `/profile` with a message indicating success or failure.

  - **Validation and Logging**: 
    - Validates the email format and non-blank fields.
    - Logs warnings for invalid email attempts and unsuccessful email change processes.

- **POST /profilePassword**: Allows the user to change their password.

  - **Request Parameters**:
    - **String oldPassword**: The user's current password.
    - **String newPassword**: The new password to be set.
    - **String confirmNewPassword**: The confirmation of the new password.
    - **Principal principal**: Provides the current logged-in user's details.
    - **RedirectAttributes redirectAttributes**: Used to pass feedback messages to the view.
    - **HttpServletRequest request**: The HTTP request object.
    - **HttpServletResponse response**: The HTTP response object.

  - **Response**: 
    - Redirects to `/profile` with a message if the password change fails.
    - Redirects to `/login` if the password change is successful and logs the user out.

  - **Validation and Logging**: 
    - Ensures the new password and confirmation match.
    - Logs warnings for mismatched passwords and unsuccessful password change attempts.

- **POST /profileCash**: Allows the user to add cash to their account.

  - **Request Parameters**:
    - **BigDecimal cashToAdd**: The amount of cash to add.
    - **String password**: The user's current password for authentication.
    - **RedirectAttributes redirectAttributes**: Used to pass feedback messages to the view.
    - **Principal principal**: Provides the current logged-in user's details.

  - **Response**: 
    - Redirects to `/profile` with a message indicating the result of the operation.

  - **Logging**: 
    - Logs information messages with the result of the cash addition operation.

#### DTO: ChangeEmailRequest

The `ChangeEmailRequest` DTO encapsulates the details required to change a user's email address.

- **Attributes**:
  - **String email**: The new email address (must be a valid email format and not blank).
  - **String password**: The user's current password (must not be blank).

- **Validation**:
  - Uses annotations like `@Email` and `@NotBlank` to ensure data integrity.

### QuoteController

The `QuoteController` is responsible for handling stock quote requests, allowing users to get the current price and total value of a specified number of shares for a given stock symbol. This controller leverages the `TransactionService` to interact with stock data.

#### Endpoints

- **GET /quote**: Displays the stock quote page.

  - **Response**: Renders the `quote.html` page, where users can input the stock symbol and number of shares they wish to quote.

- **POST /quote**: Processes the stock quote request and returns the current stock price and total value.

  - **Request Parameters**:
    - **QuoteRequest request**: A data transfer object containing the stock symbol and number of shares.
      ```json
      {
          "symbol": "AAPL",
          "shares": 10.0
      }
      ```
    - **Errors errors**: Captures validation errors during the quote request process.
    - **Model model**: Used to pass data to the view.
    - **RedirectAttributes redirectAttributes**: Used to pass feedback messages to the view.

  - **Response**:
    - Redirects to `/quote` with a message if the quote request fails (e.g., due to validation errors or an invalid stock symbol).
    - Updates the `quote.html` page with the stock's symbol, price, the number of shares, and the total value if the quote is successful.

  - **Validation and Logging**:
    - Validates that the stock symbol is not blank and that the number of shares is a valid decimal number.
    - Logs warnings for invalid quote attempts and invalid stock symbols.

#### DTO: QuoteRequest

The `QuoteRequest` DTO encapsulates the details required to request a stock quote.

- **Attributes**:
  - **String symbol**: The stock symbol (must not be blank).
  - **BigDecimal shares**: The number of shares to quote (must not be null and must be a valid decimal).

- **Validation**:
  - Uses annotations like `@NotBlank` and `@Digits` to ensure data integrity.

### BuyController

The `BuyController` handles the process of buying stocks. It facilitates stock lookup, displays the stock's price and total value for the requested shares, and manages the purchase confirmation.

#### Endpoints

- **GET /buy**: Displays the stock purchase page.

  - **Response**: Renders the `buy.html` page, where users can input the stock symbol and number of shares they wish to purchase.

- **POST /buy**: Processes the stock purchase request, retrieves the stock price, and displays the total cost for the specified number of shares.

  - **Request Parameters**:
    - **QuoteRequest request**: A data transfer object containing the stock symbol and number of shares.
      ```json
      {
          "symbol": "AAPL",
          "shares": 10.0
      }
      ```
    - **Errors errors**: Captures validation errors during the purchase request process.
    - **HttpSession session**: Used to store the transaction request for purchase confirmation.
    - **Model model**: Used to pass data to the view.
    - **RedirectAttributes redirectAttributes**: Used to pass feedback messages to the view.

  - **Response**:
    - Redirects to `/buy` with a message if the request fails (e.g., due to validation errors or an invalid stock symbol).
    - Updates the `buy.html` page with the stock's symbol, price, the number of shares, and the total value if the request is successful.
    - Stores the `TransactionRequest` object in the session for purchase confirmation.

  - **Validation and Logging**:
    - Validates that the stock symbol is not blank and that the number of shares is a valid decimal number.
    - Logs warnings for invalid purchase requests and invalid stock symbols.

- **POST /buyConfirm**: Confirms the stock purchase using the stored transaction request from the session.

  - **Request Parameters**:
    - **TransactionRequest request**: A data transfer object containing the stock symbol, number of shares, and stock price.
      ```json
      {
          "symbol": "AAPL",
          "shares": 10.0,
          "price": 150.00
      }
      ```
    - **Principal principal**: Provides the current logged-in user's information.
    - **Errors errors**: Captures validation errors during the purchase confirmation process.
    - **HttpSession session**: Used to retrieve the stored transaction request for purchase confirmation.
    - **RedirectAttributes redirectAttributes**: Used to pass feedback messages to the view.

  - **Response**:
    - Redirects to `/buy` with a message if the purchase confirmation fails (e.g., due to validation errors or a mismatch with the stored transaction request).
    - Redirects to `/` with a message if the purchase is successful.

  - **Validation and Logging**:
    - Validates that the purchase confirmation request matches the stored transaction request.
    - Logs warnings for invalid purchase confirmation attempts and mismatches with the stored transaction request.

### SellController

The `SellController` manages the process of selling stocks. It retrieves the user's stocks, facilitates the sale of a stock, and confirms the sale transaction.

#### Endpoints

- **GET /sell**: Displays the stock sell page with the user's available stocks.

  - **Request Parameters**:
    - **Principal principal**: Provides the current logged-in user's information.

  - **Response**: Renders the `sell.html` page, which includes a list of the user's stocks available for sale.

  - **Functionality**:
    - Retrieves the list of stocks owned by the user from the `TransactionService`.
    - Passes this list to the `sell.html` view for display.

- **POST /sell**: Processes the request to sell a stock, retrieves the stock's price, and prepares for sale confirmation.

  - **Request Parameters**:
    - **QuoteRequest request**: A data transfer object containing the stock symbol and number of shares to sell.
      ```json
      {
          "symbol": "AAPL",
          "shares": 10.0
      }
      ```
    - **Errors errors**: Captures validation errors during the sell request process.
    - **HttpSession session**: Used to store the transaction request for sale confirmation.
    - **Model model**: Used to pass data to the view.
    - **RedirectAttributes redirectAttributes**: Used to pass feedback messages to the view.

  - **Response**:
    - Redirects to `/sell` with a message if the request fails (e.g., due to validation errors or an invalid stock symbol).
    - Updates the `sell.html` page with the stock's symbol, price, the number of shares, and the total value if the request is successful.
    - Stores the `TransactionRequest` object in the session for sale confirmation.

  - **Validation and Logging**:
    - Validates that the stock symbol is not blank and that the number of shares is a valid decimal number.
    - Logs warnings for invalid sell requests and invalid stock symbols.

- **POST /sellConfirm**: Confirms the stock sale using the stored transaction request from the session.

  - **Request Parameters**:
    - **TransactionRequest request**: A data transfer object containing the stock symbol, number of shares, and stock price.
      ```json
      {
          "symbol": "AAPL",
          "shares": 10.0,
          "price": 150.00
      }
      ```
    - **Principal principal**: Provides the current logged-in user's information.
    - **Errors errors**: Captures validation errors during the sale confirmation process.
    - **HttpSession session**: Used to retrieve the stored transaction request for sale confirmation.
    - **RedirectAttributes redirectAttributes**: Used to pass feedback messages to the view.

  - **Response**:
    - Redirects to `/sell` with a message if the sale confirmation fails (e.g., due to validation errors or a mismatch with the stored transaction request).
    - Redirects to `/` with a message if the sale is successful.

  - **Validation and Logging**:
    - Validates that the sale confirmation request matches the stored transaction request.
    - Logs warnings for invalid sale confirmation attempts and mismatches with the stored transaction request.

## Kafka

The application uses Apache Kafka to handle messaging for transaction notifications. This section explains the Kafka configuration, producer, and consumer services.

### Kafka Configuration

- **KafkaConfig**:
  - Configures Kafka components for both producers and consumers.
  - **Bootstrap Servers**: Set to `broker:9090` for connecting to the Kafka cluster.
  - **KafkaAdmin**: Manages Kafka topics and configurations.
  - **ProducerFactory**: Configures the Kafka producer with idempotence and JSON serialization.
  - **ConsumerFactory**: Configures the Kafka consumer with JSON deserialization.
  - **KafkaTemplate**: Provides a high-level API for sending messages to Kafka topics.
  - **ConcurrentKafkaListenerContainerFactory**: Sets up the Kafka listener container factory for processing messages.

### Kafka Producer

- **KafkaProducerService**:
  - Sends `History` objects to the Kafka topic `transaction-notification`.
  - Uses `KafkaTemplate` to send messages.
  - Logs the status of messages sent to Kafka and handles exceptions.

### Kafka Consumer

- **KafKaConsumerService**:
  - Consumes messages from the Kafka topic `transaction-notification`.
  - Listens to the topic using `@KafkaListener`.
  - Upon receiving a `History` object, it logs the message and triggers an email notification via `NotificationService`.

### Kafka Flow

1. **Producer Configuration**:
   - Configures a Kafka producer with JSON serialization.
   - Creates a topic named `transaction-notification` with one partition and one replica.

2. **Sending Messages**:
   - The `KafkaProducerService` sends `History` objects to the `transaction-notification` topic.
   - Messages are sent asynchronously, and status is logged.

3. **Consuming Messages**:
   - The `KafKaConsumerService` listens to the `transaction-notification` topic.
   - Upon consuming a message, it processes the `History` object and invokes the `NotificationService` to send an email.
