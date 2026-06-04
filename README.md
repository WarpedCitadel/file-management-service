# file-management-service
This project contains the source code and supporting files for the applications file management mircoservice.

## Deploy the application
This project uses Docker to deploy in local, development, and production environments.

### Use Docker to build and test locally
Build the application image with the `docker build` command.
```bashrc
docker build -t file-management-service/local .
```
Docker uses the projects `dockerfile` to build a multistaged docker image.
The image builder, uses `maven:3.9-amazoncorretto-25-alpine` to build and compile the project, then executes `mvn clean package` to create a run target .jar file called `file-management-service-0.0.1-SNAPSHOT.jar` inside a newly created folder called `/app`.

The image runner, uses `amazoncorretto:25-alpine` to run the projects newly compiled Java application located in the `/app` folder using Maven.

Build the docker container using `docker-compose up`.
```bashrc
docker compose up wc_local -d
```
Referencing the `docker-compose.yml`, Docker creates a container called `wc_local` with the defined image `file-management-service/local` and port on `8080`. The `.env` file references the applications secrets needed to run.
```yml
services:
  wc_local:
    image: file-management-service/local
    env_file:
      - .env
    ports:
      - "8081:8081"
```

## Using Make for local Docker deployments
To use `make` you must install the build automation tool in a Linux terminal with the following command.
```bashrc
sudo apt install make
```
Once `make` is installed, navigate to the root directory of the project via `/file-management-service` where the `Makefile` is located and enter `make readme` as a command.
```bashrc
make readme
```
The `make` target *readme* will display inside the terminal a list of make commands for deployment.

### make deploy_local
Build the local docker image and build local docker container on localhost:8080.
```bashrc
make deploy_local
```

### make rip_deploy_local
Stop the local docker container and rebuild the docker image and container on localhost:8080.
```bashrc
make rip_deploy_local
```

### make rip_local
Stop and delete the local docker container. Then delete the local docker image on localhost:8080.
```bashrc
make rip_local
```