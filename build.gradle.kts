plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jetty:jetty-server:11.0.7")
    implementation("org.glassfish.jersey.containers:jersey-container-jetty-http:3.0.3")
    implementation("org.glassfish.jersey.inject:jersey-hk2:3.0.3")
    implementation("org.glassfish.jersey.media:jersey-media-json-jackson:3.0.3")
    implementation("jakarta.servlet:jakarta.servlet-api:5.0.0")
}