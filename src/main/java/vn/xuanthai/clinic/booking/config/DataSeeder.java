package vn.xuanthai.clinic.booking.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.xuanthai.clinic.booking.entity.Permission;
import vn.xuanthai.clinic.booking.entity.Role;
import vn.xuanthai.clinic.booking.entity.User;
import vn.xuanthai.clinic.booking.enums.Gender;
import vn.xuanthai.clinic.booking.repository.PermissionRepository;
import vn.xuanthai.clinic.booking.repository.RoleRepository;
import vn.xuanthai.clinic.booking.repository.UserRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // === 1. TẠO CÁC QUYỀN (PERMISSIONS) ===

        // Quyền quản lý người dùng (User)
        Permission userView = createPermissionIfNotFound("USER_VIEW");
        Permission userCreate = createPermissionIfNotFound("USER_CREATE");
        Permission userEdit = createPermissionIfNotFound("USER_EDIT");
        Permission userDelete = createPermissionIfNotFound("USER_DELETE"); // Nguy hiểm, thường chỉ Super Admin
        Permission userManageRoles = createPermissionIfNotFound("USER_MANAGE_ROLES"); // Quyền lực nhất

        // Quyền quản lý chuyên khoa (Specialty)
        Permission specialtyCreate = createPermissionIfNotFound("SPECIALTY_CREATE");
        Permission specialtyUpdate = createPermissionIfNotFound("SPECIALTY_UPDATE");
        Permission specialtyDelete = createPermissionIfNotFound("SPECIALTY_DELETE");

        // Quyền quản lý phòng khám (Clinic)
        Permission clinicCreate = createPermissionIfNotFound("CLINIC_CREATE");
        Permission clinicUpdate = createPermissionIfNotFound("CLINIC_UPDATE");
        Permission clinicDelete = createPermissionIfNotFound("CLINIC_DELETE");

        // Quyền quản lý bác sĩ (Doctor)
        Permission doctorCreate = createPermissionIfNotFound("DOCTOR_CREATE");
        Permission doctorUpdate = createPermissionIfNotFound("DOCTOR_UPDATE");
        Permission doctorDelete = createPermissionIfNotFound("DOCTOR_DELETE");
        Permission doctorUpdateSelf = createPermissionIfNotFound("DOCTOR_UPDATE_SELF");
        Permission doctorManageSchedule = createPermissionIfNotFound("DOCTOR_MANAGE_SCHEDULE"); // Bác sĩ tự xếp lịch

        // Quyền quản lý lịch hẹn (Appointment)
        Permission appointmentView = createPermissionIfNotFound("APPOINTMENT_VIEW"); // Admin xem danh sách
        Permission appointmentApprove = createPermissionIfNotFound("APPOINTMENT_APPROVE");
        Permission appointmentCancel = createPermissionIfNotFound("APPOINTMENT_CANCEL");

        // Quyền hệ thống khác
        Permission fileUpload = createPermissionIfNotFound("FILE_UPLOAD");


        // === 2. TẠO CÁC VAI TRÒ (ROLES) VÀ GÁN QUYỀN ===

        // Role PATIENT: Quyền hạn rất hạn chế (thường được xử lý riêng trong logic code)
        createRoleIfNotFound("ROLE_PATIENT", Set.of(
                // Có thể thêm các quyền như "APPOINTMENT_CREATE_OWN" nếu muốn quản lý chặt chẽ
        ));

        // Role DOCTOR: Bác sĩ cần quyền quản lý lịch của mình
        createRoleIfNotFound("ROLE_DOCTOR", Set.of(
                doctorManageSchedule,
                doctorUpdateSelf
        ));

        // Role ADMIN: Quản lý vận hành (nhưng không quản lý User cấp cao)
        createRoleIfNotFound("ROLE_ADMIN", Set.of(
                // User
//                userView, userCreate, userEdit,
                // Không có userDelete và userManageRoles

                // Specialty
                specialtyCreate, specialtyUpdate, specialtyDelete,

                // Clinic
                clinicCreate, clinicUpdate, clinicDelete,

                // Doctor
                doctorCreate, doctorUpdate, doctorDelete, doctorManageSchedule,

                // Appointment
                appointmentView, appointmentApprove, appointmentCancel,

                // System
                fileUpload
        ));

        // Role SUPER_ADMIN: Quyền lực tuyệt đối (Lấy tất cả permission hiện có trong DB)
        // Lưu ý: Phải dùng new HashSet để tránh lỗi immutable list nếu findAll trả về list không đổi
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

            // Set thông tin cá nhân
            superAdmin.setGender(Gender.MALE);
            superAdmin.setBirthday(LocalDate.of(2004, 11, 18));

            superAdmin.setActive(true);
            superAdmin.setRoles(Set.of(superAdminRole));

            userRepository.save(superAdmin);
            System.out.println("======> Created SUPER_ADMIN account: superadmin@clinic.com / Xuanthai1811@");
        }
    }

    private Permission createPermissionIfNotFound(String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(new Permission(name)));
    }

    private Role createRoleIfNotFound(String name, Set<Permission> permissions) {
        return roleRepository.findByName(name)
                .map(role -> {
                    // Nếu role đã tồn tại, CẬP NHẬT lại quyền cho nó (để đảm bảo luôn đúng với code mới nhất)
                    // Đây là logic quan trọng khi em thêm quyền mới vào code
                    role.setPermissions(permissions);
                    return roleRepository.save(role);
                })
                .orElseGet(() -> {
                    // Nếu chưa tồn tại, tạo mới
                    Role role = new Role(name);
                    role.setPermissions(permissions);
                    return roleRepository.save(role);
                });
    }
}