package com.project.llt.mock;

import com.project.llt.category.Category;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.project.llt.mock.SectionMock.getMockedSection1;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CategoryMock {

    public static List<Category> getMockedCategories() {
        return List.of(
              getMockedCategoryAlphabet(),
              getMockedCategoryNumbers(),
              getMockedCategoryColors(),
              getMockedCategoryA(),
              getMockedCategoryB(),
              getMockedCategoryOne(),
              getMockedCategoryTwo(),
              getMockedCategoryRed(),
              getMockedCategoryOrange());
    }

    public static List<Category> getMockedParentCategories() {
        return List.of(getMockedCategoryAlphabet(), getMockedCategoryNumbers(), getMockedCategoryColors());
    }

    public static List<Category> getMockedParentCategoriesFilteredByName() {
        return List.of(getMockedCategoryAlphabet());
    }

    public static List<Category> getMockedChildrenCategoriesAlphabet() {
        return List.of(getMockedCategoryA(), getMockedCategoryB());
    }

    public static List<Category> getMockedChildrenCategoriesAlphabetFilteredByName() {
        return List.of(getMockedCategoryA());
    }

    public static List<Category> getMockedFavoriteCategories() {
        return List.of(
              getMockedCategoryAlphabet(),
              getMockedCategoryColors(),
              getMockedCategoryA(),
              getMockedCategoryTwo(),
              getMockedCategoryOrange());
    }

    public static List<Category> getMockedFavoriteCategoriesFilteredByName() {
        return List.of(
              getMockedCategoryAlphabet(),
              getMockedCategoryA());
    }

    public static Category getMockedCategoryAlphabet() {
        return Category.builder()
              .id(1L)
              .name("Alfabet")
              .imageId(1)
              .isExpanded(false)
              .isFavorite(true)
              .parent(null)
              .section(getMockedSection1())
              .build();
    }

    public static Category getMockedCategoryNumbers() {
        return Category.builder()
              .id(2L)
              .name("Numere")
              .imageId(2)
              .isExpanded(false)
              .isFavorite(false)
              .parent(null)
              .section(getMockedSection1())
              .build();
    }

    public static Category getMockedCategoryColors() {
        return Category.builder()
              .id(3L)
              .name("Culori")
              .imageId(3)
              .isExpanded(false)
              .isFavorite(true)
              .parent(null)
              .section(getMockedSection1())
              .build();
    }

    public static Category getMockedCategoryA() {
        return Category.builder()
              .id(10L)
              .name("a")
              .imageId(10)
              .isExpanded(false)
              .isFavorite(true)
              .parent(getMockedCategoryAlphabet())
              .section(getMockedSection1())
              .build();
    }

    public static Category getMockedCategoryB() {
        return Category.builder()
              .id(13L)
              .name("b")
              .imageId(13)
              .isExpanded(false)
              .isFavorite(false)
              .parent(getMockedCategoryAlphabet())
              .section(getMockedSection1())
              .build();
    }

    public static Category getMockedCategoryOne() {
        return Category.builder()
              .id(41L)
              .name("unu")
              .imageId(41)
              .isExpanded(false)
              .isFavorite(false)
              .parent(getMockedCategoryNumbers())
              .section(getMockedSection1())
              .build();
    }

    public static Category getMockedCategoryTwo() {
        return Category.builder()
              .id(42L)
              .name("doi")
              .imageId(42)
              .isExpanded(false)
              .isFavorite(true)
              .parent(getMockedCategoryNumbers())
              .section(getMockedSection1())
              .build();
    }

    public static Category getMockedCategoryRed() {
        return Category.builder()
              .id(51L)
              .name("rosu")
              .imageId(51)
              .isExpanded(false)
              .isFavorite(false)
              .parent(getMockedCategoryColors())
              .section(getMockedSection1())
              .build();
    }

    public static Category getMockedCategoryOrange() {
        return Category.builder()
              .id(52L)
              .name("portocaliu")
              .imageId(52)
              .isExpanded(false)
              .isFavorite(true)
              .parent(getMockedCategoryColors())
              .section(getMockedSection1())
              .build();
    }
}
