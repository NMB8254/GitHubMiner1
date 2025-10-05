package aiss.githubminer1.controller;

import aiss.githubminer1.model.projects.Project;
import aiss.githubminer1.model.commits.Commit;
import aiss.githubminer1.model.issues.Issue;
import aiss.githubminer1.service.CommitService;
import aiss.githubminer1.service.GitMinerService;
import aiss.githubminer1.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/github")
public class GitHubMinerController {

    @Autowired
    private CommitService commitService;
    @Autowired
    private IssueService issueService;
    @Autowired
    private GitMinerService gitMinerService;

    @PostMapping("/{owner}/{repoName}")
    public ResponseEntity<Map<String, Object>> fetchAndSendData(
            @PathVariable String owner,
            @PathVariable String repoName,
            @RequestParam(defaultValue = "5") int sinceCommits,
            @RequestParam(defaultValue = "5") int sinceIssues,
            @RequestParam(defaultValue = "2") int maxPages) {

        Map<String, Object> finalResponse = new HashMap<>();

        try {
            List<Commit> commits = commitService.getAllCommits(owner, repoName, sinceCommits, maxPages);
            List<Issue> issues = issueService.getAllIssues(owner, repoName, sinceIssues, maxPages);

            Project proj = new Project();
            proj.setId(owner + "/" + repoName);
            proj.setName(repoName);
            proj.setWebUrl("https://github.com/" + owner + "/" + repoName);
            proj.setCommits(commits);
            proj.setIssues(issues);

            Map<String, Object> gitMinerResponse = gitMinerService.sendDataToGitMiner(owner, repoName, commits, issues);

            finalResponse.put("estado", "OK");
            finalResponse.put("mensaje", "Proceso completado. Datos enviados a GitMiner.");
            finalResponse.put("workspace", owner);
            finalResponse.put("repo", repoName);
            finalResponse.put("commits_procesados", commits.size());
            finalResponse.put("issues_procesados", issues.size());
            finalResponse.put("reporte_gitminer", gitMinerResponse);

            finalResponse.put("commits_enviados", commits);
            finalResponse.put("issues_enviados", issues);

        } catch (Exception e) {
            finalResponse.put("estado", "ERROR");
            finalResponse.put("mensaje", "Hubo un problema al procesar la solicitud.");
            finalResponse.put("detalle", e.getMessage());
            return ResponseEntity.badRequest().body(finalResponse);
        }

        return ResponseEntity.ok(finalResponse);
    }

    @GetMapping("/{owner}/{repoName}")
    public ResponseEntity<Object> fetchPreview(
            @PathVariable String owner,
            @PathVariable String repoName,
            @RequestParam(defaultValue = "5") int sinceCommits,
            @RequestParam(defaultValue = "5") int sinceIssues,
            @RequestParam(defaultValue = "2") int maxPages) {

        Map<String, Object> preview = new HashMap<>();
        try {
            preview.put("commits", commitService.getAllCommits(owner, repoName, sinceCommits, maxPages));
            preview.put("issues", issueService.getAllIssues(owner, repoName, sinceIssues, maxPages));
            preview.put("status", "PREVIEW_LISTO");
        } catch (Exception e) {
            preview.put("status", "ERROR");
            preview.put("error", "No se pudo generar la previsualización: " + e.getMessage());
        }
        return ResponseEntity.ok(preview);
    }
}