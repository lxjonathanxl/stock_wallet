[JAVASCRIPT__BADGE]: https://img.shields.io/badge/Javascript-000?style=for-the-badge&logo=javascript
[THYMELEAF__BADGE]: https://img.shields.io/badge/Thymeleaf-%23005C0F.svg?style=for-the-badge&logo=Thymeleaf&logoColor=white
[MYSQL__BADGE]: https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white
[KAFKA__BADGE]: https://img.shields.io/badge/Apache%20Kafka-000?style=for-the-badge&logo=apachekafka
[TESTCONTAINERS__BADGE]: https://img.shields.io/badge/Testcontainers-%23316192?style=for-the-badge&logo=box&logoColor=white
[JUNIT__BADGE]: https://img.shields.io/badge/JUnit-C71A36?style=for-the-badge&logo=openjdk&logoColor=white
[JAVA_BADGE]:https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white
[SPRING_BADGE]: https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white
[MONGO_BADGE]:https://img.shields.io/badge/MongoDB-%234ea94b.svg?style=for-the-badge&logo=mongodb&logoColor=white
[AZURE_BADGE]:https://img.shields.io/badge/azure-%230072C6.svg?style=for-the-badge&logo=microsoftazure&logoColor=white
[OAUTH2_BADGE]:https://img.shields.io/badge/Auth0-EB5424?logo=auth0&logoColor=fff&style=for-the-badge
[ACTIONS_BADGE]:https://img.shields.io/badge/GitHub%20Actions-2088FF?logo=githubactions&logoColor=fff&style=for-the-badge
[DOCKER_BADGE]:https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=fff&style=for-the-badge

<h1 align="center" style="font-weight: bold;">Finance stock wallet 💻</h1>

![java][JAVA_BADGE]
![spring][SPRING_BADGE]
![mysql][MYSQL__BADGE]
![mongo][MONGO_BADGE]
![kafka][KAFKA__BADGE]
![thymeleaf][THYMELEAF__BADGE]
![javascript][JAVASCRIPT__BADGE]
![testcontainers][TESTCONTAINERS__BADGE]
![junit][JUNIT__BADGE]
![oauth2][OAUTH2_BADGE]
![docker][DOCKER_BADGE]
![actions][ACTIONS_BADGE]
![azure][AZURE_BADGE]

<p align="center">
  This Stock Wallet application allows users to buy, sell, and quote stock shares. Each transaction generates a detailed history, which is saved and sent directly to the user's email for easy tracking and security.
</p>


## Table of Contents

- [Introduction](#introduction)
- [Components](#components)
- [Requirements](#requirements)
- [Quick Start](#quick-start)
- [Testing](#testing)
- [API](#requirements)
- [Acknowledgements](#acknowledgements)




## Introduction

This project is divided into five main parts:

1. **Wallet**:
   - Manages the Model-View-Controller (MVC) architecture.
   - Perform user registration, authorization, and updates.
   - Stores user data, including owned stocks and transaction history, in a MySQL database.
   - Handles stock transactions by connecting to either an outsourced API or the local MockStockApi to gather stock quotes.
   - Generates a transaction history during each transaction, saving it to the database and sending it to a Kafka topic.
   - On kafka message consumption it connects to the NotificationApi to asynchronously send transaction information to the user's email, ensuring the notification process does not interfere with the transaction flow.

2. **MockStockApi**:
   - Generates mock stock data with random information given a symbol.
   - Stores the generated stock data in a MongoDB database.
   - Provides stock information upon request.
   - Secures the stock creation port by connecting to AuthorizationServerMockStock, which uses OAuth 2 with an authorization code flow.

3. **NotificationApi**:
   - Sends an email to the user with transaction details based on the provided transaction information.

4. **Docker**:
   - Each module has a Dockerfile, which is used by `compose.yaml` to create a network of interdependent services.

5. **CI/CD Workflow**:
   - Utilizes GitHub Actions to create a workflow triggered on every push to the main branch.
   - Builds the container, runs unit and integration tests, and, upon passing tests, deploys to an Azure virtual machine.

## Components

1. [wallet](/wallet) Spring MVC application, Created with Spring security, Kafka for messaging, Testcontainers for testing environments, Thymeleaf for server-side rendering, and JPA for database interactions.
2. [apiMockStocks](/apiMockStocks) Spring RestApi, created with Spring security, includes dependencies for MongoDB integration, security with OAuth2, and comprehensive testing support using Testcontainers and JUnit.
3. [notificationApi](/notificationApi) Spring RestApi, created with email support with Spring Mail, validation, and testing using Spring Boot Starter Test.

Take a look at the components diagram that describes their interactions.
![microservice-app-example](https://github.com/user-attachments/assets/2d3bafd8-3bd8-4b60-9fa1-3ac2295d919c)


## Requirements
The application can be run in a docker container, the requirements for setup are listed below.

### Local
* [Java 17 SDK](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
* [Maven](https://maven.apache.org/download.cgi)


### Docker
* [Docker](https://www.docker.com/get-docker)


## Quick Start

### Run Docker

First build the image:
```bash
$ docker-compose build
```

When ready, run it:
```bash
$ docker-compose up
```

Application will run by default on port `8080`

Configure the port by changing its value in __.env__ file.


## Testing
After the compose container is up, you can run the tests with the fallowing commands.
the application has in total 137 different tests.

**Disclaimer**

Env variables must be added to test environment

**NotificationApi**
```bash
  cd notificationApi
```

```bash
  mvn -B test --file pom.xml
```

```bash
  cd ..
```

**MockStockApi**
```bash
  cd apiMockStocks
```

```bash
  mvn -B test --file pom.xml
```

```bash
  cd ..
```

**Wallet**
```bash
  cd wallet
```

```bash
  mvn -B test --file pom.xml
```

```bash
  cd ..
```

## API

**Disclaimer**

This application was built solely for the purpose of studying and practicing different technologies. As such, all information in the `.env` file has been made fully available to facilitate easy cloning of the project. Please note that the stock wallet email is a burner account.

**Demo**

youtube video of application running: [![Watch the video](https://img.youtube.com/vi/1gbRvNPtIts/maxresdefault.jpg)](https://www.youtube.com/watch?v=1gbRvNPtIts)
