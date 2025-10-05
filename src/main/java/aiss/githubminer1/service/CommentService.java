package aiss.githubminer1.service;

import aiss.githubminer1.model.User;
import aiss.githubminer1.model.comments.Comment;
import aiss.githubminer1.model.comments.MapComment;
import aiss.githubminer1.model.comments.MapUserComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    RestTemplate restTemplate;

    public List<Comment> getAllCommentsFromIssue(String owner, String repo, Integer issueNumber) {
        List<Comment> comments = new ArrayList<>();
        String uri = "https://api.github.com/repos/" + owner + "/" + repo + "/issues/" + issueNumber + "/comments";
        ResponseEntity<MapComment[]> response = restTemplate.getForEntity(uri, MapComment[].class);
        MapComment[] mapComments = response.getBody();

        if (mapComments != null) {
            for (MapComment mc : mapComments) {
                Comment comment = new Comment();
                comment.setId(String.valueOf(mc.getId()));
                comment.setBody(mc.getBody());
                comment.setCreatedAt(mc.getCreatedAt());
                comment.setUpdatedAt(mc.getUpdatedAt());

                MapUserComment mapUser = mc.getUser();
                if (mapUser != null) {
                    User author = new User();
                    author.setId(String.valueOf(mapUser.getId()));
                    author.setUsername(mapUser.getLogin());
                    author.setAvatarUrl(mapUser.getAvatarUrl());
                    author.setWebUrl(mapUser.getHtmlUrl());
                    comment.setAuthor(author);
                }
                comments.add(comment);
            }
        }
        return comments;
    }
}
