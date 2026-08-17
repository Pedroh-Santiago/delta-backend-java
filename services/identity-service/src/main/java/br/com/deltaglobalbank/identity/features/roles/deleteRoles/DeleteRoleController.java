package br.com.deltaglobalbank.identity.features.roles.deleteRoles;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteRoleController {

    private final DeleteRoleUseCase deleteRoleUseCase;

    public DeleteRoleController(DeleteRoleUseCase deleteRoleUseCase) {
        this.deleteRoleUseCase = deleteRoleUseCase;
    }

    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("hasRole('platform.admin') or hasRole('identity.admin')")
    public ResponseEntity<Void> delete(@PathVariable UUID roleId) {
        deleteRoleUseCase.execute(roleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
