package com.overflow.user;

import com.overflow.user.dto.CreatePartnerRequest;
import com.overflow.user.dto.CustomerRegistrationRequest;
import com.overflow.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest {

    @Test
    void publicRegistrationCreatesCustomerAndHashesPassword() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        MemoryUsers records = new MemoryUsers();
        UserService service = new UserService(records.repository(), encoder);

        UserResponse response = service.registerCustomer(new CustomerRegistrationRequest(
                "Asha Customer", "ASHA@example.test", "private-password", "555-0100"));

        assertEquals(UserRole.CUSTOMER, response.role());
        assertEquals("asha@example.test", response.email());
        assertTrue(encoder.matches("private-password", records.byEmail("asha@example.test").getPassword()));
    }

    @Test
    void adminCanCreatePartnerAccountWithPartnerRole() {
        MemoryUsers records = new MemoryUsers();
        UserService service = new UserService(records.repository(), new BCryptPasswordEncoder());

        UserResponse response = service.createPartner(new CreatePartnerRequest(
                "Delivery Partner", "partner@example.test", "private-password", null));

        assertEquals(UserRole.PARTNER, response.role());
        assertEquals("partner@example.test", response.email());
    }

    @Test
    void bootstrapCreatesAdminOnceAndDoesNotReplaceExistingPassword() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        MemoryUsers records = new MemoryUsers();
        UserService service = new UserService(records.repository(), encoder);

        assertTrue(service.createBootstrapAdmin("Overflow Admin", "admin@example.test", "first-private-password"));
        String storedHash = records.byEmail("admin@example.test").getPassword();
        assertEquals(UserRole.ADMIN, records.byEmail("admin@example.test").getRole());
        assertTrue(encoder.matches("first-private-password", storedHash));

        assertFalse(service.createBootstrapAdmin("Other Name", "ADMIN@example.test", "second-private-password"));

        assertEquals(storedHash, records.byEmail("admin@example.test").getPassword());
        assertTrue(encoder.matches("first-private-password", storedHash));
    }

    private static final class MemoryUsers implements InvocationHandler {
        private final Map<String, AppUser> users = new HashMap<>();
        private long nextId = 1;

        AppUserRepository repository() {
            return (AppUserRepository) Proxy.newProxyInstance(AppUserRepository.class.getClassLoader(),
                    new Class<?>[]{AppUserRepository.class}, this);
        }

        AppUser byEmail(String email) {
            return users.get(normalize(email));
        }

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] arguments) throws Throwable {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "toString" -> "in-memory AppUserRepository";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == arguments[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                };
            }
            return switch (method.getName()) {
                case "existsByEmailIgnoreCase" -> users.containsKey(normalize((String) arguments[0]));
                case "findByEmailIgnoreCase" -> Optional.ofNullable(users.get(normalize((String) arguments[0])));
                case "findAllByRoleAndActiveTrueOrderByNameAsc" -> users.values().stream()
                        .filter(user -> user.getRole() == arguments[0] && user.isActive())
                        .sorted((left, right) -> left.getName().compareToIgnoreCase(right.getName()))
                        .toList();
                case "findById" -> users.values().stream()
                        .filter(user -> user.getId().equals(arguments[0])).findFirst();
                case "save" -> save((AppUser) arguments[0]);
                default -> throw new UnsupportedOperationException(method.getName());
            };
        }

        private AppUser save(AppUser user) throws Exception {
            if (user.getId() == null) {
                Field id = AppUser.class.getDeclaredField("id");
                id.setAccessible(true);
                id.set(user, nextId++);
            }
            users.put(normalize(user.getEmail()), user);
            return user;
        }

        private static String normalize(String email) {
            return email.toLowerCase(Locale.ROOT);
        }
    }
}
