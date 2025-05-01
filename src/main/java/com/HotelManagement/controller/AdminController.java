package com.HotelManagement.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.HotelManagement.models.Booking;
import com.HotelManagement.models.BookingDetail;
import com.HotelManagement.models.Payment;
import com.HotelManagement.models.Room;
import com.HotelManagement.models.RoomType;
import com.HotelManagement.models.User;
import com.HotelManagement.repository.BookingDetailRepository;
import com.HotelManagement.service.AdminService;
import com.HotelManagement.service.BookingService;
import com.HotelManagement.service.PaymentService;
import com.HotelManagement.service.RoomService;
import com.HotelManagement.service.UserService;
import com.HotelManagement.validation.RoomTypeValidationError;
import com.HotelManagement.validation.RoomTypeValidationException;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private BookingDetailRepository bookingDetailRepository;
    
    /**
     * Hash a password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    @GetMapping("")
    public String dashboard(Model model, HttpSession session) {
        // Check if user is logged in and has admin role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || !Integer.valueOf(1).equals(userRole)) {
            return "redirect:/";
        }
        
        // Get summary data for dashboard
        Map<String, Object> dashboardData = adminService.getDashboardSummary();
        model.addAttribute("dashboardData", dashboardData);
        
        return "admin/Dashboard";
    }
    
    // ==================== STAFF MANAGEMENT ====================
    
    @GetMapping("/staff")
    public String staffManagement(Model model, HttpSession session) {
        // Check if user is logged in and has admin role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || !Integer.valueOf(1).equals(userRole)) {
            return "redirect:/";
        }
        
        // Get all staff members (admins and receptionists)
        List<User> staffMembers = userService.getAllUsers().stream()
                .filter(u -> Integer.valueOf(1).equals(u.getRoleId()) || Integer.valueOf(2).equals(u.getRoleId())) // 1=admin, 2=receptionist
                .collect(Collectors.toList());
        
        model.addAttribute("staffMembers", staffMembers);
        // Pass the current user's ID to the template so we can disable role editing for self
        model.addAttribute("currentUserId", user.getId());
        
        return "admin/StaffManagement";
    }
    
    @GetMapping("/staff/{id}")
    @ResponseBody
    public User getStaffById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }
    
    @PostMapping("/staff/create")
    public String createStaff(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam Integer roleId,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate roleId (must be receptionist or admin)
            if (roleId != 1 && roleId != 2) {
                throw new RuntimeException("Invalid role ID. Must be 1 (admin) or 2 (receptionist)");
            }
            
            // Check if username already exists
            if (userService.getAllUsers().stream().anyMatch(u -> u.getUsername().equals(username))) {
                throw new RuntimeException("Username already exists");
            }
            
            // Check if email already exists
            if (email != null && !email.isEmpty() && 
                userService.getAllUsers().stream().anyMatch(u -> email.equals(u.getEmail()))) {
                throw new RuntimeException("Email already in use");
            }
            
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(hashPassword(password)); // Encode password
            newUser.setFullName(fullName);
            newUser.setPhone(phone);
            newUser.setEmail(email);
            newUser.setRoleId(roleId);
            
            userService.saveUser(newUser);
            redirectAttributes.addFlashAttribute("success", "Staff member created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create staff member: " + e.getMessage());
        }
        
        return "redirect:/admin/staff";
    }
    
    @PostMapping("/staff/update")
    public String updateStaff(
            @RequestParam Integer id,
            @RequestParam String username,
            @RequestParam(required = false) String password,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam Integer roleId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate roleId (must be receptionist or admin)
            if (roleId != 1 && roleId != 2) {
                throw new RuntimeException("Invalid role ID. Must be 1 (admin) or 2 (receptionist)");
            }
            
            User user = userService.getUserById(id);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            
            // Get current logged-in user
            User currentUser = (User) session.getAttribute("user");
            
            // Prevent admins from demoting themselves to non-admin roles
            if (currentUser.getId().equals(id) && user.getRoleId() == 1 && roleId != 1) {
                throw new RuntimeException("Cannot change your own admin role");
            }
            
            // Check if username already exists (but not for the current user)
            User existingUser = userService.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(username) && !u.getId().equals(id))
                .findFirst().orElse(null);
            
            if (existingUser != null) {
                throw new RuntimeException("Username already exists");
            }
            
            // Check if email already exists (but not for the current user)
            if (email != null && !email.isEmpty()) {
                existingUser = userService.getAllUsers().stream()
                    .filter(u -> email.equals(u.getEmail()) && !u.getId().equals(id))
                    .findFirst().orElse(null);
                    
                if (existingUser != null) {
                    throw new RuntimeException("Email already in use");
                }
            }
            
            // Check if trying to update the last admin
            if (!currentUser.getId().equals(id) && user.getRoleId() == 1 && roleId != 1) {
                long adminCount = userService.countUsersByRole(1);
                if (adminCount <= 1) {
                    throw new RuntimeException("Cannot change role of the last administrator");
                }
            }
            
            user.setUsername(username);
            if (password != null && !password.isEmpty()) {
                user.setPassword(hashPassword(password)); // Encode password
            }
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setEmail(email);
            // Only update role if not editing self or not changing from admin
            if (!currentUser.getId().equals(id) || (currentUser.getId().equals(id) && Integer.valueOf(1).equals(user.getRoleId()) && Integer.valueOf(1).equals(roleId))) {
                user.setRoleId(roleId);
            } else if (!Integer.valueOf(roleId).equals(user.getRoleId())) {
                // If trying to change own role from admin, show warning but keep admin role
                redirectAttributes.addFlashAttribute("warning", "You cannot change your own admin role. Your admin privileges have been maintained.");
            }
            
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "Staff member updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update staff member: " + e.getMessage());
        }
        
        return "redirect:/admin/staff";
    }
    
    @PostMapping("/staff/delete")
    public String deleteStaff(
            @RequestParam Integer id, 
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            
            // Get current logged-in user
            User currentUser = (User) session.getAttribute("user");
            
            // Prevent admins from deleting themselves
            if (currentUser.getId().equals(id)) {
                throw new RuntimeException("Administrators cannot delete their own accounts");
            }
            
            // Don't allow deletion if this is the last admin
            if (Integer.valueOf(1).equals(user.getRoleId())) {
                long adminCount = userService.countUsersByRole(1);
                if (adminCount <= 1) {
                    throw new RuntimeException("Cannot delete the last administrator account");
                }
            }
            
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Staff member deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete staff member: " + e.getMessage());
        }
        
        return "redirect:/admin/staff";
    }
    
    // ==================== USER MANAGEMENT ====================
    
    @GetMapping("/users")
    public String userManagement(Model model, HttpSession session) {
        // Check if user is logged in and has admin role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || !Integer.valueOf(1).equals(userRole)) {
            return "redirect:/";
        }
        
        // Get all users
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        
        // Add role options for dropdowns
        Map<Integer, String> roles = new HashMap<>();
        roles.put(0, "Customer");
        roles.put(1, "Administrator");
        roles.put(2, "Receptionist");
        model.addAttribute("roles", roles);
        
        // Pass the current user's ID to the template so we can disable editing for self
        model.addAttribute("currentUserId", user.getId());
        
        return "admin/UserManagement";
    }
    
    @GetMapping("/users/{id}")
    @ResponseBody
    public User getUserById(@PathVariable Integer id) {
        return userService.getUserById(id);
    }
    
    @PostMapping("/users/create")
    public String createUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam Integer roleId,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Check if username already exists
            if (userService.getAllUsers().stream().anyMatch(u -> u.getUsername().equals(username))) {
                throw new RuntimeException("Username already exists");
            }
            
            // Check if email already exists
            if (email != null && !email.isEmpty() && 
                userService.getAllUsers().stream().anyMatch(u -> email.equals(u.getEmail()))) {
                throw new RuntimeException("Email already in use");
            }
            
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(hashPassword(password)); // Encode password
            newUser.setFullName(fullName);
            newUser.setPhone(phone);
            newUser.setEmail(email);
            newUser.setRoleId(roleId);
            
            userService.saveUser(newUser);
            redirectAttributes.addFlashAttribute("success", "User created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create user: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/update")
    public String updateUser(
            @RequestParam Integer id,
            @RequestParam String username,
            @RequestParam(required = false) String password,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String email,
            @RequestParam Integer roleId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            
            // Get current logged-in user
            User currentUser = (User) session.getAttribute("user");
            
            // Prevent admins from demoting themselves
            if (currentUser.getId().equals(id) && user.getRoleId() == 1 && roleId != 1) {
                throw new RuntimeException("Cannot change your own admin role");
            }
            
            // Check if username already exists (but not for the current user)
            User existingUser = userService.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(username) && !u.getId().equals(id))
                .findFirst().orElse(null);
                
            if (existingUser != null) {
                throw new RuntimeException("Username already exists");
            }
            
            // Check if email already exists (but not for the current user)
            if (email != null && !email.isEmpty()) {
                existingUser = userService.getAllUsers().stream()
                    .filter(u -> email.equals(u.getEmail()) && !u.getId().equals(id))
                    .findFirst().orElse(null);
                    
                if (existingUser != null) {
                    throw new RuntimeException("Email already in use");
                }
            }
            
            // Check if trying to update the last admin
            if (!currentUser.getId().equals(id) && user.getRoleId() == 1 && roleId != 1) {
                long adminCount = userService.countUsersByRole(1);
                if (adminCount <= 1) {
                    throw new RuntimeException("Cannot change role of the last administrator");
                }
            }
            
            user.setUsername(username);
            if (password != null && !password.isEmpty()) {
                user.setPassword(hashPassword(password)); // Encode password
            }
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setEmail(email);
            
            // Only update role if not editing self or not changing from admin
            if (!currentUser.getId().equals(id) || (currentUser.getId().equals(id) && Integer.valueOf(1).equals(user.getRoleId()) && Integer.valueOf(1).equals(roleId))) {
                user.setRoleId(roleId);
            } else if (!Integer.valueOf(roleId).equals(user.getRoleId())) {
                // If trying to change own role from admin, show warning but keep admin role
                redirectAttributes.addFlashAttribute("warning", "You cannot change your own admin role. Your admin privileges have been maintained.");
            }
            
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update user: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/delete")
    public String deleteUser(
            @RequestParam Integer id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                throw new RuntimeException("User not found");
            }
            
            // Get current logged-in user
            User currentUser = (User) session.getAttribute("user");
            
            // Prevent admins from deleting themselves
            if (currentUser.getId().equals(id)) {
                throw new RuntimeException("Administrators cannot delete their own accounts");
            }
            
            // Don't allow deletion if this is the last admin
            if (Integer.valueOf(1).equals(user.getRoleId())) {
                long adminCount = userService.countUsersByRole(1);
                if (adminCount <= 1) {
                    throw new RuntimeException("Cannot delete the last administrator account");
                }
            }
            
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete user: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    // ==================== ROOM MANAGEMENT ====================
    
    @GetMapping("/rooms")
    public String roomManagement(Model model, HttpSession session) {
        // Check if user is logged in and has admin role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || !Integer.valueOf(1).equals(userRole)) {
            return "redirect:/";
        }
        
        // Get all rooms for the room management page
        List<Room> rooms = roomService.getAllRooms();
        model.addAttribute("rooms", rooms);
        
        // Get all room types for the dropdown
        List<RoomType> roomTypes = roomService.getAllRoomTypes();
        model.addAttribute("roomTypes", roomTypes);
        
        return "admin/RoomManagement";
    }
    
    @GetMapping("/rooms/{id}")
    @ResponseBody
    public Room getRoomById(@PathVariable Integer id) {
        return roomService.getRoomById(id);
    }
    
    @PostMapping("/rooms/create")
    public String createRoom(
            @RequestParam String roomNumber,
            @RequestParam Integer typeId,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Check if room number already exists
            if (roomService.getAllRooms().stream().anyMatch(r -> r.getRoomNumber().equals(roomNumber))) {
                throw new RuntimeException("Room number already exists");
            }
            
            Room newRoom = new Room();
            newRoom.setRoomNumber(roomNumber);
            
            RoomType roomType = roomService.getRoomTypeById(typeId);
            if (roomType == null) {
                throw new RuntimeException("Room type not found");
            }
            newRoom.setRoomType(roomType);
            
            newRoom.setStatus("AVAILABLE");
            
            roomService.saveRoom(newRoom);
            redirectAttributes.addFlashAttribute("success", "Room created successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create room: " + e.getMessage());
        }
        
        return "redirect:/admin/rooms";
    }
    
    @PostMapping("/rooms/update")
    public String updateRoom(
            @RequestParam Integer id,
            @RequestParam String roomNumber,
            @RequestParam Integer typeId,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        
        try {
            Room room = roomService.getRoomById(id);
            if (room == null) {
                throw new RuntimeException("Room not found");
            }
            
            // Check if room number already exists (but not for the current room)
            Room existingRoom = roomService.getAllRooms().stream()
                .filter(r -> r.getRoomNumber().equals(roomNumber) && !r.getId().equals(id))
                .findFirst().orElse(null);
                
            if (existingRoom != null) {
                throw new RuntimeException("Room number already exists");
            }
            
            room.setRoomNumber(roomNumber);
            
            RoomType roomType = roomService.getRoomTypeById(typeId);
            if (roomType == null) {
                throw new RuntimeException("Room type not found");
            }
            room.setRoomType(roomType);
            
            room.setStatus(status);
            
            roomService.saveRoom(room);
            redirectAttributes.addFlashAttribute("success", "Room updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update room: " + e.getMessage());
        }
        
        return "redirect:/admin/rooms";
    }
    
    @PostMapping("/rooms/delete")
    public String deleteRoom(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            // Check if room exists
            Room room = roomService.getRoomById(id);
            if (room == null) {
                throw new RuntimeException("Room not found");
            }
            
            // Simple check if room can be deleted
            // In a real app, check if there are related bookings
            
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("success", "Room deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete room: " + e.getMessage());
        }
        
        return "redirect:/admin/rooms";
    }
    
    // ==================== ROOM TYPE MANAGEMENT ====================
    
    @GetMapping("/roomtypes")
    public String roomTypeManagement(Model model, HttpSession session) {
        // Check if user is logged in and has admin role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || !Integer.valueOf(1).equals(userRole)) {
            return "redirect:/";
        }
        
        // Get all room types for the room type management page
        List<RoomType> roomTypes = roomService.getAllRoomTypes();
        model.addAttribute("roomTypes", roomTypes);
        
        return "admin/RoomTypeManagement";
    }
    
    @GetMapping("/roomtypes/{id}")
    @ResponseBody
    public RoomType getRoomTypeById(@PathVariable Integer id) {
        return roomService.getRoomTypeById(id);
    }
    
    @PostMapping("/roomtypes/create")
    public String createRoomType(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Integer capacity,
            @RequestParam BigDecimal price,
            @RequestParam String amenities,
            @RequestParam(required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate image format if provided
            if (image != null && !image.isEmpty()) {
                String contentType = image.getContentType();
                if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
                    throw new RoomTypeValidationException(RoomTypeValidationError.INVALID_IMAGE_FORMAT);
                }
            }
            
            RoomType newRoomType = new RoomType();
            newRoomType.setName(name);
            newRoomType.setDescription(description);
            newRoomType.setCapacity(capacity);
            newRoomType.setPrice(price);
            newRoomType.setAmenities(amenities);
            
            // Save room type first to get the ID
            roomService.saveRoomType(newRoomType);
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                String imagePath = adminService.saveRoomTypeImage(image, name, newRoomType.getId());
                newRoomType.setImagePath(imagePath);
                // Update room type with image path
                roomService.saveRoomType(newRoomType);
            }
            
            redirectAttributes.addFlashAttribute("success", "Room type created successfully!");
        } catch (RoomTypeValidationException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("errorCode", e.getError().name());
            // Add the invalid input back to the form
            redirectAttributes.addFlashAttribute("roomTypeForm", Map.of(
                "name", name,
                "description", description,
                "capacity", capacity,
                "price", price,
                "amenities", amenities
            ));
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
        }
        
        return "redirect:/admin/roomtypes";
    }
    
    @PostMapping("/roomtypes/update")
    public String updateRoomType(
            @RequestParam Integer id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Integer capacity,
            @RequestParam BigDecimal price,
            @RequestParam String amenities,
            @RequestParam(required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate price
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Price must be greater than zero");
            }
            
            RoomType roomType = roomService.getRoomTypeById(id);
            if (roomType == null) {
                throw new RuntimeException("Room type not found");
            }
            
            roomType.setName(name);
            roomType.setDescription(description);
            roomType.setCapacity(capacity);
            roomType.setPrice(price);
            roomType.setAmenities(amenities);
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                String imagePath = adminService.saveRoomTypeImage(image, name, id);
                roomType.setImagePath(imagePath);
            }
            
            roomService.saveRoomType(roomType);
            redirectAttributes.addFlashAttribute("success", "Room type updated successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update room type: " + e.getMessage());
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to upload image: " + e.getMessage());
        }
        
        return "redirect:/admin/roomtypes";
    }
    
    @PostMapping("/roomtypes/delete")
    public String deleteRoomType(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            // Check if room type exists
            RoomType roomType = roomService.getRoomTypeById(id);
            if (roomType == null) {
                throw new RuntimeException("Room type not found");
            }
            
            // Check if room type is in use
            boolean inUse = roomService.getAllRooms().stream()
                .anyMatch(r -> r.getRoomType().getId().equals(id));
                
            if (inUse) {
                throw new RuntimeException("Cannot delete room type that has rooms assigned to it");
            }
            
            roomService.deleteRoomType(id);
            redirectAttributes.addFlashAttribute("success", "Room type deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete room type: " + e.getMessage());
        }
        
        return "redirect:/admin/roomtypes";
    }
    
    // ==================== REVENUE REPORT ====================
    
    @GetMapping("/revenue")
    public String revenueReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model,
            HttpSession session) {
        
        // Check if user is logged in and has admin role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || !Integer.valueOf(1).equals(userRole)) {
            return "redirect:/";
        }
        
        // Set default date range if not provided
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusMonths(1);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        // Get revenue report data
        Map<String, Object> reportData = paymentService.getRevenueReportData(start, end);
        model.addAttribute("reportData", reportData);
        model.addAttribute("startDate", start.format(DateTimeFormatter.ISO_DATE));
        model.addAttribute("endDate", end.format(DateTimeFormatter.ISO_DATE));
        
        return "admin/RevenueReport";
    }
    
    // ==================== PASSWORD MANAGEMENT ====================
    
    /**
     * Reset all user passwords to "test"
     */
    @PostMapping("/reset-all-passwords")
    public String resetAllPasswords(HttpSession session, RedirectAttributes redirectAttributes) {
        // Check if user is logged in and has admin role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || !Integer.valueOf(1).equals(userRole)) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/";
        }
        
        try {
            // The password to set for all users
            String newPassword = "test";
            String hashedPassword = hashPassword(newPassword);
            
            // Get all users and update their passwords
            List<User> users = userService.getAllUsers();
            int count = 0;
            
            for (User userToUpdate : users) {
                userToUpdate.setPassword(hashedPassword);
                userService.saveUser(userToUpdate);
                count++;
            }
            
            redirectAttributes.addFlashAttribute("success", "Successfully reset " + count + " user passwords to 'test'");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Failed to reset passwords: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }

    /**
     * Display invoice for a specific booking (for admin use)
     */
    @GetMapping("/invoice/{bookingId}")
    public String showInvoice(@PathVariable Integer bookingId, 
                             Model model, 
                             HttpSession session) {
        // Check if user is logged in and has admin role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || userRole != 1) {
            return "redirect:/";
        }
        
        try {
            // Get the booking details
            Booking booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                model.addAttribute("error", "Booking not found");
                return "redirect:/admin/revenue";
            }
            
            // Get all booking details (rooms)
            List<BookingDetail> bookingDetails = bookingDetailRepository.findByBookingId(bookingId);
            
            // Get payment info
            List<Payment> payments = paymentService.getPaymentsForBooking(bookingId);
            String paymentMethod = !payments.isEmpty() ? payments.get(0).getPaymentMethod() : "N/A";
            
            // Calculate nights stayed
            long nightsStayed = java.time.temporal.ChronoUnit.DAYS
                .between(booking.getCheckInDate().toLocalDate(), booking.getCheckOutDate().toLocalDate());
            
            // Ensure at least 1 night is charged
            if (nightsStayed < 1) {
                nightsStayed = 1;
            }
            
            // Add to model
            model.addAttribute("booking", booking);
            model.addAttribute("bookingDetails", bookingDetails);
            model.addAttribute("paymentMethod", paymentMethod);
            model.addAttribute("nightsStayed", nightsStayed);
            model.addAttribute("checkoutDate", booking.getCheckOutDate().toLocalDate());
            model.addAttribute("isAdminView", true);
            
            // Format total
            model.addAttribute("formattedTotal", String.format("%.2f", booking.getTotalAmount()));
            
            return "invoice/InvoicePage";
        } catch (Exception e) {
            model.addAttribute("error", "Error generating invoice: " + e.getMessage());
            return "redirect:/admin/revenue";
        }
    }
}