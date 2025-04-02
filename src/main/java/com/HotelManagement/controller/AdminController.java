package com.HotelManagement.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

import com.HotelManagement.dto.RevenueReportDto;
import com.HotelManagement.models.Room;
import com.HotelManagement.models.RoomType;
import com.HotelManagement.models.User;
import com.HotelManagement.service.AdminService;
import com.HotelManagement.service.RoomService;
import com.HotelManagement.service.UserService;

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
    
    @GetMapping("")
    public String dashboard(Model model, HttpSession session) {
        // Check if user is logged in and has admin role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || userRole != 3) {
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
        
        if (user == null || userRole == null || userRole != 3) {
            return "redirect:/";
        }
        
        // Get all staff members (admins and receptionists)
        List<User> staffMembers = userService.getAllUsers().stream()
                .filter(u -> u.getRoleId() == 1 || u.getRoleId() == 3) // 1=receptionist, 3=admin
                .collect(Collectors.toList());
        
        model.addAttribute("staffMembers", staffMembers);
        
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
            // Validate the role ID (only allow receptionist=1 or admin=3)
            if (roleId != 1 && roleId != 3) {
                throw new RuntimeException("Invalid role selected");
            }
            
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);  // Should be encrypted in production
            newUser.setFullName(fullName);
            newUser.setPhone(phone);
            newUser.setEmail(email);
            newUser.setRoleId(roleId);
            
            userService.saveUser(newUser);
            redirectAttributes.addFlashAttribute("success", "Staff member created successfully!");
        } catch (Exception e) {
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
            RedirectAttributes redirectAttributes) {
        
        try {
            // Validate the role ID (only allow receptionist=1 or admin=3)
            if (roleId != 1 && roleId != 3) {
                throw new RuntimeException("Invalid role selected");
            }
            
            User user = userService.getUserById(id);
            
            // Check if trying to update the last admin
            if (user.getRoleId() == 3 && roleId != 3) {
                long adminCount = userService.countUsersByRole(3);
                if (adminCount <= 1) {
                    throw new RuntimeException("Cannot change role of the last administrator");
                }
            }
            
            user.setUsername(username);
            if (password != null && !password.isEmpty()) {
                user.setPassword(password);  // Should be encrypted in production
            }
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setEmail(email);
            user.setRoleId(roleId);
            
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "Staff member updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update staff member: " + e.getMessage());
        }
        
        return "redirect:/admin/staff";
    }
    
    @PostMapping("/staff/delete")
    public String deleteStaff(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            
            // Don't allow deletion if this is the last admin
            if (user.getRoleId() == 3) {
                long adminCount = userService.countUsersByRole(3);
                if (adminCount <= 1) {
                    throw new RuntimeException("Cannot delete the last administrator account");
                }
            }
            
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Staff member deleted successfully!");
        } catch (Exception e) {
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
        
        if (user == null || userRole == null || userRole != 3) {
            return "redirect:/";
        }
        
        // Get all users for the user management page
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        
        // For the role dropdown in the create/edit form
        Map<Integer, String> roles = new HashMap<>();
        roles.put(1, "Receptionist");
        roles.put(2, "Customer");
        roles.put(3, "Administrator");
        model.addAttribute("roles", roles);
        
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
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password);  // Should be encrypted in production
            newUser.setFullName(fullName);
            newUser.setPhone(phone);
            newUser.setEmail(email);
            newUser.setRoleId(roleId);
            
            userService.saveUser(newUser);
            redirectAttributes.addFlashAttribute("success", "User created successfully!");
        } catch (Exception e) {
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
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = userService.getUserById(id);
            user.setUsername(username);
            if (password != null && !password.isEmpty()) {
                user.setPassword(password);  // Should be encrypted in production
            }
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setEmail(email);
            user.setRoleId(roleId);
            
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "User updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update user: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
    
    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully!");
        } catch (Exception e) {
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
        
        if (user == null || userRole == null || userRole != 3) {
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
            @RequestParam BigDecimal price,
            @RequestParam(required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        
        try {
            Room newRoom = new Room();
            newRoom.setRoomNumber(roomNumber);
            
            RoomType roomType = roomService.getRoomTypeById(typeId);
            newRoom.setRoomType(roomType);
            
            newRoom.setPrice(price);
            newRoom.setStatus("AVAILABLE");
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                String imagePath = adminService.saveRoomImage(image);
                newRoom.setImagePath(imagePath);
            }
            
            roomService.saveRoom(newRoom);
            redirectAttributes.addFlashAttribute("success", "Room created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create room: " + e.getMessage());
        }
        
        return "redirect:/admin/rooms";
    }
    
    @PostMapping("/rooms/update")
    public String updateRoom(
            @RequestParam Integer id,
            @RequestParam String roomNumber,
            @RequestParam Integer typeId,
            @RequestParam BigDecimal price,
            @RequestParam String status,
            @RequestParam(required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        
        try {
            Room room = roomService.getRoomById(id);
            room.setRoomNumber(roomNumber);
            
            RoomType roomType = roomService.getRoomTypeById(typeId);
            room.setRoomType(roomType);
            
            room.setPrice(price);
            room.setStatus(status);
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                String imagePath = adminService.saveRoomImage(image);
                room.setImagePath(imagePath);
            }
            
            roomService.saveRoom(room);
            redirectAttributes.addFlashAttribute("success", "Room updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update room: " + e.getMessage());
        }
        
        return "redirect:/admin/rooms";
    }
    
    @PostMapping("/rooms/delete")
    public String deleteRoom(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("success", "Room deleted successfully!");
        } catch (Exception e) {
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
        
        if (user == null || userRole == null || userRole != 3) {
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
            @RequestParam String amenities,
            @RequestParam(required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        
        try {
            RoomType newRoomType = new RoomType();
            newRoomType.setName(name);
            newRoomType.setDescription(description);
            newRoomType.setCapacity(capacity);
            newRoomType.setAmenities(amenities);
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                String imagePath = adminService.saveRoomTypeImage(image);
                newRoomType.setImagePath(imagePath);
            }
            
            roomService.saveRoomType(newRoomType);
            redirectAttributes.addFlashAttribute("success", "Room type created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create room type: " + e.getMessage());
        }
        
        return "redirect:/admin/roomtypes";
    }
    
    @PostMapping("/roomtypes/update")
    public String updateRoomType(
            @RequestParam Integer id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam Integer capacity,
            @RequestParam String amenities,
            @RequestParam(required = false) MultipartFile image,
            RedirectAttributes redirectAttributes) {
        
        try {
            RoomType roomType = roomService.getRoomTypeById(id);
            roomType.setName(name);
            roomType.setDescription(description);
            roomType.setCapacity(capacity);
            roomType.setAmenities(amenities);
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                String imagePath = adminService.saveRoomTypeImage(image);
                roomType.setImagePath(imagePath);
            }
            
            roomService.saveRoomType(roomType);
            redirectAttributes.addFlashAttribute("success", "Room type updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update room type: " + e.getMessage());
        }
        
        return "redirect:/admin/roomtypes";
    }
    
    @PostMapping("/roomtypes/delete")
    public String deleteRoomType(@RequestParam Integer id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoomType(id);
            redirectAttributes.addFlashAttribute("success", "Room type deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete room type: " + e.getMessage());
        }
        
        return "redirect:/admin/roomtypes";
    }
    
    // ==================== REVENUE REPORTS ====================
    
    @GetMapping("/revenue")
    public String revenueReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model, 
            HttpSession session) {
        
        // Check if user is logged in and has admin role
        User user = (User) session.getAttribute("user");
        Integer userRole = (Integer) session.getAttribute("userRole");
        
        if (user == null || userRole == null || userRole != 3) {
            return "redirect:/";
        }
        
        // Set default date range if not provided
        LocalDate start = startDate == null ? 
                LocalDate.now().minusMonths(1) : 
                LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        
        LocalDate end = endDate == null ? 
                LocalDate.now() : 
                LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
        
        // Get revenue data
        RevenueReportDto revenueReport = adminService.getRevenueReport(start, end);
        model.addAttribute("report", revenueReport);
        model.addAttribute("startDate", start.toString());
        model.addAttribute("endDate", end.toString());
        
        return "admin/RevenueReport";
    }
}