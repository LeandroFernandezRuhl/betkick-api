# BetKick API

BetKick is a web application that provides a risk-free simulation environment for football betting. Users can explore real football matches and competitions, place virtual bets on match outcomes,
and compete with others to see who makes the best predictions. This repository contains the backend codebase built with Spring Boot. For the frontend repository see [BetKick client](https://github.com/LeandroFernandezRuhl/betkick-client).

## Features

The BetKick backend is responsible for the following key features:

- **Integration with the football-data.org API**: Retrieves and stores football data from the [football-data.org API](https://www.football-data.org/).
- **Data Management**: Stores and retrieves user data, bet information, and football data (matches, standings, competitions) in a database.
- **Scheduled Updates**: Periodically polls the football-data.org API to maintain match data (score, status, etc) and league standings up to date in the backend's database.
- **Odds Calculation**: Calculates odds for football matches based on team and league statistics like recent and historical performance of a team, home advantage, etc.
- **API Endpoints**: Provides RESTful APIs for user authentication, bet management, data retrieval, and other functionalities.
- **Caching**: Implements caching with [Caffeine Cache](https://github.com/ben-manes/caffeine) to improve performance and reduce data retrieval times.
- **Security**: Includes security measures such as input validation, authentication, and authorization to protect user data and prevent unauthorized access.
- **Database Integration**: Integrates with a MariaDB database (or any other compatible database) for data persistence.

## Technologies

The BetKick API is built using the following technologies:

- Java 17
- Spring Boot 3.1.x
- Spring Data JPA
- Spring Security
- Spring Cache
- MariaDB
- Hibernate
- Gradle

## Getting Started

### Prerequisites

- Java 17 JDK
- Gradle
- MariaDB (or any other compatible database)

### Installation

1. Clone the repository: `git clone https://github.com/LeandroFernandezRuhl/betkick-api.git)`
2. Navigate to the project directory: `cd betkick-api`
3. Configure the database connection in `src/main/resources/application.properties`
4. Add an environment variable with a [football-data.org](https://www.football-data.org/) API key
5. Build the project: `./gradlew build`

### Running the Application

1. Start the backend server: `./gradlew bootRun`
2. The server will start on `http://localhost:8080` on `http://localhost:8080`