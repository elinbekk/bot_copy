package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.UpdateDto;
import java.util.List;

public interface UpdateService {
    void save(UpdateDto update);
    List<UpdateDto> getAll();
    void markSent(List<Long> updateIds);
}
