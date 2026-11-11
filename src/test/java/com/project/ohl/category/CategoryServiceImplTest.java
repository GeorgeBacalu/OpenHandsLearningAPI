package com.project.ohl.category;

import com.project.ohl.application.model.CategoryDto;
import com.project.ohl.application.port.out.CategoryRepositoryPort;
import com.project.ohl.application.service.CategoryServiceImpl;
import com.project.ohl.domain.model.Category;

import com.project.ohl.domain.exception.ResourceNotFoundException;
import com.project.ohl.application.port.in.SectionService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.project.ohl.application.error.ExceptionMessageConstants.CATEGORY_NOT_FOUND;
import static com.project.ohl.constants.IdentifierConstants.INVALID_ID;
import static com.project.ohl.constants.IdentifierConstants.VALID_ID;
import static com.project.ohl.application.mapper.CategoryMapper.convertToDto;
import static com.project.ohl.application.mapper.CategoryMapper.convertToDtoList;
import static com.project.ohl.mock.CategoryMock.getMockedCategories;
import static com.project.ohl.mock.CategoryMock.getMockedCategoryOne;
import static com.project.ohl.mock.CategoryMock.getMockedCategoryTwo;
import static com.project.ohl.mock.CategoryMock.getMockedChildrenCategoriesAlphabet;
import static com.project.ohl.mock.CategoryMock.getMockedChildrenCategoriesAlphabetFilteredByName;
import static com.project.ohl.mock.CategoryMock.getMockedFavoriteCategories;
import static com.project.ohl.mock.CategoryMock.getMockedFavoriteCategoriesFilteredByName;
import static com.project.ohl.mock.CategoryMock.getMockedParentCategories;
import static com.project.ohl.mock.CategoryMock.getMockedParentCategoriesFilteredByName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Mock
    private CategoryRepositoryPort categoryRepository;

    @Mock
    private SectionService sectionService;

    @Captor
    private ArgumentCaptor<Category> categoryCaptor;

    private Category categoryOne;
    private Category categoryTwo;
    private List<Category> categories;
    private List<Category> parentCategories;
    private List<Category> filteredParentCategories;
    private List<Category> childrenCategoriesAlphabet;
    private List<Category> filteredChildrenCategoriesAlphabet;
    private List<Category> favoriteCategories;
    private List<Category> filteredFavoriteCategories;
    private CategoryDto categoryDtoOne;
    private CategoryDto categoryDtoTwo;
    private List<CategoryDto> categoryDtos;
    private List<CategoryDto> parentCategoryDtos;
    private List<CategoryDto> filteredParentCategoryDtos;
    private List<CategoryDto> childrenCategoryDtosAlphabet;
    private List<CategoryDto> filteredChildrenCategoryDtosAlphabet;
    private List<CategoryDto> favoriteCategoryDtos;
    private List<CategoryDto> filteredFavoriteCategoryDtos;

    @BeforeEach
    void setUp() {
        categoryOne = getMockedCategoryOne();
        categoryTwo = getMockedCategoryTwo();
        categories = getMockedCategories();
        parentCategories = getMockedParentCategories();
        filteredParentCategories = getMockedParentCategoriesFilteredByName();
        childrenCategoriesAlphabet = getMockedChildrenCategoriesAlphabet();
        filteredChildrenCategoriesAlphabet = getMockedChildrenCategoriesAlphabetFilteredByName();
        favoriteCategories = getMockedFavoriteCategories();
        filteredFavoriteCategories = getMockedFavoriteCategoriesFilteredByName();
        categoryDtoOne = convertToDto(categoryOne);
        categoryDtoTwo = convertToDto(categoryTwo);
        categoryDtos = convertToDtoList(categories);
        parentCategoryDtos = convertToDtoList(parentCategories);
        filteredParentCategoryDtos = convertToDtoList(filteredParentCategories);
        childrenCategoryDtosAlphabet = convertToDtoList(childrenCategoriesAlphabet);
        filteredChildrenCategoryDtosAlphabet = convertToDtoList(filteredChildrenCategoriesAlphabet);
        favoriteCategoryDtos = convertToDtoList(favoriteCategories);
        filteredFavoriteCategoryDtos = convertToDtoList(filteredFavoriteCategories);
    }

    @Test
    void getAllCategories_shouldReturnListOfCategories() {
        given(categoryRepository.findAll()).willReturn(categories);
        List<CategoryDto> result = categoryService.getAllCategories();
        assertThat(result).isEqualTo(categoryDtos);
    }

    @Test
    void getCategoryById_withValidId_shouldReturnCategoryWithGivenId() {
        given(categoryRepository.findById(anyLong())).willReturn(Optional.ofNullable(categoryOne));
        CategoryDto result = categoryService.getCategoryById(VALID_ID);
        assertThat(result).isEqualTo(categoryDtoOne);
    }

    @Test
    void getCategoryById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> categoryService.getCategoryById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(CATEGORY_NOT_FOUND, INVALID_ID));
    }

    @Test
    void saveCategory_shouldAddCategoryToList() {
        given(categoryRepository.save(any(Category.class))).willReturn(categoryOne);
        CategoryDto result = categoryService.saveCategory(categoryDtoOne);
        verify(categoryRepository).save(categoryCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(categoryCaptor.getValue()));
    }

    @Test
    void updateCategoryById_withValidId_shouldUpdateCategoryWithGivenId() {
        Category category = categoryTwo; category.setId(VALID_ID);
        given(categoryRepository.findById(VALID_ID)).willReturn(Optional.of(category));
        given(categoryRepository.findById(categoryDtoTwo.getParentId())).willReturn(Optional.of(categoryTwo.getParent()));
        given(categoryRepository.update(any(Category.class))).willReturn(category);
        CategoryDto result = categoryService.updateCategoryById(categoryDtoTwo, VALID_ID);
        verify(categoryRepository).update(categoryCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(categoryCaptor.getValue()));
    }

    @Test
    void updateCategoryById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> categoryService.updateCategoryById(categoryDtoTwo, INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(CATEGORY_NOT_FOUND, INVALID_ID));
        verify(categoryRepository, never()).update(any(Category.class));
    }

    @Test
    void deleteCategoryById_withValidId_shouldRemoveCategoryWithGivenIdFromList() {
        given(categoryRepository.findById(anyLong())).willReturn(Optional.ofNullable(categoryOne));
        categoryService.deleteCategoryById(VALID_ID);
        verify(categoryRepository).delete(categoryOne);
    }

    @Test
    void deleteCategoryById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> categoryService.deleteCategoryById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(CATEGORY_NOT_FOUND, INVALID_ID));
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void getCategoriesByParentIdAndSectionIdAndName_whenParentIdNullAndNameBlank_shouldReturnListOfParentCategories() {
        given(categoryRepository.findByParentIdNullAndSectionId(anyLong())).willReturn(parentCategories);
        List<CategoryDto> result = categoryService.getCategoriesByParentIdAndSectionIdAndName(null, VALID_ID, "");
        assertThat(result).isEqualTo(parentCategoryDtos);
    }

    @Test
    void getCategoriesByParentIdAndSectionIdAndName_whenParentIdNullAndNameNotBlank_shouldReturnListOfParentCategoriesFilteredByName() {
        given(categoryRepository.findByParentIdNullAndSectionIdAndNameContaining(anyLong(), anyString())).willReturn(filteredParentCategories);
        List<CategoryDto> result = categoryService.getCategoriesByParentIdAndSectionIdAndName(null, VALID_ID, "a");
        assertThat(result).isEqualTo(filteredParentCategoryDtos);
    }

    @Test
    void getCategoriesByParentIdAndSectionIdAndName_whenParentIdNotNullAndNameBlank_shouldReturnListOfChildrenCategories() {
        given(categoryRepository.findByParentIdAndSectionId(anyLong(), anyLong())).willReturn(childrenCategoriesAlphabet);
        List<CategoryDto> result = categoryService.getCategoriesByParentIdAndSectionIdAndName(VALID_ID, VALID_ID, "");
        assertThat(result).isEqualTo(childrenCategoryDtosAlphabet);
    }

    @Test
    void getCategoriesByParentIdAndSectionIdAndName_whenParentIdNotNullAndNameNotBlank_shouldReturnListOfChildrenCategoriesFilteredByName() {
        given(categoryRepository.findByParentIdAndSectionIdAndNameContaining(anyLong(), anyLong(), anyString())).willReturn(filteredChildrenCategoriesAlphabet);
        List<CategoryDto> result = categoryService.getCategoriesByParentIdAndSectionIdAndName(VALID_ID, VALID_ID, "a");
        assertThat(result).isEqualTo(filteredChildrenCategoryDtosAlphabet);
    }

    @Test
    void getFavoriteCategories_shouldReturnListOfFavorites() {
        given(categoryRepository.findFavorites()).willReturn(favoriteCategories);
        List<CategoryDto> result = categoryService.getFavoriteCategories();
        assertThat(result).isEqualTo(favoriteCategoryDtos);
    }

    @Test
    void getFavoritesByName_whenNameIsBlank_shouldReturnListOfFavorites() {
        given(categoryRepository.findFavorites()).willReturn(favoriteCategories);
        List<CategoryDto> result = categoryService.getFavoritesByName("");
        assertThat(result).isEqualTo(favoriteCategoryDtos);
    }

    @Test
    void getFavoritesByName_whenNameIsNotBlank_shouldReturnListOfFavoritesFilteredByName() {
        given(categoryRepository.findFavoritesByNameContaining(anyString())).willReturn(filteredFavoriteCategories);
        List<CategoryDto> result = categoryService.getFavoritesByName("a");
        assertThat(result).isEqualTo(filteredFavoriteCategoryDtos);
    }

    @Test
    void saveFavorite_shouldAddFavoriteCategoryToList() {
        Category category = categoryOne; category.setIsFavorite(true);
        given(categoryRepository.findById(anyLong())).willReturn(Optional.ofNullable(categoryOne));
        given(categoryRepository.update(any(Category.class))).willReturn(category);
        CategoryDto result = categoryService.saveFavorite(categoryOne.getId());
        verify(categoryRepository).update(categoryCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(categoryCaptor.getValue()));
    }

    @Test
    void deleteFavorite_shouldAddFavoriteCategoryToList() {
        Category category = categoryOne; category.setIsFavorite(false);
        given(categoryRepository.findById(anyLong())).willReturn(Optional.ofNullable(categoryOne));
        given(categoryRepository.update(any(Category.class))).willReturn(category);
        CategoryDto result = categoryService.deleteFavorite(categoryOne.getId());
        verify(categoryRepository).update(categoryCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(categoryCaptor.getValue()));
    }
}
