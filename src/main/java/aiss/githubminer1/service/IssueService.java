package aiss.githubminer1.service;

import aiss.githubminer1.model.User;
import aiss.githubminer1.model.comments.Comment;
import aiss.githubminer1.model.comments.MapUserComment;
import aiss.githubminer1.model.commits.Commit;
import aiss.githubminer1.model.commits.MapCommit;
import aiss.githubminer1.model.issues.*;
import aiss.githubminer1.model.projects.Project;
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
public class IssueService {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private CommentService commentService;

    public List<Issue> getAllIssues(String owner, String repo, int sinceIssues, int maxPages) {
        List<Issue> issues = new ArrayList<>();
        String baseUrl = "https://api.github.com/repos/" + owner + "/" + repo + "/issues";
        String sinceIso = LocalDateTime.now().minusDays(sinceIssues).format(DateTimeFormatter.ISO_DATE_TIME);

        for (int page = 1; page <= maxPages; page++) {
            String uri = baseUrl + "?since=" + sinceIso + "&page=" + page + "&per_page=100";
            ResponseEntity<MapIssue[]> response = restTemplate.getForEntity(uri, MapIssue[].class);
            MapIssue[] mapIssues = response.getBody();
            if (mapIssues != null) {
                for (MapIssue mi : mapIssues) {
                    Issue issue = new Issue();
                    issue.setId(String.valueOf(mi.getId()));
                    issue.setTitle(mi.getTitle());
                    issue.setDescription(mi.getBody());
                    issue.setState(mi.getState());
                    issue.setCreatedAt(mi.getCreatedAt());
                    issue.setUpdatedAt(mi.getUpdatedAt());
                    issue.setClosedAt(mi.getClosedAt());

                    List<String> labels = (mi.getLabels() != null)
                            ? mi.getLabels().stream().map(Label::getName).toList()
                            : new ArrayList<>();
                    issue.setLabels(labels);

                    issue.setVotes(mi.getComments());
                    issue.setComments(commentService.getAllCommentsFromIssue(owner, repo, mi.getNumber()));

                    MapUserIssue mapUser = mi.getUser();
                    if (mapUser != null) {
                        User user = new User();
                        user.setId(String.valueOf(mapUser.getId()));
                        user.setUsername(mapUser.getLogin());
                        user.setAvatarUrl(mapUser.getAvatarUrl());
                        user.setWebUrl(mapUser.getHtmlUrl());
                        user.setName(null);
                        issue.setAuthor(user);
                    }

                    if (mi.getAssignee() != null) {
                        Assignee ghAssignee = mi.getAssignee();
                        User assignee = new User();
                        assignee.setId(String.valueOf(ghAssignee.getId()));
                        assignee.setUsername(ghAssignee.getLogin());
                        assignee.setAvatarUrl(ghAssignee.getAvatarUrl());
                        assignee.setWebUrl(ghAssignee.getHtmlUrl());
                        assignee.setName(null);
                        issue.setAssignee(assignee);
                    }

                    issues.add(issue);
                }
            }
        }
        return issues;
    }

    public Issue getIssueById(String id, String repo, String owner, int sinceIssues, int maxPages) {
        String uri = "https://api.github.com/repos/" + owner + "/" + repo + "/issues";

        if (getAllIssues(repo, owner, sinceIssues, maxPages) != null) {
            Optional<Issue> issueOpt = getAllIssues(repo, owner, sinceIssues, maxPages)
                    .stream()
                    .filter(issue -> issue.getId().equals(id))
                    .findFirst();
            return issueOpt.orElse(null); // Devuelve null si no se encuentra
        }

        return null;
    }

    public List<Issue> getIssuesByAuthorId(String authorId, String repo, String owner, int sinceIssues, int maxPages) {
        List<Issue> allIssues = getAllIssues(repo, owner, sinceIssues, maxPages);
        if (allIssues == null || allIssues.isEmpty()) {
            return Collections.emptyList();
        }

        return allIssues.stream()
                .filter(issue -> issue.getAuthor() != null && authorId.equals(issue.getAuthor().getId()))
                .toList();

    }

    public List<Issue> getIssuesByState(String state, String repo, String owner, int sinceIssues, int maxPages) {
        List<Issue> allIssues = getAllIssues(repo, owner, sinceIssues, maxPages);
        if (allIssues == null || allIssues.isEmpty()) {
            return Collections.emptyList();
        }

        return allIssues.stream()
                .filter(issue -> issue.getState() != null &&
                        issue.getState().equalsIgnoreCase(state))
                .toList();
    }
}
