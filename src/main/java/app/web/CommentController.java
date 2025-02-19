package app.web;

import app.comment.service.CommentService;
import app.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add/{id}")
    public String addComment(@PathVariable UUID id,
                             @RequestParam("content") String content,
                             @AuthenticationPrincipal CustomUserDetails customUserDetails,
                             RedirectAttributes redirectAttributes) {

        if (content == null || content.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please enter a comment");

            return "redirect:/recipes/" + id;
        }

        commentService.add(content, id, customUserDetails.getId());

        return "redirect:/recipes/" + id;
    }
}
