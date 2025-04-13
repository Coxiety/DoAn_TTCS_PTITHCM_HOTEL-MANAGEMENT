package com.HotelManagement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error details
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        
        // Log the error details
        logger.error("Error Status: {}", status);
        logger.error("Error Message: {}", message);
        if (exception != null) {
            logger.error("Exception: ", (Throwable) exception);
        }
        
        // Add error details to the model
        model.addAttribute("status", status);
        model.addAttribute("error", message != null ? message : "An unexpected error occurred");
        model.addAttribute("exception", exception);
        
        return "Error";
    }
} 