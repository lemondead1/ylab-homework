FROM eclipse-temurin:17
RUN mkdir /opt/carshop-service
ARG JAR_PATH
ADD ${JAR_PATH} /opt/carshop-service/CarShop-Service.jar
CMD java -jar /opt/carshop-service/CarShop-Service.jar
EXPOSE 8080