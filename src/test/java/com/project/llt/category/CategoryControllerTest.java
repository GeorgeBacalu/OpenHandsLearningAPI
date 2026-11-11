package com.project.llt.category;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.project.llt.constants.IdentifierConstants.VALID_ID;
import static com.project.llt.mapper.CategoryMapper.convertToDto;
import static com.project.llt.mapper.CategoryMapper.convertToDtoList;
import static com.project.llt.mock.CategoryMock.getMockedCategories;
import static com.project.llt.mock.CategoryMock.getMockedCategoryAlphabet;
import static com.project.llt.mock.CategoryMock.getMockedCategoryNumbers;
import static com.project.llt.mock.CategoryMock.getMockedFavoriteCategories;
import static com.project.llt.mock.CategoryMock.getMockedFavoriteCategoriesFilteredByName;
import static com.project.llt.mock.CategoryMock.getMockedParentCategoriesFilteredByName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @InjectMocks
    private CategoryController categoryController;

    @Mock
    private CategoryService categoryService;

    @Spy
    private ModelMapper modelMapper;

    private CategoryDto categoryDtoOne;
    private CategoryDto categoryDtoTwo;
    private List<CategoryDto> categoryDtos;
    private List<CategoryDto> filteredCategoryDtos;
    private List<CategoryDto> favoriteCategoryDtos;
    private List<CategoryDto> filteredFavoriteCategoryDtos;

    @BeforeEach
    void setUp() {
        categoryDtoOne = convertToDto(modelMapper, getMockedCategoryAlphabet());
        categoryDtoTwo = convertToDto(modelMapper, getMockedCategoryNumbers());
        categoryDtos = convertToDtoList(modelMapper, getMockedCategories());
        filteredCategoryDtos = convertToDtoList(modelMapper, getMockedParentCategoriesFilteredByName());
        favoriteCategoryDtos = convertToDtoList(modelMapper, getMockedFavoriteCategories());
        filteredFavoriteCategoryDtos = convertToDtoList(modelMapper, getMockedFavoriteCategoriesFilteredByName());
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
        given(categoryService.getCategoryById(anyLong())).willReturn(categoryDtoOne);
        ResponseEntity<CategoryDto> response = categoryController.getCategoryById(VALID_ID);
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
        given(categoryService.updateCategoryById(any(CategoryDto.class), anyLong())).willReturn(categoryDto);
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
        given(categoryService.getCategoriesByParentIdAndSectionIdAndName(anyLong(), anyLong(), anyString())).willReturn(filteredCategoryDtos);
        ResponseEntity<List<CategoryDto>> response = categoryController.getCategoriesByParentIdAndSectionIdAndName(VALID_ID, VALID_ID, "a");
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(filteredCategoryDtos);
    }

    @Test
    void getFavoriteCategories_shouldReturnListOfFavorites() {
        given(categoryService.getFavoriteCategories()).willReturn(favoriteCategoryDtos);
        ResponseEntity<List<CategoryDto>> response = categoryController.getFavoriteCategories();
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(favoriteCategoryDtos);
    }

    @Test
    void getFavoritesByName_shouldReturnListOfFavoritesFilteredByName() {
        given(categoryService.getFavoritesByName(anyString())).willReturn(filteredFavoriteCategoryDtos);
        ResponseEntity<List<CategoryDto>> response = categoryController.getFavoritesByName("a");
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(filteredFavoriteCategoryDtos);
    }

    @Test
    void saveFavorite_saveFavorite_shouldAddFavoriteCategoryToList() {
        given(categoryService.saveFavorite(anyLong())).willReturn(categoryDtoOne);
        ResponseEntity<CategoryDto> response = categoryController.saveFavorite(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(categoryDtoOne);
    }

    @Test
    void deleteFavorite_saveFavorite_shouldAddFavoriteCategoryToList() {
        given(categoryService.deleteFavorite(anyLong())).willReturn(categoryDtoOne);
        ResponseEntity<CategoryDto> response = categoryController.deleteFavorite(VALID_ID);
        assertNotNull(response);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(categoryDtoOne);
    }
}
