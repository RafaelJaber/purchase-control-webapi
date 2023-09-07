#!/bin/sh
sudo docker run -e "SPRING_PROFILES_ACTIVE=prod" -p8080:8080 purchase-control/api


docker container run --name compras_api2 --hostname comprasapi.giganet.psi.br -e "SPRING_PROFILES_ACTIVE=prod" --network vlan1810 --ip 172.26.10.238 -v /etc/localtime:/etc/localtime:ro -v /etc/timezone:/etc/timezone:ro -d --restart always --cpus 4 --memory 1024mb rafaeljaber/compras:005825aad671

docker pull rafaeljaber/compras:005825aad671