package com.project.ohl.category;

import com.project.ohl.adapter.web.CategoryController;
import com.project.ohl.application.mapper.CategoryMapper;
import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.application.port.in.CategoryService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.project.ohl.constants.IdentifierConstants.VALID_ID;
import static com.project.ohl.mock.CategoryMock.getMockedCategories;
import static com.project.ohl.mock.CategoryMock.getMockedCategoryAlphabet;
import static com.project.ohl.mock.CategoryMock.getMockedCategoryNumbers;
import static com.project.ohl.mock.CategoryMock.getMockedFavoriteCategories;
import static com.project.ohl.mock.CategoryMock.getMockedFavoriteCategoriesFilteredByName;
import static com.project.ohl.mock.CategoryMock.getMockedParentCategoriesFilteredByName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private final CategoryMapper mapper = Mappers.getMapper(CategoryMapper.class);

    @InjectMocks
    private CategoryController categoryController;

    @Mock
    private CategoryService categoryService;

    private CategoryDto categoryDtoOne;
    private CategoryDto categoryDtoTwo;
    private List<CategoryDto> categoryDtos;
    private List<CategoryDto> filteredCategoryDtos;
    private List<CategoryDto> favoriteCategoryDtos;
    private List<CategoryDto> filteredFavoriteCategoryDtos;

    @BeforeEach
    void setUp() {
        categoryDtoOne = mapper.convertToDto(getMockedCategoryAlphabet());
        categoryDtoTwo = mapper.convertToDto(getMockedCategoryNumbers());
        categoryDtos = mapper.convertToDtoList(getMockedCategories());
        filteredCategoryDtos = mapper.convertToDtoList(getMockedParentCategoriesFilteredByName());
        favoriteCategoryDtos = mapper.convertToDtoList(getMockedFavoriteCategories());
        filteredFavoriteCategoryDtos = mapper.convertToDtoList(getMockedFavoriteCategoriesFilteredByName());
    }

    @Test
    void getAllCategories_shouldReturnListOfCategories() {
        given(categoryService.getAllCategories()).willReturn(categoryDtos);
        ResponseEntity<List<CategoryDto>> response = categoryController.getAllCategories();
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(categoryDtos);
    }

    @Test
    void getCategoryById_shouldReturnCategoryWithGivenId() {
        given(categoryService.getCategoryById(any(UUID.class))).willReturn(categoryDtoOne);
        ResponseEntity<CategoryDto> response = categoryController.getCategoryById(VALID_ID, null);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(categoryDtoOne);
    }

    @Test
    void saveCategory_shouldAddCategoryToList() {
        given(categoryService.saveCategory(any(CategoryDto.class))).willReturn(categoryDtoOne);
        ResponseEntity<CategoryDto> response = categoryController.saveCategory(categoryDtoOne);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(categoryDtoOne);
    }

    @Test
    void updateCategoryById_shouldUpdateCategoryWithGivenId() {
        CategoryDto categoryDto = categoryDtoTwo; categoryDto.setId(VALID_ID);
        given(categoryService.updateCategoryById(any(CategoryDto.class), any(UUID.class))).willReturn(categoryDto);
        ResponseEntity<CategoryDto> response = categoryController.updateCategoryById(categoryDtoTwo, VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(categoryDto);
    }

    @Test
    void deleteCategoryById_shouldRemoveCategoryWithGivenIdFromList() {
        ResponseEntity<Void> response = categoryController.deleteCategoryById(VALID_ID);
        verify(categoryService).deleteCategoryById(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void getCategoriesByParentIdAndSectionIdAndName_shouldReturnListOfCategoriesFilteredByName() {
        given(categoryService.getCategoriesByParentIdAndSectionIdAndName(any(UUID.class), any(UUID.class), anyString())).willReturn(filteredCategoryDtos);
        ResponseEntity<List<CategoryDto>> response = categoryController.getCategoriesByParentIdAndSectionIdAndName(VALID_ID, VALID_ID, "a", null, "VIDEO");
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(filteredCategoryDtos);
    }

    @Test
    void getFavoriteCategories_shouldReturnListOfFavorites() {
        given(categoryService.getFavoriteCategories(any(UUID.class))).willReturn(favoriteCategoryDtos);
        ResponseEntity<List<CategoryDto>> response = categoryController.getFavoriteCategories(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(favoriteCategoryDtos);
    }

    @Test
    void getFavoritesByName_shouldReturnListOfFavoritesFilteredByName() {
        given(categoryService.getFavoritesByName(any(UUID.class), anyString())).willReturn(filteredFavoriteCategoryDtos);
        ResponseEntity<List<CategoryDto>> response = categoryController.getFavoritesByName(VALID_ID, "a");
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(filteredFavoriteCategoryDtos);
    }

    @Test
    void saveFavorite_saveFavorite_shouldAddFavoriteCategoryToList() {
        given(categoryService.saveFavorite(any(UUID.class), any(UUID.class))).willReturn(categoryDtoOne);
        ResponseEntity<CategoryDto> response = categoryController.saveFavorite(VALID_ID, VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(categoryDtoOne);
    }

    @Test
    void deleteFavorite_saveFavorite_shouldAddFavoriteCategoryToList() {
        given(categoryService.deleteFavorite(any(UUID.class), any(UUID.class))).willReturn(categoryDtoOne);
        ResponseEntity<CategoryDto> response = categoryController.deleteFavorite(VALID_ID, VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(categoryDtoOne);
    }
}
