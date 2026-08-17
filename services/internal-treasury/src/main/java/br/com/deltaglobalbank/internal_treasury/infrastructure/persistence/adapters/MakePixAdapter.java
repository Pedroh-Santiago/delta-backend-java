package br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.adapters;

import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePix;
import br.com.deltaglobalbank.internal_treasury.domain.makePix.MakePixRepository;
import br.com.deltaglobalbank.internal_treasury.domain.shared.PaymentsStatus;
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.entities.MakePixEntity;
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.mappers.MakePixMapper;
import br.com.deltaglobalbank.internal_treasury.infrastructure.persistence.repositories.JpaMakePixRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class MakePixAdapter implements MakePixRepository {

    private final JpaMakePixRepository pixRepository;

    public MakePixAdapter(JpaMakePixRepository pixRepository) {
        this.pixRepository = pixRepository;
    }

    @Override
    public MakePix save(MakePix makePix) {
        return MakePixMapper.toDomain(pixRepository.save(MakePixMapper.toEntity(makePix)));
    }

    @Override
    public Page<MakePix> findByStatus(PaymentsStatus status, Pageable pageable) {
        return pixRepository.findByStatus(status.name(), pageable).map(MakePixMapper::toDomain);
    }

    @Override
    public Page<MakePix> findAll(Pageable pageable) {
        return pixRepository.findAll(pageable).map(MakePixMapper::toDomain);
    }

    @Override
    public MakePix findById(UUID id) {
        MakePixEntity entity = pixRepository.findById(id).orElse(null);
        return entity == null ? null : MakePixMapper.toDomain(entity);
    }
}
