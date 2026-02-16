FROM ubuntu:latest
LABEL authors="evgen"

ENTRYPOINT ["top", "-b"]