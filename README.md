# AWS Serverless CRUD API (Java 21 + Lambda + API Gateway + DynamoDB)

This repository contains a minimal, fully serverless CRUD API built using:

- AWS Lambda
- Amazon API Gateway (HTTP API v2)
- Amazon DynamoDB
- Java 21 + Maven + AWS SDK v2

The goal of this project is to demonstrate how to build a clean, lightweight Lambda function in Java, without Spring Boot or heavy frameworks, while following AWS best practices for serverless development.

A full blog article explaining this project (architecture, code, deployment steps, lessons learned) is published here:
👉 https://mindhub42.com/en/aws/build-a-serverless-aws-lambda-dynamodb-crud-api-with-java-21-step-by-step-guide/



🛠️ Build & Package

This project uses Maven and the Shade plugin to produce a fat JAR.

Build it using:

>> mvn clean package

The deployable artifact will be located at:

>> target/function.jar

Upload function.jar to AWS Lambda.

⚙️ AWS Setup (Minimal Steps)
- Create DynamoDB Table
  - Table name	ItemsTable
  - Partition key	id (String)
  - Leave all other settings default.
- Create Lambda Function
  - Runtime: Java 21
  - Execution role: Simple microservice permissions (later restrict to your table)
  - Environment variables:
    - TABLE_NAME=ItemsTable
  - Upload function.jar.
  - Update handler:
    - com.example.lambda.controller.ApiHandler::handleRequest 
- Create API Gateway HTTP API (v2)
- Define routes:
  - GET    /items
  - GET    /items/{id}
  - POST   /items
  - PUT    /items/{id}
  - DELETE /items/{id}
- Attach Lambda integration to each route.
- Deploy your API and copy the invoke URL.

📄 License

MIT — feel free to use this code as a template for your own serverless backends.

📣 Article

A detailed write-up explaining the full process is here:
https://mindhub42.com/en/aws/build-a-serverless-aws-lambda-dynamodb-crud-api-with-java-21-step-by-step-guide/