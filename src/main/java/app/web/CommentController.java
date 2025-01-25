package app.web;

import app.comment.service.CommentService;
import app.web.dto.AddComment;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add/{id}")
    public String addComment(@Valid AddComment addComment,
                             @PathVariable UUID id,
                             @AuthenticationPrincipal UserDetails userDetails) {
        commentService.add(addComment, id, userDetails.getUsername());

        return "redirect:/recipes/" + id;
    }
}
