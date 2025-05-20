package com.HotelManagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.HotelManagement.exception.BusinessException;
import com.HotelManagement.exception.SuccessCodes;
import com.HotelManagement.models.User;
import com.HotelManagement.service.AuthService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/auth")
public class AuthController 
{
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public String login(
        @RequestParam String username, 
        @RequestParam String password,
        HttpSession session,
        RedirectAttributes redirectAttributes) 
    {
        try 
        {
            User user = authService.login(username, password);
            if (user != null) 
            {
                // Store user in session
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getId());
                session.setAttribute("userRole", user.getRoleId());
                
                // Redirect based on user role
                return switch (user.getRoleId()) {
                    case 1 -> "redirect:/admin"; // Admin role
                    case 2 -> "redirect:/receptionist"; // Receptionist role
                    default -> "redirect:/"; // Customer (0) or other roles
                };
            } 
            else 
            {
                redirectAttributes.addFlashAttribute("error", "Invalid username or password");
                return "redirect:/"; // Redirect back with error
            }
        } 
        catch (BusinessException e) 
        {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("errorCode", e.getErrorCode());
            return "redirect:/";
        }
        catch (Exception e) 
        {
            redirectAttributes.addFlashAttribute("error", "Login failed: " + e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/signup")
    public String signup(
        @RequestParam String username,
        @RequestParam String password,
        @RequestParam String email,
        @RequestParam String fullName,
        @RequestParam String phone,
        HttpSession session,
        RedirectAttributes redirectAttributes) 
    {
        try 
        {
            User newUser = authService.signup(username, password, email, fullName, phone);
            
            // Automatically log in the new user
            session.setAttribute("user", newUser);
            session.setAttribute("userId", newUser.getId());
            session.setAttribute("userRole", newUser.getRoleId());
            
            redirectAttributes.addFlashAttribute("success", SuccessCodes.getMessage(SuccessCodes.USER_CREATED));
            redirectAttributes.addFlashAttribute("successCode", SuccessCodes.USER_CREATED);
            return "redirect:/";
        } 
        catch (BusinessException e) 
        {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("errorCode", e.getErrorCode());
            return "redirect:/";
        }
        catch (Exception e) 
        {
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) 
    {
        session.invalidate();
        return "redirect:/";
    }
}