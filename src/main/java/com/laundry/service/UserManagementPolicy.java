package com.laundry.service;

import com.laundry.model.Role;
import com.laundry.model.User;
import java.util.List;
import java.nio.charset.StandardCharsets;

public final class UserManagementPolicy {
    private UserManagementPolicy() { }
    public static void validate(User u) {
        if (u.getFullName() == null || u.getFullName().isBlank() || u.getFullName().length()>120)
            throw new IllegalArgumentException("Enter a name of 1–120 characters.");
        if (u.getEmail() == null || u.getEmail().length()>150 || !u.getEmail().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            throw new IllegalArgumentException("Enter a valid email address.");
        if (u.getRole()==null || !List.of("ACTIVE","INACTIVE","LOCKED").contains(u.getStatus()))
            throw new IllegalArgumentException("Select a valid role and account status.");
    }
    public static void password(String password, String confirmation) {
        if (password==null || password.length()<8 || password.getBytes(StandardCharsets.UTF_8).length>72)
            throw new IllegalArgumentException("Use at least 8 characters and at most 72 UTF-8 bytes for the password.");
        if (!password.equals(confirmation)) throw new IllegalArgumentException("Passwords do not match.");
    }
    public static void authorizeDelete(List<User> users, long actorId, long targetId) {
        User actor = users.stream().filter(u -> u.getId()==actorId).findFirst().orElseThrow(() -> new SecurityException("Administrator access required."));
        if (actor.getRole()!=Role.ADMINISTRATOR || !"ACTIVE".equals(actor.getStatus())) throw new SecurityException("Administrator access required.");
        User target = users.stream().filter(u -> u.getId()==targetId).findFirst().orElseThrow(() -> new IllegalArgumentException("User no longer exists."));
        if (actorId==targetId) throw new IllegalArgumentException("You cannot delete your own account.");
        if (!"INACTIVE".equals(target.getStatus())) throw new IllegalArgumentException("Only inactive accounts can be permanently deleted. Deactivate the account first.");
    }
    public static void authorize(List<User> users, long actorId, User edit, boolean passwordOnly) {
        User actor = users.stream().filter(u -> u.getId()==actorId).findFirst().orElseThrow(() -> new SecurityException("Administrator access required."));
        if (actor.getRole()!=Role.ADMINISTRATOR || !"ACTIVE".equals(actor.getStatus())) throw new SecurityException("Administrator access required.");
        User old = edit.getId()==null ? null : users.stream().filter(u -> u.getId().equals(edit.getId())).findFirst().orElseThrow(() -> new IllegalArgumentException("User no longer exists."));
        if (passwordOnly) return;
        if (edit.getRole()==Role.CUSTOMER && (old==null || old.getCustomerId()==null))
            throw new IllegalArgumentException("Create customer accounts in Customers so their contact and delivery details are included.");
        if (old!=null && old.getRole()==Role.ADMINISTRATOR && "ACTIVE".equals(old.getStatus())
                && (edit.getRole()!=Role.ADMINISTRATOR || !"ACTIVE".equals(edit.getStatus()))
                && users.stream().filter(u -> u.getRole()==Role.ADMINISTRATOR && "ACTIVE".equals(u.getStatus())).count()<=1)
            throw new IllegalArgumentException("Keep at least one active administrator.");
    }
}
