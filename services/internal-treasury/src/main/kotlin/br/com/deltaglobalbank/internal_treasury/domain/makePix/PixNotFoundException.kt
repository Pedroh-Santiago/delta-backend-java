package br.com.deltaglobalbank.internal_treasury.domain.makePix

import java.util.UUID

class PixNotFoundException (id: UUID) : RuntimeException("PIX $id não foi encontrado")