LOCAL_APP_IP	?=localhost
LOCAL_APP_PORT	?=8081

IMAGE_NAME =file-management-service
CONTAINER_NAME =file-management-service

readme deploy_local: localappip    :=$(LOCAL_APP_IP)
readme deploy_local: localappport  :=$(LOCAL_APP_PORT)


.PHONY: readme deploy_local rip_deploy_local rip_local deploy_dev rip_dev deploy_prod rip_prod

readme:
	@echo \
"\n**** Stop! You must run make with a specific target! ****\n\
\n\
To manage a container for file-management-service, use one of these targets.\n\
Ensure you have a .env file to map secrets for file-management-service application.properties file.\n\
If you do not have an .env file you must make one in the root directory in order for SpringBoot to work.\n\
Before using the make commands or docker commands verify if the docker daemon is active\n\
And for dev, prod, qa deployments require the docker image to be pulled down from dockerhub\n\
For more help refer to the documentation in Github.\n\
\n\
	rip_deploy_local	Stop the local docker container and rebuild the docker image and container on $(localappip):$(localappport).\n\
\n\
	deploy_local		Build the local docker image and build local docker container on $(localappip):$(localappport).\n\
\n\
	rip_local		Stop the local docker container on $(localappip):$(localappport).\n\
\n\
\n\
	deploy_dev		Build and deploy the dev docker container on $(localappip):$(localappport).\n\
\n\
	rip_dev			Stop the dev docker container on $(localappip):$(localappport).\n\
\n\
\n\
	deploy_prod		Build and deploy the prod docker container on $(localappip):$(localappport).\n\
\n\
	rip_prod		Stop the prod docker container on $(localappip):$(localappport).\n"


# ------ deploy application ------

deploy_local:
	@echo Deploying LOCAL $(CONTAINER_NAME) at $(localappip):$(localappport). Are you sure? [Y/n]
	@read line; if [ ! $$line = "Y" ] && [ ! $$line = "y" ]; then echo Aborting...; exit 1; fi

	@echo "Building image $(IMAGE_NAME)/local..."
	docker build -t $(IMAGE_NAME)/local .

	@echo "Building container $(CONTAINER_NAME)..."
	docker compose up wc_local --build -d

deploy_dev:
	@echo Deploying LOCAL $(CONTAINER_NAME) at $(localappip):$(localappport). Are you sure? [Y/n]
	@read line; if [ ! $$line = "Y" ] && [ ! $$line = "y" ]; then echo Aborting...; exit 1; fi

	@echo pulling warpedcitadel/$(IMAGE_NAME)-dev
	docker pull warpedcitadel/$(IMAGE_NAME):dev

	@echo "Building container $(CONTAINER_NAME)..."
	docker compose up wc_dev -d

deploy_prod:
	@echo Deploying LOCAL $(CONTAINER_NAME) at $(localappip):$(localappport). Are you sure? [Y/n]
	@read line; if [ ! $$line = "Y" ] && [ ! $$line = "y" ]; then echo Aborting...; exit 1; fi

	@echo pulling warpedcitadel/$(IMAGE_NAME)-prod
	docker pull warpedcitadel/$(IMAGE_NAME):prod

	@echo "Building container $(CONTAINER_NAME)..."
	docker compose up wc_prod -d


# ------ rip application ------
rip_local:
	@echo Ripping LOCAL $(CONTAINER_NAME) at $(localappip):$(localappport). Are you sure? [Y/n]
	@read line; if [ ! $$line = "Y" ] && [ ! $$line = "y" ]; then echo Aborting...; exit 1; fi

	@echo "Stopping and removing container $(CONTAINER_NAME)..."
	-docker stop $(CONTAINER_NAME)-wc_local-1
	-docker rm $(CONTAINER_NAME)-wc_local-1

	@echo "Removing image $(IMAGE_NAME)/local..."
	-docker rmi -f $(IMAGE_NAME)/local

rip_dev:
	@echo Ripping DEV $(CONTAINER_NAME)/dev at $(localappip):$(localappport). Are you sure? [Y/n]
	@read line; if [ ! $$line = "Y" ] && [ ! $$line = "y" ]; then echo Aborting...; exit 1; fi

	@echo "Stopping and removing container $(CONTAINER_NAME)..."
	-docker stop $(CONTAINER_NAME)-wc_dev-1
	-docker rm $(CONTAINER_NAME)-wc_dev-1
	
	@echo "Removing image $(IMAGE_NAME)..."
	-docker rmi -f warpedcitadel/$(IMAGE_NAME):dev

rip_prod:
	@echo Ripping PROD $(CONTAINER_NAME) at $(localappip):$(localappport). Are you sure? [Y/n]
	@read line; if [ ! $$line = "Y" ] && [ ! $$line = "y" ]; then echo Aborting...; exit 1; fi

	@echo "Stopping and removing container $(CONTAINER_NAME)-prod..."
	-docker stop $(CONTAINER_NAME)-wc_prod-1
	-docker rm $(CONTAINER_NAME)-wc_prod-1

	@echo "Removing image $(IMAGE_NAME)..."
	-docker rmi -f warpedcitadel/$(IMAGE_NAME):prod


# ------ rip and deploy application ------
rip_deploy_local:
	@echo Ripping and redeploying LOCAL $(CONTAINER_NAME) at $(localappip):$(localappport). Are you sure? [Y/n]
	@read line; if [ ! $$line = "Y" ] && [ ! $$line = "y" ]; then echo Aborting...; exit 1; fi

	@echo "Stopping $(CONTAINER_NAME)..."
	docker compose down

	@echo "Building image $(IMAGE_NAME)..."
	docker build -t $(IMAGE_NAME)/local .

	@echo "Building container file-management-service..."
	docker compose up wc_local --build -d