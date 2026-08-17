package br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BrazilianDateTest {

    @Test
    void converteDataIsoParaOFormatoBrasileiro() {
        assertEquals("01/01/1990", BrazilianDate.toBrDisplayOrRaw("1990-01-01"));
        assertEquals("06/11/2002", BrazilianDate.toBrDisplayOrRaw("2002-11-06"));
    }

    @Test
    void devolveADataComoVeioQuandoNaoEIso() {
        assertEquals("06/11/2002", BrazilianDate.toBrDisplayOrRaw("06/11/2002"));
        assertEquals("sem data", BrazilianDate.toBrDisplayOrRaw("sem data"));
    }
}
