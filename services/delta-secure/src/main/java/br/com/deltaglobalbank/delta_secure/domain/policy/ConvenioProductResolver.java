package br.com.deltaglobalbank.delta_secure.domain.policy;

import java.util.Map;

public final class ConvenioProductResolver {

    private ConvenioProductResolver() {
    }

    public static int resolve(Convenio convenio, Map<String, Integer> typeOfProduct) {
        Integer typeCode = typeOfProduct.get(convenio.name().toLowerCase());
        if (typeCode == null) {
            throw new IllegalArgumentException("type_of_product não configurado para o convênio " + convenio);
        }
        return typeCode;
    }
}
