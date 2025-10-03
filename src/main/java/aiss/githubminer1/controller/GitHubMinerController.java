/*package aiss.githubminer.controller;

import aiss.githubminer.model.commits.Commit;
import aiss.githubminer.model.issues.Issue;
import aiss.githubminer.model.projects.Project;
import aiss.githubminer.service.*;
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
            @RequestParam(defaultValue = "2") int sinceCommits,
            @RequestParam(defaultValue = "20") int sinceIssues,
            @RequestParam(defaultValue = "2") int maxPages) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<Commit> commits = commitService.getAllCommits(owner, repoName, sinceCommits, maxPages);
            List<Issue> issues = issueService.getAllIssues(owner, repoName, sinceIssues, maxPages);

            Project project = new Project();
            project.setId("github:" + owner + "/" + repoName);
            project.setWebUrl("https://github.com/" + owner + "/" + repoName);
            project.setName(repoName);
            project.setCommits(commits);
            project.setIssues(issues);

            ResponseEntity<Void> gitMinerResponse = gitMinerService.sendDataToGitMiner(project);

            response.put("estado", "OK");
            response.put("mensaje", "Proceso completado. Datos enviados a GitMiner.");
            response.put("owner", owner);
            response.put("repo", repoName);
            response.put("commits_procesados", commits.size());
            response.put("issues_procesados", issues.size());
            response.put("gitminer_status", gitMinerResponse.getStatusCode().value());
            response.put("gitminer_success", gitMinerResponse.getStatusCode().is2xxSuccessful());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("estado", "ERROR");
            response.put("mensaje", "Hubo un problema al procesar la solicitud.");
            response.put("detalle", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{owner}/{repoName}")
    public ResponseEntity<Map<String, Object>> fetchPreview(
            @PathVariable String owner,
            @PathVariable String repoName,
            @RequestParam(defaultValue = "2") int sinceCommits,
            @RequestParam(defaultValue = "20") int sinceIssues,
            @RequestParam(defaultValue = "2") int maxPages) {

        Map<String, Object> preview = new HashMap<>();
        try {
            preview.put("status", "PREVIEW_LISTO");
            preview.put("commits", commitService.getAllCommits(owner, repoName, sinceCommits, maxPages));
            preview.put("issues", issueService.getAllIssues(owner, repoName, sinceIssues, maxPages));
        } catch (Exception e) {
            preview.put("status", "ERROR");
            preview.put("error", "No se pudo generar la previsualización: " + e.getMessage());
        }
        return ResponseEntity.ok(preview);
    }

}*/
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

    // Endpoint principal para extraer y enviar datos
    @PostMapping("/{owner}/{repoName}")
    public ResponseEntity<Map<String, Object>> fetchAndSendData(
            @PathVariable String owner,
            @PathVariable String repoName,
            @RequestParam(defaultValue = "5") int sinceCommits,
            @RequestParam(defaultValue = "5") int sinceIssues,
            @RequestParam(defaultValue = "2") int maxPages) {

        Map<String, Object> finalResponse = new HashMap<>();

        try {
            // Paso 1: Obtener los datos de Bitbucket
            List<Commit> commits = commitService.getAllCommits(owner, repoName, sinceCommits, maxPages);
            List<Issue> issues = issueService.getAllIssues(owner, repoName, sinceIssues, maxPages);

            // Paso 2: Crear el objeto Project (por si lo necesitamos en el futuro)
            Project proj = new Project();
            proj.setId(owner + "/" + repoName);
            proj.setName(repoName);
            proj.setWebUrl("https://github.com/" + owner + "/" + repoName); // Corregido: sin espacios extra
            proj.setCommits(commits);
            proj.setIssues(issues);

            // Paso 3: Enviar los datos a GitMiner y obtener el reporte
            ResponseEntity<Project> gitMinerResponse = gitMinerService.sendDataToGitMiner(proj);

            // Paso 4: Preparar la respuesta para el cliente
            finalResponse.put("estado", "OK");
            finalResponse.put("mensaje", "Proceso completado. Datos enviados a GitMiner.");
            finalResponse.put("gitminer_status", gitMinerResponse.getStatusCode().value());
            finalResponse.put("gitminer_success", gitMinerResponse.getStatusCode().is2xxSuccessful());
            finalResponse.put("project_enviado", proj);

        } catch (Exception e) {
            // Si algo falla, devolvemos un error genérico
            finalResponse.put("estado", "ERROR");
            finalResponse.put("mensaje", "Hubo un problema al procesar la solicitud.");
            finalResponse.put("detalle", e.getMessage()); // Solo el mensaje, no el stack trace completo
            return ResponseEntity.badRequest().body(finalResponse);
        }

        return ResponseEntity.ok(finalResponse);
    }

    // Endpoint de previsualización (solo para ver qué datos se extraerían)
    @GetMapping("/{owner}/{repoName}")
    public ResponseEntity<Object> fetchPreview(
            @PathVariable String owner,
            @PathVariable String repoName,
            @RequestParam(defaultValue = "5") int sinceCommits,
            @RequestParam(defaultValue = "5") int sinceIssues,
            @RequestParam(defaultValue = "2") int maxPages) {

        try {
            List<Commit> commits = commitService.getAllCommits(owner, repoName, sinceCommits, maxPages);
            List<Issue> issues = issueService.getAllIssues(owner, repoName, sinceIssues, maxPages);

            // Paso 2: Crear el objeto Project (por si lo necesitamos en el futuro)
            Project proj = new Project();
            proj.setId(owner + "/" + repoName);
            proj.setName(repoName);
            proj.setWebUrl("https://github.com/" + owner + "/" + repoName); // Corregido: sin espacios extra
            proj.setCommits(commits);
            proj.setIssues(issues);

            return ResponseEntity.ok(proj);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("error", "No se pudo generar la previsualización: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}