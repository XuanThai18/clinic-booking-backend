package vn.xuanthai.clinic.booking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.xuanthai.clinic.booking.entity.Permission;
import vn.xuanthai.clinic.booking.entity.Role;
import vn.xuanthai.clinic.booking.entity.User;
import vn.xuanthai.clinic.booking.repository.PermissionRepository;
import vn.xuanthai.clinic.booking.repository.RoleRepository;
import vn.xuanthai.clinic.booking.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository; // THÊM VÀO
    private final PasswordEncoder passwordEncoder; // THÊM VÀO

    @Override
    public void run(String... args) throws Exception {
        // === 1. TẠO CÁC QUYỀN (PERMISSIONS) ===
        // Quyền quản lý người dùng
        Permission userView = createPermissionIfNotFound("USER_VIEW");
        Permission userCreate = createPermissionIfNotFound("USER_CREATE");
        Permission userEdit = createPermissionIfNotFound("USER_EDIT");
        Permission userDelete = createPermissionIfNotFound("USER_DELETE");
        Permission userManageRoles = createPermissionIfNotFound("USER_MANAGE_ROLES"); // Quyền lực nhất

        // Quyền quản lý nghiệp vụ
        Permission doctorManageSchedule = createPermissionIfNotFound("DOCTOR_MANAGE_SCHEDULE");
        Permission appointmentApprove = createPermissionIfNotFound("APPOINTMENT_APPROVE");
        Permission appointmentCancel = createPermissionIfNotFound("APPOINTMENT_CANCEL");


        // === 2. TẠO CÁC VAI TRÒ (ROLES) VÀ GÁN QUYỀN ===

        // Role PATIENT chỉ có quyền cơ bản
        createRoleIfNotFound("ROLE_PATIENT", Set.of(
                // Sau này có thể thêm các quyền như APPOINTMENT_CREATE_OWN
        ));

        // Role DOCTOR có quyền quản lý lịch của mình
        createRoleIfNotFound("ROLE_DOCTOR", Set.of(
                doctorManageSchedule
        ));

        // Role ADMIN có quyền quản lý nghiệp vụ, nhưng không có quyền tạo Admin khác
        createRoleIfNotFound("ROLE_ADMIN", Set.of(
                userView,
                userCreate,
                userEdit,
                appointmentApprove,
                appointmentCancel
        ));

        // Role SUPER_ADMIN có tất cả các quyền
        Role superAdminRole = createRoleIfNotFound("ROLE_SUPER_ADMIN", new HashSet<>(permissionRepository.findAll()));


        // === 3. TẠO TÀI KHOẢN SUPER_ADMIN NẾU CHƯA TỒN TẠI ===
        createSuperAdminIfNotFound(superAdminRole);
    }

    // --- CÁC PHƯƠNG THỨC TRỢ GIÚP ---

    private void createSuperAdminIfNotFound(Role superAdminRole) {
        // Kiểm tra xem đã có user nào có email của super admin chưa
        if (userRepository.findByEmail("superadmin@clinic.com").isEmpty()) {
            User superAdmin = new User();
            superAdmin.setFullName("Super Admin");
            superAdmin.setEmail("superadmin@clinic.com");
            // Băm mật khẩu trước khi lưu
            superAdmin.setPassword(passwordEncoder.encode("Xuanthai1811@"));
            superAdmin.setAddress("17A Cong Hoa, Tan Binh, HCM");
            superAdmin.setPhoneNumber("0397720010");
            superAdmin.setActive(true);
            superAdmin.setRoles(Set.of(superAdminRole));

            userRepository.save(superAdmin);
            System.out.println("======> Created SUPER_ADMIN account with default password 'Xuanthai1811@'");
        }
    }

    private Permission createPermissionIfNotFound(String name) {
        // Kiểm tra permission đã tồn tại chưa
        return permissionRepository.findByName(name)
                // Nếu chưa, tạo mới và lưu lại
                .orElseGet(() -> permissionRepository.save(new Permission(name)));
    }

    private Role createRoleIfNotFound(String name, Set<Permission> permissions) {
        // Kiểm tra role đã tồn tại chưa
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role(name);
                    role.setPermissions(permissions);
                    return roleRepository.save(role);
                });
    }
}