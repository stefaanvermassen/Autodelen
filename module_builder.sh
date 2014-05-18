#!/bin/bash

mkdir -p lib
cd modules/dao
mvn -Dmaven.test.skip=true install
cp "target/dao-1.0.jar" ../../lib/dao.jar
cd ..
