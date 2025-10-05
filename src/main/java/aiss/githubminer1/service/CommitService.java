package aiss.githubminer1.service;

import aiss.githubminer1.model.commits.Commit;
import aiss.githubminer1.model.commits.MapCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CommitService {

    @Autowired
    RestTemplate restTemplate;

    public List<Commit> getAllCommits(String owner, String repo, int sinceCommits, int maxPages) {
        List<Commit> commits = new ArrayList<>();
        String baseUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/commits";
        LocalDateTime sinceDateTime = LocalDateTime.now().minusDays(sinceCommits);
        String sinceIso = sinceDateTime.format(DateTimeFormatter.ISO_DATE_TIME);

        for (int page = 1; page <= maxPages; page++) {
            String uri = baseUrl + "?since=" + sinceIso + "&page=" + page + "&per_page=100";
            ResponseEntity<MapCommit[]> response = restTemplate.getForEntity(uri, MapCommit[].class);
            MapCommit[] mapCommits = response.getBody();
            if (mapCommits != null) {
                for (MapCommit mc : mapCommits) {
                    Commit commit = new Commit();
                    commit.setId(mc.getSha());
                    commit.setTitle(null); // aqui no se que poner porque no viene ningun atributo llamado tittle
                    commit.setMessage(mc.getCommit().getMessage());
                    commit.setAuthorName(mc.getCommit().getAuthor().getName());
                    commit.setAuthorEmail(mc.getCommit().getAuthor().getEmail());
                    commit.setAuthoredDate(mc.getCommit().getAuthor().getDate());
                    commit.setWebUrl(mc.getUrl());

                    commits.add(commit);
                }
                return commits;
            }
        }
        return Collections.emptyList();
    }

    public Commit getCommitById(String sha, String owner, String repo, int sinceCommits, int maxPages) {
        String uri = "https://api.github.com/repos/" + owner + "/" + repo + "/commits";

        if (getAllCommits(repo, owner, sinceCommits, maxPages) != null) {
            Optional<Commit> commitOpt = getAllCommits(repo, owner, sinceCommits, maxPages)
                    .stream()
                    .filter(commit -> commit.getId().equals(sha))
                    .findFirst();
            return commitOpt.orElse(null); // Devuelve null si no se encuentra
        }

        return null;
    }
}
