# MockStockApi

## Table of Contents

- [DataBase](#database)
- [Controllers and Endpoints](#controllers-and-endpoints)

## Database

### MongoDB Document Structure

The application uses MongoDB to store stock data, modeled by the `StockQuote` class. This mock stock document structure aligns with the stock data that is provided by the external stock API, ensuring seamless integration and data manipulation.

#### StockQuote Document

#### Fields

The class contains the following fields, annotated with `@BsonProperty` to match the naming conventions used by the external API:

- **`id`**: Unique identifier for the stock quote document.
- **`symbol`** (`01. symbol`): The stock's ticker symbol.
- **`open`** (`02. open`): The opening price of the stock.
- **`high`** (`03. high`): The highest price of the stock during the trading day.
- **`low`** (`04. low`): The lowest price of the stock during the trading day.
- **`price`** (`05. price`): The current price of the stock.
- **`volume`** (`06. volume`): The number of shares traded during the trading day.
- **`latestTradingDay`** (`07. latest trading day`): The date of the latest trading session.
- **`previousClose`** (`08. previous close`): The closing price of the stock on the previous trading day.
- **`change`** (`09. change`): The price change since the previous close.
- **`changePercent`** (`10. change percent`): The percentage change in price since the previous close.

#### Constructors

The `StockQuote` class provides several constructors to facilitate the creation of mock stock quote objects:

- **Mock Data Constructor (Symbol Specified)**: Generates a `StockQuote` object with random mock data, using the specified symbol.

  ```java
  public StockQuote(String symbol) {
      Random random = new Random();
      this.symbol = symbol;
      this.open = String.valueOf(random.nextInt(1000 - 500 + 1) + 500);
      this.high = String.valueOf(random.nextInt(100000 - 50000 + 1) + 50000);
      this.low = String.valueOf(random.nextInt(100) + 1);
      this.price = String.valueOf(random.nextInt(2000) + 1);
      this.volume = String.valueOf(random.nextInt(100000) + 1);
      this.latestTradingDay = "2024-" + String.valueOf(random.nextInt(11) + 1) + "-" + String.valueOf(random.nextInt(11) + 1);
      this.previousClose = String.valueOf(random.nextInt(100000) + 1);
      this.change = String.valueOf(random.nextFloat());
      this.changePercent = String.valueOf(random.nextFloat()) + "%";
  }
  ```

- **Mock Data Constructor (Default)**: Generates a `StockQuote` object with random mock data for testing purposes, assigning a random symbol starting with "TEST".

  ```java
  public StockQuote() {
      Random random = new Random();
      this.symbol = "TEST" + String.valueOf(random.nextInt());
      this.open = String.valueOf(random.nextInt(1000 - 500 + 1) + 500);
      this.high = String.valueOf(random.nextInt(100000 - 50000 + 1) + 50000);
      this.low = String.valueOf(random.nextInt(100) + 1);
      this.price = String.valueOf(random.nextInt(100000) + 1);
      this.volume = String.valueOf(random.nextInt(100000) + 1);
      this.latestTradingDay = "2024-" + String.valueOf(random.nextInt(11) + 1) + "-" + String.valueOf(random.nextInt(11) + 1);
      this.previousClose = String.valueOf(random.nextInt(100000) + 1);
      this.change = String.valueOf(random.nextFloat());
      this.changePercent = String.valueOf(random.nextFloat()) + "%";
  }
  ```

## Controllers and Endpoints

### StockController

The `StockController` is a RESTful controller responsible for handling HTTP requests related to stock data. It provides endpoints to save and retrieve stock information based on a stock's symbol.

#### Endpoints

The `StockController` exposes the following endpoints:

- **`POST /save/{symbol}`**: Saves the stock data for the specified symbol to the database.

  - **Path Variable**: `symbol` - The stock symbol to be saved.
  - **Response**: Returns a JSON representation of the saved stock data with an HTTP status of 200 (OK).
  - **Security**: The `POST` endpoint is secured by OAuth2 Authorization Code Flow, ensuring that only authorized users can perform this operation.

- **`GET /get/{symbol}`**: Retrieves the stock data for the specified symbol from the database.

  - **Path Variable**: `symbol` - The stock symbol to be retrieved.
  - **Response**: Returns a JSON representation of the retrieved stock data with an HTTP status of 200 (OK).

#### OAuth2 Security

The `POST /save/{symbol}` endpoint is secured using OAuth2 Authorization Code Flow. This ensures that only authenticated and authorized users can save stock data. To access this endpoint, you must obtain an access token through the OAuth2 authorization process.

##### Example: Using Postman to Save Stock Data

1. **Open Postman** and create a new `POST` request.

2. **Set the URL** to your application's endpoint:

   ```
   http://localhost:8090/save/nflx
   ```

3. **Add Authorization**:

  - Go to the **Authorization** tab in Postman.
  - Select **OAuth 2.0** as the type.
  - Go to **Configure New Token**.
  - Fill in the necessary details:
    - **Grant Type**: Authorization Code
    - **Callback URL**: http://127.0.0.1:8080/authorized
    - **Auth URL**: http://localhost:9000/oauth2/authorize
    - **Access Token URL**: http://localhost:9000/oauth2/token
    - **Client Id**: stock-client
    - **Client Secret**: secret
    - **Scope**: stock.write
    
4. **Request the Access Token**: Click on **Get new Access Token**, and Postman will open a login form.
- Fill in the necessary details:
    - **Username**: username
    - **Password**: password

5. **Consent required**: Click on **stock.write** checkbox and then **submit consent**.

6. **Token details**: Wait some seconds and then click on **Use Token**

7. Now you can send the request
#### Logging

- Logs an informational message when a user attempts to save or retrieve stock data.


