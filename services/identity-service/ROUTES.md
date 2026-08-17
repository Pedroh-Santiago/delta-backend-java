# Identity Service — HTTP Routes

## Auth

| Método | Path | Body | Resposta |
|--------|------|------|----------|
| `POST` | `/auth/login` | `LoginRequest` | `LoginResponse` |
| `POST` | `/auth/refresh` | `RefreshTokenRequest` | `RefreshTokenResponse` |
| `POST` | `/auth/change-password` | `ChangePasswordRequest` | `ChangePasswordResponse` |

## JWKS

| Método | Path | Body | Resposta |
|--------|------|------|----------|
| `GET` | `/.well-known/jwks.json` | — | `JwkResponse` |

## Tenants (`platform.admin`)

| Método | Path | Path Params | Query | Body | Resposta |
|--------|------|-------------|-------|------|----------|
| `POST` | `/admin/tenants` | — | — | `CreateTenantRequest` | `CreateTenantResponse` |
| `GET` | `/admin/tenants` | — | `page` (int, default 0), `size` (int, default 20) | — | `ListTenantsResponse` |

## Users

| Método | Path | Path Params | Query | Body | Resposta | Permissão |
|--------|------|-------------|-------|------|----------|-----------|
| `GET` | `/me` | — | — | — | `MeResponse` | autenticado |
| `POST` | `/admin/users` | — | — | `CreateUserRequest` | `CreateUserResponse` | `identity.admin` |
| `POST` | `/admin/tenants/{tenantId}/users` | `tenantId` (UUID) | — | `CreateUserRequest` | `CreateUserResponse` | `platform.admin` |
| `GET` | `/admin/users` | — | `page` (int, default 0), `size` (int, default 20) | — | `ListUsersResponse` | `identity.admin` ou `platform.admin` |
| `GET` | `/admin/tenants/{tenantId}/users` | `tenantId` (UUID) | `page` (int, default 0), `size` (int, default 20) | — | `ListUsersResponse` | `platform.admin` |

## Roles

| Método | Path | Path Params | Body | Resposta | Permissão |
|--------|------|-------------|------|----------|-----------|
| `GET` | `/admin/roles` | — | — | `ListRolesResponse` | `identity.admin` ou `platform.admin` |
| `POST` | `/admin/users/{userId}/roles` | `userId` (UUID) | `AssignRolesRequest` | `AssignRolesResponse` | `identity.admin` |
| `POST` | `/admin/tenants/{tenantId}/users/{userId}/roles` | `tenantId`, `userId` (UUID) | `AssignRolesRequest` | `AssignRolesResponse` | `platform.admin` |

## API Clients

| Método | Path | Path Params | Query | Body | Resposta | Permissão |
|--------|------|-------------|-------|------|----------|-----------|
| `POST` | `/admin/api-clients` | — | — | `CreateApiClientRequest` | `CreateApiClientResponse` | `identity.admin` |
| `POST` | `/admin/tenants/{tenantId}/api-clients` | `tenantId` (UUID) | — | `CreateApiClientRequest` | `CreateApiClientResponse` | `platform.admin` |
| `GET` | `/admin/api-clients` | — | `page`, `size` | — | `ListClientsResponse` | `identity.admin` |
| `GET` | `/admin/tenants/{tenantId}/api-clients` | `tenantId` (UUID) | `page`, `size` | — | `ListClientsResponse` | `platform.admin` |

## API Keys

| Método | Path | Path Params | Query | Body | Resposta | Permissão |
|--------|------|-------------|-------|------|----------|-----------|
| `POST` | `/admin/api-clients/{apiClientId}/keys` | `apiClientId` (UUID) | — | `CreateApiKeyRequest` | `CreateApiKeyResponse` | `identity.admin` |
| `POST` | `/admin/tenants/{tenantId}/api-clients/{apiClientId}/keys` | `tenantId`, `apiClientId` (UUID) | — | `CreateApiKeyRequest` | `CreateApiKeyResponse` | `platform.admin` |
| `GET` | `/admin/api-clients/{apiClientId}/keys` | `apiClientId` (UUID) | `page`, `size` | — | `ClientApiKeyResponse` | `identity.admin` |
| `GET` | `/admin/tenants/{tenantId}/api-clients/{apiClientId}/keys` | `tenantId`, `apiClientId` (UUID) | `page`, `size` | — | `ClientApiKeyResponse` | `platform.admin` |

## Modules (`platform.admin`)

| Método | Path | Path Params | Body | Resposta |
|--------|------|-------------|------|----------|
| `GET` | `/admin/modules` | — | — | `ListModulesResponse` |
| `GET` | `/admin/tenants/{tenantId}/modules` | `tenantId` (UUID) | — | `ListTenantModulesResponse` |
| `POST` | `/admin/tenants/{tenantId}/modules/{moduleCode}` | `tenantId` (UUID), `moduleCode` (String) | — | `EnableModuleResponse` |
| `DELETE` | `/admin/tenants/{tenantId}/modules/{moduleCode}` | `tenantId` (UUID), `moduleCode` (String) | — | void |

## IP Allowlist

| Método | Path | Path Params | Body | Resposta | Permissão |
|--------|------|-------------|------|----------|-----------|
| `GET` | `/admin/tenant-ip-allowlist` | — | — | `ListTenantIpAllowlistResponse` | `identity.admin` |
| `GET` | `/admin/tenants/{tenantId}/ip-allowlist` | `tenantId` (UUID) | — | `ListTenantIpAllowlistResponse` | `platform.admin` |
| `POST` | `/admin/tenant-ip-allowlist` | — | `CreateTenantIpAllowlistRequest` | `CreateTenantIpAllowlistResponse` | `identity.admin` |
| `POST` | `/admin/tenants/{tenantId}/ip-allowlist` | `tenantId` (UUID) | `CreateTenantIpAllowlistRequest` | `CreateTenantIpAllowlistResponse` | `platform.admin` |
| `DELETE` | `/admin/tenant-ip-allowlist/{id}` | `id` (UUID) | — | void | `identity.admin` |
| `DELETE` | `/admin/tenants/{tenantId}/ip-allowlist/{id}` | `tenantId`, `id` (UUID) | — | void | `platform.admin` |
