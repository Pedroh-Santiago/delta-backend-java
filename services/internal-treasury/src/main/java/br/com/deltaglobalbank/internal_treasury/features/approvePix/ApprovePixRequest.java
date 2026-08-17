package br.com.deltaglobalbank.internal_treasury.features.approvePix;

import java.util.List;
import java.util.UUID;

public record ApprovePixRequest(
    List<UUID> approved
) {
}
