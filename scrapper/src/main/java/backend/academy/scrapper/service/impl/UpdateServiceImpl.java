package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.dto.UpdateDto;
import backend.academy.scrapper.repository.UpdateRepository;
import backend.academy.scrapper.service.UpdateService;
import org.springframework.stereotype.Service;
import java.util.List;

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
    public List<UpdateDto> findUnsents() {
        return updateRepo.findUnsents(1, 1);
    }

    @Override
    public void markSent(List<Long> updateIds) {
        updateRepo.markSent(updateIds);
    }
}

