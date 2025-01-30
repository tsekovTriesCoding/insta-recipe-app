package app.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminViewController {

    @GetMapping
    public String adminDashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/users")
    public String adminUserManagement() {
        return "admin-user-management";
    }
}