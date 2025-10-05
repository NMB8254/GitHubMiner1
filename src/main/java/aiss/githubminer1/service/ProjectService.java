package aiss.githubminer1.service;

import aiss.githubminer1.model.projects.MapProject;
import aiss.githubminer1.model.projects.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    CommitService commitService;
    @Autowired
    IssueService issueService;

    public List<Project> getAllProjects(String repo, String owner, int sinceCommits, int sinceIssues, int maxPages) {
        List<Project> projects = new ArrayList<>();
        String uri = "https://api.github.com/repos/" + owner + "/" + repo + "/projects";
        ResponseEntity<MapProject[]> response = restTemplate.getForEntity(uri, MapProject[].class);
        MapProject[] mapProjects = response.getBody();

        if (mapProjects != null) {
            for (MapProject mp : mapProjects) {
                Project project = new Project();
                project.setId(String.valueOf(mp.getId()));
                project.setName(mp.getName());
                project.setWebUrl(mp.getHtmlUrl());
                project.setCommits(commitService.getAllCommits(repo, owner, sinceCommits, maxPages));
                project.setIssues(issueService.getAllIssues(repo, owner, sinceIssues, maxPages));

                projects.add(project);
            }
            return projects;
        }

        return Collections.emptyList();
    }

    public Project createProject(Project projectInput) {
        Project project = new Project();
        project.setId(projectInput.getId());
        project.setName(projectInput.getName());
        project.setWebUrl(projectInput.getWebUrl());

        if (projectInput.getCommits() != null) {
            project.setCommits(new ArrayList<>(projectInput.getCommits()));
        }

        if (projectInput.getIssues() != null) {
            project.setIssues(new ArrayList<>(projectInput.getIssues()));
        }

        return project;
    }

    public Project getProjectById(String id, String repo, String owner, int sinceCommits, int sinceIssues, int maxPages) {
        String uri = "https://api.github.com/repos/" + owner + "/" + repo + "/projects";

        if (getAllProjects(repo, owner, sinceCommits, sinceIssues, maxPages) != null) {
            Optional<Project> projectOpt = getAllProjects(repo, owner, sinceCommits, sinceIssues, maxPages)
                    .stream()
                    .filter(project -> project.getId().equals(id))
                    .findFirst();
            return projectOpt.orElse(null); // Devuelve null si no se encuentra
        }

        return null;
    }

}
