package br.com.deltaglobalbank.delta_secure.infrastructure.client.sworks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SWorksFieldFormatsTest {

    @Test
    void aceitaValorNumericoERecusaAlfanumerico() {
        assertEquals("572313", SWorksFieldFormats.apenasSeNumerico("572313"));
        assertNull(SWorksFieldFormats.apenasSeNumerico("DELT1DELT1000000051"));
        assertNull(SWorksFieldFormats.apenasSeNumerico("PROP-9911"));
        assertNull(SWorksFieldFormats.apenasSeNumerico(""));
        assertNull(SWorksFieldFormats.apenasSeNumerico(null));
    }
}
