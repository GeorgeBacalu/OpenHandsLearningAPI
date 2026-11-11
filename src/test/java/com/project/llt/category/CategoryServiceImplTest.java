package com.project.llt.category;

import com.project.llt.exception.ResourceNotFoundException;
import com.project.llt.section.SectionService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static com.project.llt.constants.ExceptionMessageConstants.CATEGORY_NOT_FOUND;
import static com.project.llt.constants.IdentifierConstants.INVALID_ID;
import static com.project.llt.constants.IdentifierConstants.VALID_ID;
import static com.project.llt.mapper.CategoryMapper.convertToDto;
import static com.project.llt.mapper.CategoryMapper.convertToDtoList;
import static com.project.llt.mock.CategoryMock.getMockedCategories;
import static com.project.llt.mock.CategoryMock.getMockedCategoryOne;
import static com.project.llt.mock.CategoryMock.getMockedCategoryTwo;
import static com.project.llt.mock.CategoryMock.getMockedChildrenCategoriesAlphabet;
import static com.project.llt.mock.CategoryMock.getMockedChildrenCategoriesAlphabetFilteredByName;
import static com.project.llt.mock.CategoryMock.getMockedFavoriteCategories;
import static com.project.llt.mock.CategoryMock.getMockedFavoriteCategoriesFilteredByName;
import static com.project.llt.mock.CategoryMock.getMockedParentCategories;
import static com.project.llt.mock.CategoryMock.getMockedParentCategoriesFilteredByName;
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
    private CategoryDao categoryDao;

    @Mock
    private SectionService sectionService;

    @Spy
    private ModelMapper modelMapper;

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
        categoryDtoOne = convertToDto(modelMapper, categoryOne);
        categoryDtoTwo = convertToDto(modelMapper, categoryTwo);
        categoryDtos = convertToDtoList(modelMapper, categories);
        parentCategoryDtos = convertToDtoList(modelMapper, parentCategories);
        filteredParentCategoryDtos = convertToDtoList(modelMapper, filteredParentCategories);
        childrenCategoryDtosAlphabet = convertToDtoList(modelMapper, childrenCategoriesAlphabet);
        filteredChildrenCategoryDtosAlphabet = convertToDtoList(modelMapper, filteredChildrenCategoriesAlphabet);
        favoriteCategoryDtos = convertToDtoList(modelMapper, favoriteCategories);
        filteredFavoriteCategoryDtos = convertToDtoList(modelMapper, filteredFavoriteCategories);
    }

    @Test
    void getAllCategories_shouldReturnListOfCategories() {
        given(categoryDao.findAll()).willReturn(categories);
        List<CategoryDto> result = categoryService.getAllCategories();
        assertThat(result).isEqualTo(categoryDtos);
    }

    @Test
    void getCategoryById_withValidId_shouldReturnCategoryWithGivenId() {
        given(categoryDao.findById(anyLong())).willReturn(Optional.ofNullable(categoryOne));
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
        given(categoryDao.save(any(Category.class))).willReturn(categoryOne);
        CategoryDto result = categoryService.saveCategory(categoryDtoOne);
        verify(categoryDao).save(categoryCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, categoryCaptor.getValue()));
    }

    @Test
    void updateCategoryById_withValidId_shouldUpdateCategoryWithGivenId() {
        Category category = categoryTwo; category.setId(VALID_ID);
        given(categoryDao.findById(anyLong())).willReturn(Optional.ofNullable(categoryTwo));
        given(categoryDao.update(any(Category.class))).willReturn(category);
        CategoryDto result = categoryService.updateCategoryById(categoryDtoTwo, VALID_ID);
        verify(categoryDao).update(categoryCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, categoryCaptor.getValue()));
    }

    @Test
    void updateCategoryById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> categoryService.updateCategoryById(categoryDtoTwo, INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(CATEGORY_NOT_FOUND, INVALID_ID));
        verify(categoryDao, never()).update(any(Category.class));
    }

    @Test
    void deleteCategoryById_withValidId_shouldRemoveCategoryWithGivenIdFromList() {
        given(categoryDao.findById(anyLong())).willReturn(Optional.ofNullable(categoryOne));
        categoryService.deleteCategoryById(VALID_ID);
        verify(categoryDao).delete(categoryOne);
    }

    @Test
    void deleteCategoryById_withInvalidId_shouldThrowException() {
        assertThatThrownBy(() -> categoryService.deleteCategoryById(INVALID_ID))
              .isInstanceOf(ResourceNotFoundException.class)
              .hasMessage(String.format(CATEGORY_NOT_FOUND, INVALID_ID));
        verify(categoryDao, never()).delete(any(Category.class));
    }

    @Test
    void getCategoriesByParentIdAndSectionIdAndName_whenParentIdNullAndNameBlank_shouldReturnListOfParentCategories() {
        given(categoryDao.findByParentIdNullAndSectionId(anyLong())).willReturn(parentCategories);
        List<CategoryDto> result = categoryService.getCategoriesByParentIdAndSectionIdAndName(null, VALID_ID, "");
        assertThat(result).isEqualTo(parentCategoryDtos);
    }

    @Test
    void getCategoriesByParentIdAndSectionIdAndName_whenParentIdNullAndNameNotBlank_shouldReturnListOfParentCategoriesFilteredByName() {
        given(categoryDao.findByParentIdNullAndSectionIdAndNameContaining(anyLong(), anyString())).willReturn(filteredParentCategories);
        List<CategoryDto> result = categoryService.getCategoriesByParentIdAndSectionIdAndName(null, VALID_ID, "a");
        assertThat(result).isEqualTo(filteredParentCategoryDtos);
    }

    @Test
    void getCategoriesByParentIdAndSectionIdAndName_whenParentIdNotNullAndNameBlank_shouldReturnListOfChildrenCategories() {
        given(categoryDao.findByParentIdAndSectionId(anyLong(), anyLong())).willReturn(childrenCategoriesAlphabet);
        List<CategoryDto> result = categoryService.getCategoriesByParentIdAndSectionIdAndName(VALID_ID, VALID_ID, "");
        assertThat(result).isEqualTo(childrenCategoryDtosAlphabet);
    }

    @Test
    void getCategoriesByParentIdAndSectionIdAndName_whenParentIdNotNullAndNameNotBlank_shouldReturnListOfChildrenCategoriesFilteredByName() {
        given(categoryDao.findByParentIdAndSectionIdAndNameContaining(anyLong(), anyLong(), anyString())).willReturn(filteredChildrenCategoriesAlphabet);
        List<CategoryDto> result = categoryService.getCategoriesByParentIdAndSectionIdAndName(VALID_ID, VALID_ID, "a");
        assertThat(result).isEqualTo(filteredChildrenCategoryDtosAlphabet);
    }

    @Test
    void getFavoriteCategories_shouldReturnListOfFavorites() {
        given(categoryDao.findFavorites()).willReturn(favoriteCategories);
        List<CategoryDto> result = categoryService.getFavoriteCategories();
        assertThat(result).isEqualTo(favoriteCategoryDtos);
    }

    @Test
    void getFavoritesByName_whenNameIsBlank_shouldReturnListOfFavorites() {
        given(categoryDao.findFavorites()).willReturn(favoriteCategories);
        List<CategoryDto> result = categoryService.getFavoritesByName("");
        assertThat(result).isEqualTo(favoriteCategoryDtos);
    }

    @Test
    void getFavoritesByName_whenNameIsNotBlank_shouldReturnListOfFavoritesFilteredByName() {
        given(categoryDao.findFavoritesByNameContaining(anyString())).willReturn(filteredFavoriteCategories);
        List<CategoryDto> result = categoryService.getFavoritesByName("a");
        assertThat(result).isEqualTo(filteredFavoriteCategoryDtos);
    }

    @Test
    void saveFavorite_shouldAddFavoriteCategoryToList() {
        Category category = categoryOne; category.setIsFavorite(true);
        given(categoryDao.findById(anyLong())).willReturn(Optional.ofNullable(categoryOne));
        given(categoryDao.update(any(Category.class))).willReturn(category);
        CategoryDto result = categoryService.saveFavorite(categoryOne.getId());
        verify(categoryDao).update(categoryCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, categoryCaptor.getValue()));
    }

    @Test
    void deleteFavorite_shouldAddFavoriteCategoryToList() {
        Category category = categoryOne; category.setIsFavorite(false);
        given(categoryDao.findById(anyLong())).willReturn(Optional.ofNullable(categoryOne));
        given(categoryDao.update(any(Category.class))).willReturn(category);
        CategoryDto result = categoryService.deleteFavorite(categoryOne.getId());
        verify(categoryDao).update(categoryCaptor.capture());
        assertThat(result).isEqualTo(convertToDto(modelMapper, categoryCaptor.getValue()));
    }
}
