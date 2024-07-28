package hexlet.code.service;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.repository.LabelRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class LabelService {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private LabelMapper labelMapper;

    public LabelDTO create(LabelCreateDTO labelCreateDTO) {
        var label = labelMapper.map(labelCreateDTO);

        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public List<LabelDTO> getAll() {
        var tasks = labelRepository.findAll();
        return tasks.stream()
                .map(labelMapper::map)
                .toList();
    }

    public LabelDTO findById(Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label With Id: " + id + " Not Found"));
        return labelMapper.map(label);
    }

    public LabelDTO update(LabelUpdateDTO labelUpdateDTO, Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label With Id: " + id + " Not Found"));
        labelMapper.update(labelUpdateDTO, label);

        labelRepository.save(label);
        return labelMapper.map(label);
    }

    public void delete(Long id) {
        labelRepository.deleteById(id);
    }
}
