package br.com.deltaglobalbank.identity.features.roles.updateRoleStatus;

import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdateRoleStatusController {

    private final UpdateRoleStatusUseCase updateRoleStatusUseCase;

    public UpdateRoleStatusController(UpdateRoleStatusUseCase updateRoleStatusUseCase) {
        this.updateRoleStatusUseCase = updateRoleStatusUseCase;
    }

    @PatchMapping("/admin/roles/{roleId}/status")
    @PreAuthorize("hasRole('platform.admin')")
    public ResponseEntity<UpdateRoleStatusResponse> updateStatus(
        @PathVariable UUID roleId,
        @Valid @RequestBody UpdateRoleStatusRequest request
    ) {
        UpdateRoleStatusCommand command = new UpdateRoleStatusCommand(roleId, request.active());
        return ResponseEntity.ok(updateRoleStatusUseCase.execute(command));
    }
}
