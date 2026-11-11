# https://hub.docker.com/_/eclipse-temurin
FROM eclipse-temurin:21-jdk-alpine

# Create and change to the app directory.
WORKDIR /app

# Copy local code to the container image.
COPY . ./

# Build the app.
RUN chmod +x mvnw
RUN ./mvnw -DoutputFile=target/mvn-dependency-list.log -B -DskipTests clean dependency:list install

# Run the Java app.
CMD ["sh", "-c", "java -jar target/consultaviva-0.0.1-SNAPSHOT.jar"]
