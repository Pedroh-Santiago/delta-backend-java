# customers-service — Design

## Visão geral

O `customers-service` é o sistema de registro de pessoas físicas (PF) que são clientes finais do Delta Global Bank. Cliente aqui é o tomador de empréstimo / titular de cartão, não a empresa contratante (essa é `tenant` no identity-service).

Responsabilidades:

- Cadastro de PF: identificação, contato, endereço, documentos, contas bancárias
- Consulta por outros micros (lending, card) via gRPC
- Auditoria de mudanças sensíveis

NÃO é responsabilidade deste micro:

- Análise de crédito / score (credit-engine-service, futuro)
- Categorização comercial / segmentação (CRM, futuro)
- Armazenamento de arquivos digitalizados (documents-service)
- Cadastro de benefícios (fora do MVP)

## Modelo de dados

### Customer (agregado raiz)

Pessoa física. Estado cadastral.

Campos:

- `id` (UUID v7)
- `tenant_id` (FK lógica pro identity)
- `cpf` (string, só dígitos, único por tenant)
- `full_name` (string)
- `birth_date` (date)
- `gender` (enum: male, female, other)
- `nationality` (string, default "brasileira")
- `mother_name` (string)
- `marital_status` (enum: single, married, divorced, widowed, stable_union)
- `email` (string, opcional)
- `status` (enum: active, inactive)
- timestamps + soft delete + created_by/updated_by

### Sub-agregados (parte do Customer)

**Phone (1:1)**
- `phone_number` (string, E.164: +5511999998888)

**Address (1:1)**
- `cep`, `street`, `number`, `complement`, `neighborhood`, `city`, `state` (UF), `country` (default BR)

**BankAccount (1:N)**
- `bank_code` (3 dígitos FEBRABAN)
- `agency`, `account_number`, `account_digit`
- `account_type` (checking, savings)
- `purpose` (disbursement, payoff)
- `is_primary` (boolean) — só dentro do mesmo `purpose`

**PersonalDocument (1:N — apenas RG e CNH no MVP)**
- `document_type` (rg, cnh)
- `document_number`
- `issuer` (enum: SSP, DETRAN, PF, MARINHA, EXERCITO, AERONAUTICA, OUTROS)
- `issuer_state` (UF)
- `issued_at` (date)
- `expires_at` (date, opcional — só CNH)

### Auditoria

**CustomerAudit (tabela separada)**

Registros enxutos de mudanças sensíveis. NÃO usa JSONB.

Eventos registrados:

- `cpf_changed`
- `full_name_changed`
- `bank_account_added`
- `bank_account_removed`
- `status_changed`
- `customer_deleted`

Mudanças não-sensíveis (email, gender, nationality, address, phone) vão pra log estruturado (Loki) com `customer_id` no campo estruturado.

Retenção: 5 anos (compliance bancário típico).

## Multi-tenant

- Discriminator column `tenant_id` em `customers`
- Tabelas filhas (addresses, phones, etc) ligam via `customer_id`, sem `tenant_id` próprio
- Validação no use case: principal.tenantId == customer.tenantId
- Isolamento via Hibernate Filter (decisão consciente, RLS fica pra futuro consistente com identity)

## Validações

### CPF

- Normalização: remove pontos/traços, salva só dígitos
- Validação de dígito verificador no value object
- Rejeita sequências repetidas (111.111.111-11)
- Unicidade verificada por tenant

### CEP

- Formato: 8 dígitos
- Validação visual/autocompletar no front via ViaCEP
- Backend não consulta API externa, apenas valida formato

### Phone

- Normalização para E.164 (+55DDXXXXXXXX)
- Aceita números internacionais

### Email

- Validação RFC simples
- Opcional

## Status do customer

Estados: `active`, `inactive`.

Transições:
- Customer nasce `active`
- Admin pode mover pra `inactive` (cliente saiu, encerrou)
- Admin pode reativar (volta pra active)
- Soft delete é separado de status (cliente deletado some das queries)

## Roles e permissões

| Role | Acesso |
|------|--------|
| customers.admin | CRUD completo |
| customers.operator | Criar, atualizar (sem delete) |
| customers.viewer | Apenas leitura |
| customers.write | B2B: criar/atualizar via api_key |
| customers.read | B2B: leitura via api_key |

Roles seedadas no identity-service via migration V3 (BACK-90).

## APIs

### REST

| Método | Endpoint | Role mínima |
|--------|----------|-------------|
| POST | /customers | customers.operator ou customers.write |
| GET | /customers | customers.viewer ou customers.read |
| GET | /customers/{id} | customers.viewer ou customers.read |
| PUT | /customers/{id} | customers.operator ou customers.write |
| DELETE | /customers/{id} | customers.admin |

Sem filtros na listagem MVP. Paginação igual identity (offset-based).

### gRPC

| Método | Uso |
|--------|-----|
| GetCustomer(id, tenant_id) | Outros micros (lending, card) buscam customer por id |
| GetCustomerByCpf(cpf, tenant_id) | Busca por CPF |
| BatchGetCustomers(ids, tenant_id) | Lote (evita N+1 quando outro micro precisa de vários) |
| SearchCustomers(query, tenant_id) | Busca por nome aproximado |

## Decisões fora do MVP

- Benefícios (INSS, servidor público) — fora deste micro. Pode entrar em micro próprio futuro ou se reintegrar conforme demanda.
- Foto / documentos digitalizados — `documents-service` armazena, customer apenas referencia metadados se necessário.
- PIX — fora do MVP. Conta bancária tradicional apenas.
- Múltiplos endereços/telefones — 1:1 no MVP. Mudança de endereço sobrescreve (sem histórico).
- Categorização comercial (vip, regular) — fora deste micro.
- Score de crédito — fora deste micro.