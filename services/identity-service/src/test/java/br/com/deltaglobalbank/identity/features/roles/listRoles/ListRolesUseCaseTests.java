package br.com.deltaglobalbank.identity.features.roles.listRoles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.deltaglobalbank.identity.domain.role.Role;
import br.com.deltaglobalbank.identity.domain.role.RoleCode;
import br.com.deltaglobalbank.identity.domain.role.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListRolesUseCaseTests {

    private final RoleRepository roleRepository = mock(RoleRepository.class);

    private ListRolesUseCase useCase;

    private Role role(String code, boolean active) {
        return role(code, active, null);
    }

    private Role role(String code, boolean active, String label) {
        return new Role(UUID.randomUUID(), new RoleCode(code), null, null, label, active, Instant.now());
    }

    @BeforeEach
    void setUp() {
        useCase = new ListRolesUseCase(roleRepository);
    }

    @Test
    void activeTrueFiltersOutInactiveRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(
            role("customers.admin", true),
            role("special.custom", false)
        ));

        ListRolesResponse response = useCase.execute(new ListRolesQuery(true, true));

        assertTrue(response.items().stream().anyMatch(it -> it.code().equals("customers.admin")));
        assertFalse(response.items().stream().anyMatch(it -> it.code().equals("special.custom")));
    }

    @Test
    void activeOnlyFalseListsBothActiveAndInactiveRoles() {
        when(roleRepository.findAll()).thenReturn(List.of(
            role("customers.admin", true),
            role("special.custom", false)
        ));

        ListRolesResponse response = useCase.execute(new ListRolesQuery(true, false));

        assertTrue(response.items().stream().anyMatch(it -> it.code().equals("customers.admin") && it.active()));
        assertTrue(response.items().stream().anyMatch(it -> it.code().equals("special.custom") && !it.active()));
    }

    @Test
    void labelFallsBackToDescriptionThenToCodeWhenNull() {
        when(roleRepository.findAll()).thenReturn(List.of(
            role("with.label", true, "Com Label"),
            role("without.label", true, null)
        ));

        ListRolesResponse response = useCase.execute(new ListRolesQuery(true, false));

        assertEquals("Com Label", response.items().stream()
            .filter(it -> it.code().equals("with.label")).findFirst().orElseThrow().label());
        assertEquals("without.label", response.items().stream()
            .filter(it -> it.code().equals("without.label")).findFirst().orElseThrow().label());
    }

    @Test
    void platformAdminIsExcludedUnlessIncludePlatformAdminIsTrue() {
        when(roleRepository.findAll()).thenReturn(List.of(
            role("platform.admin", true),
            role("customers.admin", true)
        ));

        ListRolesResponse response = useCase.execute(new ListRolesQuery(false, false));

        assertFalse(response.items().stream().anyMatch(it -> it.code().equals("platform.admin")));
    }
}
