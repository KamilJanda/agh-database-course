#!/bin/bash
neo4j-community-3.5.1/bin/neo4j start
sleep 5
go run main.go
neo4j-community-3.5.1/bin/neo4j stop
