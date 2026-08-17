package br.com.deltaglobalbank.delta_secure.domain.shared.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CpfTest {

    @Test
    void poeMascaraNoCpfQueVemSoComDigitos() {
        assertEquals("111.444.777-35", new Cpf("11144477735").masked());
    }

    @Test
    void mantemOCpfMascaradoQueJaVemFormatado() {
        assertEquals("660.102.178-49", new Cpf("660.102.178-49").masked());
    }

    @Test
    void devolveOCpfComoVeioQuandoNaoTem11Digitos() {
        assertEquals("123", new Cpf("123").masked());
    }
}
