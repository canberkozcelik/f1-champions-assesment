# F1 Champions Assessment

A Spring Boot application that fetches and displays F1 World Champions data using the Ergast API.

## Project Structure

```
f1-champions-assesment/
├── .github/                    # GitHub Actions workflows
│   └── workflows/
│       └── pr-validation.yml           # CI/CD pipeline PR validation
│       └── deploy.yml           # CI/CD pipeline deployment
├── backend/                    # Spring Boot application
│   ├── src/                   # Source code
│   ├── build.gradle.kts       # Gradle build configuration
│   ├── gradlew               # Gradle wrapper script (Unix)
│   ├── gradlew.bat           # Gradle wrapper script (Windows)
│   └── .editorconfig         # Code style configuration
├── frontend/                  # Android application
│   ├── app/                  # Main application module
│   ├── core/                 # Core utilities and common components
│   ├── data/                 # Data layer (repositories, API clients)
│   ├── domain/              # Domain layer (use cases, models)
│   ├── feature/             # Feature modules
│   │   └── seasonslist/     # Seasons list feature module
│   │   └── racewinners/     # Race winners feature module
│   ├── build.gradle.kts     # Root Gradle build configuration
│   ├── gradle/              # Gradle wrapper files
│   ├── gradlew             # Gradle wrapper script (Unix)
│   ├── gradlew.bat         # Gradle wrapper script (Windows)
│   └── settings.gradle.kts  # Gradle settings
├── infrastructure/           # Infrastructure and deployment
│   ├── Dockerfile           # Backend service Dockerfile
│   ├── docker-compose.yml   # Docker Compose configuration
│   ├── .dockerignore       # Docker ignore rules
│   ├── scripts/            # Utility scripts
│   │   └── setup-local-env.sh  # Local environment setup
│   └── secrets/            # Secrets directory (git-ignored)
├── .gitignore              # Git ignore rules
└── README.md              # This file
```

# F1 Champions

A mobile application that displays F1 champions and race winners, with a backend API serving the data.

## Architecture

### High-Level Overview
- **Frontend**: Android app (Kotlin)
  - Multi-module architecture (app, core, data, domain, feature)
  - Clean Architecture principles
  - MVVM pattern
  - Uses Jetpack Compose for UI

- **Backend**: Spring Boot API (Kotlin)
  - RESTful API design
  - Integrates with Ergast API for F1 data
  - PostgreSQL database for caching
  - Docker containerization

### Trade-offs
1. **Architecture Decisions**
   - Multi-module Android app
     - Pros: Better separation of concerns, reusable components, easier testing
     - Cons: Increased build time, more complex project structure
   
   - Clean Architecture
     - Pros: Maintainable, testable, independent of frameworks
     - Cons: More boilerplate code, steeper learning curve

2. **Data Management**
   - Direct Ergast API integration
     - Pros: Real-time data, no data maintenance
     - Cons: Dependent on external service, potential rate limits
   
   - Local caching with PostgreSQL
     - Pros: Faster response times, reduced API calls
     - Cons: Additional infrastructure, data synchronization

3. **Deployment Strategy**
   - GitHub Container Registry (GHCR)
     - Pros: Integrated with GitHub, easy versioning
     - Cons: Limited to GitHub ecosystem
   
   - GitHub Releases for APK
     - Pros: Simple distribution, version tracking
     - Cons: Manual installation required

## Developer Notes  
### Backend  

Hybrid Approach for the Backend:  
- Seasons/Champions Data: You opted to pre-load this data when the backend application starts (using the DataInitializer and its @PostConstruct method calling f1DataService.ensureSeasonsDataPopulated()). This was a trade-off to ensure the frontend's initial screen (displaying the list of seasons and champions via GET /api/seasons) loads quickly for a better user experience, at the cost of a slightly longer backend startup time.  
- Races/Winners Data (for a specific season): This data would be fetched from the Ergast API "on the first request" for that specific season's races (via GET /api/seasons/{year}/races), if not already present in your database. For race data, fetching on demand was deemed simpler to implement initially and avoided an even longer backend startup if all races for all seasons were pre-fetched.  

Retry utility written in pure Kotlin for coroutines:  
Implemented retry and backoff mechanism because of the looped Ergast API requests. Why can’t use Spring Boot’s @Retryable annotation directly? Using Spring Retry's @Retryable annotation directly on a suspend function or a function that internally uses coroutines in a non-blocking way can be problematic and might not behave as expected without careful consideration or specific integration.  

Why does linter have different configuration for test files?
1. Test files often have long setup code that's more readable in a single line
2. Test data structures are often more readable when kept together
3. The focus in test files is on readability and maintainability, not strict formatting.  


## Local Development

### Configuration Setup

1. **Backend Configuration**
   ```bash
   # Copy the template configuration
   cp backend/src/main/resources/application.yml.template backend/src/main/resources/application.yml
   
   # Edit the configuration as needed
   # The template includes default values that work for local development
   ```

2. **Database Setup**
   ```bash
   # Run the setup script to configure the database
   cd infrastructure/scripts
   ./setup-local-env.sh
   ```
   This script will:
   - Create a `.env` file with database settings
   - Generate a secure database password
   - Set up proper file permissions

   The script creates these default environment variables:
   - `DB_NAME`: Database name (default: env_f1db)
   - `DB_USERNAME`: Database username (default: env_f1user)
   - `POSTGRES_DATA_DIR`: PostgreSQL data directory

   You can override these by editing the `.env` file after setup.

3. **Environment Variables**
   The application can be configured using environment variables. Key variables include:
   - `SPRING_PROFILES_ACTIVE`: Set to 'dev' for development (default: 'prod')
   - `SPRING_JPA_SHOW_SQL`: Enable/disable SQL logging (default: false)
   - `SPRING_JPA_HIBERNATE_DDL_AUTO`: Database schema strategy (default: create)
   - `SERVER_PORT`: Application port (default: 8080)
   - `ERGAST_API_BASE_URL`: Ergast API endpoint (default: https://api.jolpi.ca/ergast/f1)

   These can be set in your environment or in the `.env` file.

4. **Security Notes**
   - The generated database password is stored in `infrastructure/secrets/db_password.txt`
   - This file is git-ignored and has restricted permissions (600)
   - Never commit sensitive files to version control
   - The template configuration uses default values suitable for local development  

### Prerequisites
- Docker and Docker Compose
- JDK 17
- Android Studio
- Gradle

### Running the Application

1. **Start the Backend**
   ```bash
   # Start the database and backend
   docker compose up -d
   
   # Check logs
   docker compose logs -f
   ```

2. **Architecture-Specific Builds**
   The official Docker images are built for linux/amd64 architecture. If you're running on a different architecture (like Apple Silicon/M1/M2), you have two options:

   a. **Use the official image with emulation:**
   ```bash
   # This will use QEMU emulation automatically
   docker compose up -d
   ```
   Note: This might be slower than a native build.

   b. **Build locally for your architecture:**
   ```bash
   # Build the image for your architecture
   docker build -t f1-champions-backend:local \
     --platform linux/$(uname -m | sed 's/x86_64/amd64/;s/arm64/arm64/') \
     -f infrastructure/Dockerfile .

   # Update docker-compose.yml to use your local image
   # Replace the image line with:
   # image: f1-champions-backend:local
   
   # Then start the services
   docker compose up -d
   ```
   This will build a native image for your architecture, which will run faster than the emulated version.

3. **Run the Android App**
   - Open the project in Android Studio
   - Select the `app` module
   - Choose a device/emulator
   - Click Run

### Running Tests

1. **Backend Tests**
   ```bash
   # Run all backend tests
   cd backend
   ./gradlew test
   
   # Run with coverage
   ./gradlew jacocoTestReport
   ```

2. **Android Tests**
   ```bash
   # Run all Android tests
   cd frontend
   ./gradlew test
   
   # Run with coverage
   ./gradlew koverHtmlReport
   ```

### Triggering the Pipeline

1. **Feature Development**
   ```bash
   # Create a new feature branch
   git checkout -b feature/your-feature develop
   
   # Make changes and commit
   git add .
   git commit -m "Your commit message"
   
   # Push and create PR
   git push origin feature/your-feature
   ```
   Then create a PR to `develop` on GitHub

2. **Deployment**
   - Create a PR from `develop` to `main`
   - Merge to trigger deployment
   - Check GitHub Actions for progress
   - Verify release and Docker image

## API Contract

### Base URL  

http://localhost:8080/api/  

### Endpoints

#### 1. Get All Seasons  

GET /api/seasons  


Retrieves a list of all Formula 1 seasons with their champions. Only seasons from 2005 onwards are available.

**Response:**
```json
[
  {
    "year": 2023,
    "championName": "Max Verstappen",
    "championPoints": 454,
    "championWins": 19
  },
  {
    "year": 2022,
    "championName": "Max Verstappen",
    "championPoints": 454,
    "championWins": 15
  }
  // ... more seasons
]
```

**Response Fields:**
- `year` (integer): The year of the F1 season (2005-present)
- `championName` (string): Full name of the season champion
- `championPoints` (integer): Total points scored by the champion
- `championWins` (integer): Number of race wins achieved by the champion

**Error Responses:**
- `500 Internal Server Error`: If there's an error fetching season data
  ```json
  {
    "status": 500,
    "error": "Internal Server Error",
    "message": "An error occurred while fetching season data"
  }
  ```

#### 2. Get Races for a Season

GET /api/seasons/{year}/races  

Retrieves all races and their winners for a given Formula 1 season. The year must be between 2005 and the current year.

**Path Parameters:**
- `year` (integer): The year of the F1 season (2005-present)

**Response:**
```json
[
  {
    "round": 1,
    "raceName": "Bahrain Grand Prix",
    "date": "2023-03-05",
    "circuitName": "Bahrain International Circuit",
    "winningDriverName": "Max Verstappen",
    "winningDriverNationality": "Dutch",
    "winningConstructorName": "Red Bull",
    "isSeasonChampionWinner": true
  },
  // ... more races
]
```

**Response Fields:**
- `round` (integer): Race round number in the season (1-24)
- `raceName` (string): Official name of the race
- `date` (string): Race date in ISO-8601 format (YYYY-MM-DD)
- `circuitName` (string): Official name of the circuit
- `winningDriverName` (string): Full name of the race winner
- `winningDriverNationality` (string): Nationality of the race winner
- `winningConstructorName` (string): Name of the winning constructor (team)
- `isSeasonChampionWinner` (boolean): Indicates if the race winner is also the season champion

**Error Responses:**
- `400 Bad Request`: If the year is invalid
  ```json
  {
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid year: 2004. Must be between 2005 and current year."
  }
  ```
- `404 Not Found`: If season data is not found
  ```json
  {
    "status": 404,
    "error": "Not Found",
    "message": "Season data for year 2023 not found. Please ensure season data is populated first."
  }
  ```
- `500 Internal Server Error`: If there's an error processing the request
  ```json
  {
    "status": 500,
    "error": "Internal Server Error",
    "message": "An error occurred while processing the request"
  }
  ```

### Rate Limiting
The API implements rate limiting to comply with the Ergast API's limits:
- 4 calls per second
- 200 calls per hour

When rate limits are exceeded, the API will return a `429 Too Many Requests` response with a `Retry-After` header indicating when to retry.

### Data Source
This API uses the Ergast API (http://ergast.com/mrd/) as its data source. All F1 data is provided through their public API and cached in a PostgreSQL database for improved performance and reliability.  

## CI/CD Pipeline

The project uses GitHub Actions for continuous integration and deployment. The pipeline consists of two main workflows:

### 1. PR Validation (`pr-validation.yml`)

This workflow runs on pull requests to both `develop` and `main` branches.

#### Trigger Conditions
- When a PR is opened
- When new commits are pushed to the PR
- When a PR is reopened

#### Jobs
1. **Changes Detection**
   - Detects which parts of the codebase have changed
   - Monitors changes in:
     - Backend code (`backend/**`)
     - Frontend code (`frontend/**`)
     - Workflow files (`.github/workflows/**`)

2. **Backend Build and Test** (runs if backend or workflow files changed)
   - Sets up JDK 17
   - Runs Gradle build and tests
   - Uploads test results and coverage reports
   - Artifacts:
     - Test results: `backend-test-results`
     - Coverage report: `backend-coverage-report`

3. **Android Build and Test** (runs if frontend or workflow files changed)
   - Sets up JDK 17
   - Runs Android tests and generates coverage reports
   - Builds debug APK
   - Uploads build artifacts:
     - APK: `frontend/app/build/outputs/apk/dev/debug/*.apk`
     - AAR modules: `frontend/*/build/outputs/aar/*.aar`
     - Coverage reports: `android-coverage-reports`

### 2. Deployment (`deploy.yml`)

This workflow runs when a pull request to the `main` branch is merged.

#### Trigger Conditions
- When a PR to `main` is merged

#### Jobs
1. **Changes Detection**
   - Similar to PR validation
   - Only runs if the PR was actually merged

2. **Publish and Deploy**
   - Downloads artifacts from the successful PR validation run
   - Verifies and processes the Android APK
   - Creates a GitHub Release with:
     - The APK file
     - Release notes from the PR description
     - Version tag based on the commit SHA
   - Builds and pushes the backend Docker image to GitHub Container Registry (GHCR)
     - Tags: `latest` and commit SHA
     - Includes all necessary environment variables and configurations

### Deployment Flow

1. **Feature Branch → Develop**
   - Create a feature branch from `develop`
   - Make changes and create a PR to `develop`
   - PR validation workflow runs
   - After merge to `develop`, no workflows run

2. **Develop → Main**
   - Create a PR from `develop` to `main`
   - PR validation workflow runs
   - After merge to `main`:
     - Deploy workflow runs
     - Creates GitHub Release with APK
     - Deploys backend Docker image to GHCR

### Artifacts and Releases

- **Android APK**: Available in GitHub Releases
  - Each release is tagged with the commit SHA
  - Release notes include the PR description
  - APK is signed with debug key for development

- **Backend Docker Image**: Available in GHCR
  - Image: `ghcr.io/<repository>/<image-name>`
  - Tags: `latest` and commit SHA
  - Includes all necessary environment variables
  - Uses multi-stage build for optimization

### Security

- Database credentials are handled securely using GitHub Secrets
- Docker images are built with resource limits
- All sensitive files are cleaned up after use
- Workflow permissions are scoped to minimum required access

### Monitoring

- Test results and coverage reports are available in the Actions tab
- Release history is maintained in GitHub Releases
- Docker image versions are tracked in GHCR  

### Test Coverage
   Test coverage reports are generated automatically for each pull request and can be accessed from the GitHub Actions workflow:
   1. Go to the "Actions" tab in the repository
   2. Select the latest successful workflow run
   3. Download the following artifacts:
      - `backend-coverage-report` for backend coverage
      - `android-coverage-reports` for Android coverage

#### Screenshots

The application consists of two main screens:

1. **Seasons List Screen**  

   ![Seasons List Screen](frontend/screenshots/ss_seasonlist.png)
   - Displays a list of F1 seasons
   - Shows the champion driver and constructor for each season
   - Allows navigation to race winners for a selected season

2. **Race Winners Screen**  

   ![Race Winners Screen](frontend/screenshots/ss_racewinners.png)
   - Shows all race winners for a selected season
   - Displays race name, date, and winner details
   - Highlights the winner if he's also the season's champion
   - Includes navigation back to seasons list  


#### Building and Testing

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Run tests:
   ```bash
   ./gradlew test
   ```

4. Generate coverage reports:
   ```bash
   ./gradlew koverHtmlReport
   ```

#### Development Guidelines

1. **Code Style**
   - Follow Kotlin style guide
   - Use ktlint for code formatting
   - Follow Material Design guidelines for UI

2. **Testing**
   - Write unit tests for all business logic
   - Use MockK for mocking
   - Maintain minimum 70% code coverage
   - Test both success and error cases

3. **Architecture**
   - Keep features modular and independent
   - Use dependency injection for better testability
   - Follow clean architecture principles
   - Keep UI and business logic separate

4. **UI/UX**
   - Use Jetpack Compose for all new UI
   - Follow Material Design 3 guidelines
   - Support dark/light themes
   - Handle configuration changes properly

## Contributing

1. Create a feature branch from `develop`
2. Make your changes
3. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.