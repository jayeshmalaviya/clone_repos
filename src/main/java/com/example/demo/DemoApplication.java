package com.example.demo;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

  private static final String GITHUB_TOKEN = "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"; // Replace with your GitHub token
  private static final String GITHUB_API_URL = "https://api.github.com/orgs/gaiangroup/repos";
  private static final String CLONE_DIR = "/home/gaian/Downloads/repos"; // Replace with your username

  public static void main(String[] args) {

    SpringApplication.run(DemoApplication.class, args);


    try {
      List<String> repoUrls = getRepoUrls();
      cloneRepos(repoUrls);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static List<String> getRepoUrls() throws Exception {
    List<String> repoUrls = new ArrayList<>();

    URL url = new URL(GITHUB_API_URL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod("GET");
    conn.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);

    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    String inputLine;
    StringBuilder content = new StringBuilder();
    while ((inputLine = in.readLine()) != null) {
      content.append(inputLine);
    }
    in.close();
    conn.disconnect();

    JsonArray jsonArray = JsonParser.parseString(content.toString()).getAsJsonArray();
    for (JsonElement element : jsonArray) {
      JsonObject repo = element.getAsJsonObject();
      String sshUrl = repo.get("ssh_url").getAsString();
      repoUrls.add(sshUrl);
    }

    return repoUrls;
  }

  private static void cloneRepos(List<String> repoUrls) {
    for (String repoUrl : repoUrls) {
      try {
        ProcessBuilder processBuilder = new ProcessBuilder("git", "clone", repoUrl, CLONE_DIR + "/" + getRepoName(repoUrl));
        processBuilder.inheritIO();
        Process process = processBuilder.start();
        process.waitFor();
      } catch (Exception e) {
        System.out.println("Failed to clone repository: " + repoUrl);
        e.printStackTrace();
      }
    }
  }

  private static String getRepoName(String repoUrl) {
    return repoUrl.substring(repoUrl.lastIndexOf("/") + 1, repoUrl.lastIndexOf(".git"));
  }
}
