package br.com.deltaglobalbank.customers.domain.customer

sealed class CustomerDomainException(message: String) : RuntimeException(message)

class CustomerNotFound : CustomerDomainException("customer_not_found")
class BankAccountNotFound : CustomerDomainException("bank_account_not_found")
class CpfAlreadyExists : CustomerDomainException("cpf_already_exists")
class DuplicatePrimaryAccountForPurpose : CustomerDomainException("duplicate_primary_account_for_purpose")
class SubaggregateDoesNotBelongToCustomer : CustomerDomainException("subaggregate_does_not_belong_to_customer")
class DuplicateBankAccount : CustomerDomainException("duplicate_bank_account")
class DuplicatePersonalDocument : CustomerDomainException("duplicate_document")