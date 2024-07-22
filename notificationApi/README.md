# NotificationApi

## Endpoints

### 1. Send Email Notification

- **URL**: `/sendEmail`
- **Method**: `POST`
- **Description**: Sends an email notification with details about a stock transaction.
- **Request Body**: Expects a JSON payload with the transaction details.

#### Request Body Example

```json
{
  "email": "user@example.com",
  "username": "john_doe",
  "action": "BUY",
  "quant": 50.00,
  "name": "AAPL",
  "date": "2024-07-23",
  "price": 145.30
}
```

#### Request Body Validation

- **email**: Must be a valid email format and not blank.
- **username**: Must not be blank.
- **action**: Must not be blank.
- **quant**: Must not be null and should be a valid number with up to 10 integer digits and 2 fractional digits.
- **name**: Must not be blank.
- **price**: Must not be null and should be a valid number with up to `Integer.MAX_VALUE` integer digits and 2 fractional digits.

#### Response

- **Success**: Returns `true` with HTTP Status `200 OK`.
- **Failure**: Returns `false` with HTTP Status `400 Bad Request` if there are validation errors.

### 2. Get Result

- **URL**: `/get/result`
- **Method**: `GET`
- **Description**: Returns a random boolean value for testing purposes.
- **Response**: A boolean value (`true` or `false`) with HTTP Status `200 OK`.

## Environment Configuration

To facilitate cloning and running this project, the email server settings in the application.properties file are pre-configured with the stockwalletfinance burner email. This enables you to use the email sending feature out of the box.
