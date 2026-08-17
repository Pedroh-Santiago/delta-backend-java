package br.com.deltaglobalbank.delta_secure.features.heroseguros;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.deltaglobalbank.delta_secure.features.heroseguros.cancelPolicy.CancelPolicyController;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.cancelPolicy.CancelPolicyUseCase;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.getPolicy.GetPolicyController;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.getPolicy.GetPolicyUseCase;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy.IssuePolicyController;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy.IssuePolicyUseCase;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.issuePolicy.TermoAdesaoRegenerationService;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.quotation.QuotationController;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.quotation.QuotationUseCase;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.searchPolicies.SearchPoliciesController;
import br.com.deltaglobalbank.delta_secure.features.heroseguros.searchPolicies.SearchPoliciesUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class HeroSegurosRouteCompositionTest {

    @Test
    void getPolicyContinuaEmPostPoliciesGet() throws Exception {
        GetPolicyUseCase useCase = mock(GetPolicyUseCase.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new GetPolicyController(useCase)).build();

        mockMvc.perform(
            post("/heroseguros/prestamista/policies/get")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"convenio\":\"CLT\",\"id\":1}")
        ).andExpect(status().isOk());
    }

    @Test
    void searchPoliciesContinuaEmPostPoliciesSearch() throws Exception {
        SearchPoliciesUseCase useCase = mock(SearchPoliciesUseCase.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new SearchPoliciesController(useCase)).build();

        mockMvc.perform(
            post("/heroseguros/prestamista/policies/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"convenio\":\"CLT\",\"createdAt\":\"2026-01-01\",\"createdAtEnd\":\"2026-01-31\"}")
        ).andExpect(status().isOk());
    }

    @Test
    void cancelPolicyContinuaEmPostPoliciesCancel() throws Exception {
        CancelPolicyUseCase useCase = mock(CancelPolicyUseCase.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new CancelPolicyController(useCase)).build();

        mockMvc.perform(
            post("/heroseguros/prestamista/policies/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"convenio\":\"CLT\",\"docNumber\":\"11144477735\",\"ticket\":\"TCK-1\",\"reason\":1}")
        ).andExpect(status().isOk());
    }

    @Test
    void quotationContinuaEmPostQuotation() throws Exception {
        QuotationUseCase useCase = mock(QuotationUseCase.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new QuotationController(useCase)).build();

        mockMvc.perform(
            post("/heroseguros/prestamista/quotation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"convenio\":\"CLT\",\"debtAmount\":1000.0,\"installments\":12,\"age\":30}")
        ).andExpect(status().isOk());
    }

    @Test
    void issuePolicyContinuaEmPostPoliciesEGetTermoAdesaoContinuaNoMesmoCaminho() throws Exception {
        IssuePolicyUseCase useCase = mock(IssuePolicyUseCase.class);
        TermoAdesaoRegenerationService regenerationService = mock(TermoAdesaoRegenerationService.class);
        when(regenerationService.regenerate(org.mockito.ArgumentMatchers.any())).thenReturn("conteudo".getBytes());
        MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new IssuePolicyController(useCase, regenerationService))
            .build();

        mockMvc.perform(
            post("/heroseguros/prestamista/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"convenio\":\"CLT\",\"billed\":false,\"debtAmount\":1000.0,\"installments\":12,"
                        + "\"externalId\":null,\"lastInstallmentDate\":null,\"chargedAmount\":null,"
                        + "\"customer\":{\"name\":\"x\",\"docNumber\":\"11144477735\",\"birthday\":\"1990-01-01\",\"civil\":1,"
                        + "\"gender\":\"M\",\"phone\":\"11999999999\",\"email\":\"a@example.com\","
                        + "\"address\":{\"cep\":\"01310100\",\"address\":\"x\",\"number\":\"1\",\"neighborhood\":\"x\",\"city\":\"x\",\"state\":\"SP\"}}}"
                )
        ).andExpect(status().isCreated());

        mockMvc.perform(get("/heroseguros/prestamista/policies/TCK-1/termo-adesao"))
            .andExpect(status().isOk());
    }
}
