# assessment


This repository contains the source code for the **Assessment Project**, a Spring Boot application built to manage assessments and their associated operations. The application leverages PostgreSQL as its database and is configured for REST API functionality.

## Features

- REST API for Content provider service data process operations.
- PostgreSQL database integration.
- Hibernate for ORM.
- Virtual threads support for improved concurrency.
- Centralized configuration management using `application.properties`.

---

## Prerequisites

Ensure the following are installed on your system before proceeding:

1. **Java Development Kit (JDK) 21**
   [Download JDK](https://www.oracle.com/java/technologies/javase-downloads.html)

2. **Maven 3.6+**  
   [Download Maven](https://maven.apache.org/download.cgi)

3. **PostgreSQL Database**  
   [Download PostgreSQL](https://www.postgresql.org/download/)

4. **Git**  
   [Download Git](https://git-scm.com/)

---

## Project Configuration

### Database Setup

1. Install and configure PostgreSQL.
2. Create a database named `assessment_db`.
3. Update the `application.properties` file with your PostgreSQL credentials if they differ from the default:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/assessment_db
   spring.datasource.username=postgres
   spring.datasource.password=786
   server.port=9090 


## Running the Application

Follow these steps to run the application locally:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/shohag-pk/assessment
   
2. **Build the project using Maven**:
   ```bash
   mvn clean install
3. **Run the application**:
   ```bash
   mvn spring-boot:run

4. **Verify the application**:
   http://localhost:9090 // if you run it on your localhost


