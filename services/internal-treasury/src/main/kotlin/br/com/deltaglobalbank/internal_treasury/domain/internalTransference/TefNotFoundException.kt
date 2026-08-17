package br.com.deltaglobalbank.internal_treasury.domain.internalTransference

import java.util.UUID

class TefNotFoundException(id: UUID) : RuntimeException("TEF $id não encontrada")