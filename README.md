# Project template for Metatester

This is a sample project demonstrating Metatester. It uses a mocked REST API

## Set up the REST API
1. `docker pull wiremock/wiremock:latest`
2. `docker run -d --name wiremock-mocked-rest-api -p 8089:8089 -v /mappings:/home/wiremock/mappings wiremock/wiremock:latest --port 8089 --verbose`
- `mappings` folder contains mock api resources that are loaded by Wiremock service

## Run tests
Run tests with ```./gradlew clean test --info -DrunWithMetatester=true```

Set `-DrunWithMetatester=false` to run tests without Metatester
