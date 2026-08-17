package br.com.deltaglobalbank.customers.infrastructure.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.SQLRestriction
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "customers")
@SQLDelete(sql = "UPDATE customers.customers SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
class CustomerEntity(
    @Id
    var id: UUID,

    @Column(name = "tenant_id", nullable = false)
    var tenantId: UUID,

    @Column(name = "cpf", nullable = false)
    var cpf: String,

    @Column(name = "full_name", nullable = false)
    var fullName: String,

    @Column(name = "birth_date", nullable = false)
    var birthDate: LocalDate,

    @Column(name = "gender", nullable = false)
    var gender: String,

    @Column(name = "nationality", nullable = false)
    var nationality: String,

    @Column(name = "mother_name", nullable = false)
    var motherName: String,

    @Column(name = "marital_status", nullable = false)
    var maritalStatus: String,

    @Column(name = "email")
    var email: String?,

    @Column(name = "phone_number", nullable = false)
    var phoneNumber: String,

    @Column(name = "address_cep", nullable = false)
    var addressCep: String,

    @Column(name = "address_street", nullable = false)
    var addressStreet: String,

    @Column(name = "address_number")
    var addressNumber: String?,

    @Column(name = "address_complement")
    var addressComplement: String?,

    @Column(name = "address_neighborhood")
    var addressNeighborhood: String?,

    @Column(name = "address_city", nullable = false)
    var addressCity: String,

    @Column(name = "address_state", nullable = false)
    var addressState: String,

    @Column(name = "address_country", nullable = false)
    var addressCountry: String,

    @Column(name = "status", nullable = false)
    var status: String,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,

    @Column(name = "created_by", nullable = false)
    var createdBy: UUID,

    @Column(name = "updated_by", nullable = false)
    var updatedBy: UUID,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null

)