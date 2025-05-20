package com.HotelManagement.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global exception handler to provide consistent error handling
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex, Model model, 
                                         RedirectAttributes redirectAttributes,
                                         HttpServletRequest request) {
        logger.error("Business exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        String message = ErrorCodes.getMessage(ex.getErrorCode());
        
        // Check if we're using redirect attributes
        if (redirectAttributes.getFlashAttributes().isEmpty()) {
            // For direct model rendering
            model.addAttribute("error", message);
            model.addAttribute("errorCode", ex.getErrorCode());
            return "Error";
        } else {
            // For redirects
            redirectAttributes.addFlashAttribute("error", message);
            redirectAttributes.addFlashAttribute("errorCode", ex.getErrorCode());
            return null; // Let the controller handle the redirect
        }
    }
    
    @ExceptionHandler(RuntimeException.class)
    public String handleRuntimeException(RuntimeException ex, Model model, 
                                        RedirectAttributes redirectAttributes) {
        logger.error("Runtime exception: ", ex);
        
        // Check if we're using redirect attributes
        if (redirectAttributes.getFlashAttributes().isEmpty()) {
            // For direct model rendering
            model.addAttribute("error", ex.getMessage());
            return "Error";
        } else {
            // For redirects
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return null; // Let the controller handle the redirect
        }
    }
    
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model, HttpServletRequest request) {
        logger.error("Unexpected exception: ", ex);
        
        String message = "An unexpected error occurred. Please try again later.";
        
        model.addAttribute("error", message);
        return "Error";
    }
} 