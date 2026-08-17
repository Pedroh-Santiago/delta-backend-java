package br.com.deltaglobalbank.delta_secure.features.heroseguros.searchPolicies;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/heroseguros/prestamista")
public class SearchPoliciesController {

    private final SearchPoliciesUseCase useCase;

    public SearchPoliciesController(SearchPoliciesUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/policies/search")
    public SearchPoliciesResponse search(@Valid @RequestBody SearchPoliciesRequest request) {
        return useCase.execute(request);
    }
}
