package br.com.deltaglobalbank.internal_treasury.features.approveInternalTransferece;

import java.util.List;
import java.util.UUID;

public record ApproveInternalTransferenceRequest(
    List<UUID> approved
) {
}
