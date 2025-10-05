package aiss.githubminer1.service;

import aiss.githubminer1.model.commits.Commit;
import aiss.githubminer1.model.issues.Issue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class GitMinerService {

    @Autowired
    RestTemplate restTemplate;

    private static final String BASE_URL = "http://localhost:8080/gitminer"; // añadir a este enlace "/projects// "

    public Map<String, Object> sendDataToGitMiner(String owner, String repoName, List<Commit> commits, List<Issue> issues) {
        try {
            Map<String, Object> projectPayload = new HashMap<>();
            projectPayload.put("id", owner + "/" + repoName);
            projectPayload.put("name", repoName);
            projectPayload.put("web_url", "https://bitbucket.org/" + owner + "/" + repoName);
            projectPayload.put("commits", commits);
            projectPayload.put("issues", issues);

            System.out.println(">>> ENVIANDO DATOS A GITMINER: " + BASE_URL + "/projects");
            System.out.println(">>> PAYLOAD: " + projectPayload);

            // 3. Enviar el PROYECTO ÚNICO a GitMiner
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    BASE_URL + "/projects",
                    projectPayload,
                    Void.class
            );

            System.out.println(">>> RESPUESTA DE GITMINER: " + response.getStatusCode());

            Map<String, Object> result = new HashMap<>();
            if (response.getStatusCode().is2xxSuccessful()) {
                result.put("status", "success");
                result.put("project_id", projectPayload.get("id"));
                result.put("commits_sent", commits.size());
                result.put("issues_sent", issues.size());
            } else {
                result.put("status", "failure");
                result.put("http_status", response.getStatusCodeValue());
            }
            return result;

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return error;
        }
    }
}
