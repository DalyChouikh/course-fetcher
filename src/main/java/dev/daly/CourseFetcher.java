package dev.daly;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CourseFetcher {
    private static final ObjectMapper mapper = new ObjectMapper();

    private static String formatFileName(String fileName){
        return  fileName.replaceAll("[/?*:|<>\\\\]", "");
    }

    private static JsonNode fetchCoursesJson(String token) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://issatso.rnu.tn/bo/public/api/student/courses"))
                .header("Authorization", "Bearer " + token)
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        return mapper.readTree(response.body());
    }

    private static void createDirectoriesWindows(JsonNode coursesJson) throws IOException, InterruptedException {
        for(JsonNode subject : coursesJson){
            String subjectName = subject.get("designation").asText();
            String downloadPath = ".\\courses\\'" + subjectName.split("/")[0].trim() + "'\\";
            createDirectories(subject, downloadPath);
        }
    }

    private static void createDirectoriesLinux(JsonNode coursesJson) throws IOException, InterruptedException {
        for(JsonNode subject : coursesJson){
            String subjectName = subject.get("designation").asText();
            String downloadPath = "./courses/'" + subjectName.split("/")[0] + "'/";
            createDirectories(subject, downloadPath);
        }
    }

    private static void createDirectories(JsonNode subject, String downloadPath) throws IOException {
        Path path = Path.of(downloadPath);
        if(!Files.exists(path)){
            Files.createDirectories(path);
        }
        for (JsonNode course : subject.get("courses")){
            String courseName = formatFileName(course.get("title").asText());
            String courseURL ="https://issatso.rnu.tn/bo/storage/app/public/courses/" +
                    course.get("folder").asText().split("/")[1] ;
            String courseFileExtension = course.get("folder").asText().split("\\.")[1];
            URL url = URI.create(courseURL).toURL();
            Path destination = Path.of(downloadPath + courseName  + "." + courseFileExtension);
            Files.copy(url.openStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Downloaded: " + courseName + "." + courseFileExtension);
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your token: ");
        String token = scanner.nextLine();
        JsonNode coursesJson = fetchCoursesJson(token);
        if(System.getProperty("os.name").toLowerCase().contains("windows"))
            createDirectoriesWindows(coursesJson);
        else createDirectoriesLinux(coursesJson);

    }

}
