package com.project.llt.section;

import com.project.llt.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import static com.project.llt.constants.ExceptionMessageConstants.SECTION_NOT_FOUND;
import static com.project.llt.mapper.SectionMapper.convertToDto;
import static com.project.llt.mapper.SectionMapper.convertToEntity;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionDao sectionDao;
    private final ModelMapper modelMapper;

    @Override
    public List<SectionDto> getAllSections() {
        List<Section> sections = sectionDao.findAll();
        return !sections.isEmpty() ?  sections.stream().map(section -> convertToDto(modelMapper, section)).toList() : new ArrayList<>();
    }

    @Override
    public SectionDto getSectionById(Long id) {
        Section section = getSectionEntityById(id);
        return convertToDto(modelMapper, section);
    }

    @Override
    public SectionDto saveSection(SectionDto sectionDto) {
        Section section = convertToEntity(modelMapper, sectionDto);
        Section savedSection = sectionDao.save(section);
        return convertToDto(modelMapper, savedSection);
    }

    @Override
    public SectionDto updateSectionById(SectionDto sectionDto, Long id) {
        Section sectionToUpdate = getSectionEntityById(id);
        sectionToUpdate.setName(sectionDto.getName());
        sectionToUpdate.setIconId(sectionDto.getIconId());
        sectionToUpdate.setImageId(sectionDto.getImageId());
        Section updatedSection = sectionDao.update(sectionToUpdate);
        return convertToDto(modelMapper, updatedSection);
    }

    @Override
    public void deleteSectionById(Long id) {
        Section sectionToDelete = getSectionEntityById(id);
        sectionDao.delete(sectionToDelete);
    }

    @Override
    public Section getSectionEntityById(Long id) {
        return sectionDao.findById(id).orElseThrow(() -> new ResourceNotFoundException(String.format(SECTION_NOT_FOUND, id)));
    }
}
