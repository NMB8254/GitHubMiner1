package aiss.githubminer1.service;

import aiss.githubminer1.model.commits.Commit;
import aiss.githubminer1.model.issues.Issue;
import aiss.githubminer1.model.projects.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GitMinerService {

    @Autowired
    RestTemplate restTemplate;

    private static final String BASE_URL = "http://localhost:8080/gitminer"; // añadir a este enlace "/projects// "

    public ResponseEntity<Project> sendDataToGitMiner(Project project) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Project> requestEntity = new HttpEntity<>(project, headers);

        return restTemplate.exchange(BASE_URL + "/projects", HttpMethod.POST, requestEntity, Project.class);
    }
}
