package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.dto.UpdateDto;
import backend.academy.scrapper.repository.UpdateRepository;
import backend.academy.scrapper.service.UpdateService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UpdateServiceImpl implements UpdateService {
    private final UpdateRepository updateRepo;

    public UpdateServiceImpl(UpdateRepository updRepo) {
        this.updateRepo = updRepo;
    }

    @Override
    public void save(UpdateDto update) {
        updateRepo.save(update.getLinkId(), update.getPayload(), update.getOccurredAt());
    }

    @Override
    public List<UpdateDto> getAll() {
        return updateRepo.findAll();
    }

    @Override
    public void markSent(List<Long> updateIds) {
        updateRepo.markSent(updateIds);
    }
}
