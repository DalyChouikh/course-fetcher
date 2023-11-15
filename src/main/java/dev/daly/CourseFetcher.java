package dev.daly;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Collectors;

public class CourseFetcher {

    private static String formatFileName(String fileName){
        return  fileName.replaceAll("[/?*:|<>\\\\]", "");
    }

    private static JsonNode readFromFile(){
            String coursesJsonPath = "courses.json";
        try{
            ObjectMapper mapper = new ObjectMapper();
            BufferedReader reader = new BufferedReader(new FileReader(coursesJsonPath));
            return mapper.readTree(reader.lines().collect(Collectors.joining("\n")));
        }catch (IOException e){
            System.out.println("Error reading file: " + coursesJsonPath);
            System.exit(1);
        }
        return null;
    }

    private static void createDirectoriesWindows(JsonNode coursesJson) throws IOException, InterruptedException {
        for(JsonNode subject : coursesJson){
            String subjectName = subject.get("designation").asText();
            String downloadPath = ".\\courses\\'" + subjectName.split("/")[0] + "'\\";
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
        }else {
            System.out.println("Directory already exists: " + downloadPath);
        }
        for (JsonNode course : subject.get("courses")){
            String courseName = formatFileName(course.get("title").asText());
            String courseURL ="https://issatso.rnu.tn/bo/storage/app/public/courses/" +
                    course.get("folder").asText().split("/")[1] + "/" ;
            String extension = course.get("folder").asText().split("\\.")[1];
            URL url = URI.create(courseURL).toURL();
            Path destination = Path.of(downloadPath + courseName  + "." + extension);
            Files.copy(url.openStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Downloaded: " + courseName + "." + extension);
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        JsonNode coursesJson = readFromFile();
        if(System.getProperty("os.name").toLowerCase().contains("windows"))
            createDirectoriesWindows(coursesJson);
        else createDirectoriesLinux(coursesJson);

    }

}
