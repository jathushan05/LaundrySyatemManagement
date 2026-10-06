package com.laundry.service;
import com.laundry.model.Role;
import com.laundry.model.User;
import java.util.List;

public class UserManagementPolicyCheck {
    static User user(long id,Role role,String status) {
        User u=new User(); u.setId(id); u.setRole(role); u.setStatus(status);
        u.setFullName("Test User"); u.setEmail("test@example.com"); return u;
    }
    static void denied(Runnable work) {
        try { work.run(); } catch (IllegalArgumentException | SecurityException expected) { return; }
        throw new AssertionError("Unsafe change was accepted");
    }
    public static void main(String[] args) {
        User admin=user(1,Role.ADMINISTRATOR,"ACTIVE"), staff=user(2,Role.DELIVERY_COORDINATOR,"ACTIVE");
        List<User> users=List.of(admin,staff);
        UserManagementPolicy.authorize(users,1,staff,false);
        denied(() -> UserManagementPolicy.authorize(users,2,admin,false));
        denied(() -> UserManagementPolicy.authorize(users,1,user(1,Role.ADMINISTRATOR,"INACTIVE"),false));
        denied(() -> UserManagementPolicy.authorize(users,1,user(1,Role.CUSTOMER,"ACTIVE"),false));
        denied(() -> UserManagementPolicy.authorize(users,1,user(1,Role.RECEPTIONIST,"ACTIVE"),false));
        denied(() -> UserManagementPolicy.authorize(users,1,user(99,Role.RECEPTIONIST,"ACTIVE"),true));
        denied(() -> UserManagementPolicy.authorize(users,1,user(2,Role.CUSTOMER,"ACTIVE"),false));
        UserManagementPolicy.authorize(List.of(admin,user(3,Role.ADMINISTRATOR,"ACTIVE")),1,user(1,Role.RECEPTIONIST,"ACTIVE"),false);
        staff.setCustomerId(4L);
        UserManagementPolicy.authorize(users,1,user(2,Role.CUSTOMER,"ACTIVE"),false);
        UserManagementPolicy.authorize(users,1,admin,true);
        UserManagementPolicy.validate(staff);
        staff.setEmail("bad-email"); denied(() -> UserManagementPolicy.validate(staff));
        UserManagementPolicy.password("ValidPassword9!","ValidPassword9!");
        denied(() -> UserManagementPolicy.password("short","short"));
        denied(() -> UserManagementPolicy.password("ValidPassword9!","mismatch"));
        denied(() -> UserManagementPolicy.password("界".repeat(25),"界".repeat(25)));
        System.out.println("User-management policy checks passed.");
    }
}
