# prl-cos-api

[![Build Status](https://travis-ci.org/hmcts/prl-cos-api.svg?branch=master)](https://travis-ci.org/hmcts/prl-cos-api)

## Notes

This is manage order changes base branch test.

Update with master - Respondent dynamic task list with stop representation.

Since Spring Boot 2.1 bean overriding is disabled. If you want to enable it you will need to set `spring.main.allow-bean-definition-overriding` to `true`.


JUnit 5 is now enabled by default in the project. Please refrain from using JUnit4 and use the next generation

### Building and deploying the application

### Building the applications

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker compose build
```

Run the distributions (created in `build/install/prl-cos-api` directory)
by executing the following command:

```bash
  docker compose up
```

This will start the API container exposing the application's port
(set to `4044` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4044/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

### Troubleshooting

### Managing Preview environment PODs
Make sure you have added the label 'enable_keep_helm' while creating the PR. Otherwise, add the label and re-trigger the build.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
