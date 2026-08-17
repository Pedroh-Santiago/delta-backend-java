package br.com.deltaglobalbank.delta_secure.features.sworks;

import br.com.deltaglobalbank.delta_secure.domain.sworks.DocumentDispatchResult;

@FunctionalInterface
public interface DocumentDispatchNotifier {
    void notify(DocumentDispatchResult result);
}
