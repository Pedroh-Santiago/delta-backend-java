package br.com.deltaglobalbank.identity.features.users.listUsers;

import java.util.List;

public record ListUsersResponse(
    List<ListedUser> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
}
