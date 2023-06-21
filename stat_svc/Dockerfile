FROM amazoncorretto:11
COPY /Server/target/*.jar stat_svc.jar
ENTRYPOINT ["java","-jar","/stat_svc.jar"]