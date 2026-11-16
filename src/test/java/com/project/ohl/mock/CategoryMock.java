package com.project.ohl.mock;

import com.project.ohl.domain.model.Category;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.project.ohl.TestIds.id;

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
              .id(id(1))
              .name("Alfabet")
              .parent(null)
              .build();
    }

    public static Category getMockedCategoryNumbers() {
        return Category.builder()
              .id(id(2))
              .name("Numere")
              .parent(null)
              .build();
    }

    public static Category getMockedCategoryColors() {
        return Category.builder()
              .id(id(3))
              .name("Culori")
              .parent(null)
              .build();
    }

    public static Category getMockedCategoryA() {
        return Category.builder()
              .id(id(10))
              .name("a")
              .parent(getMockedCategoryAlphabet())
              .build();
    }

    public static Category getMockedCategoryB() {
        return Category.builder()
              .id(id(13))
              .name("b")
              .parent(getMockedCategoryAlphabet())
              .build();
    }

    public static Category getMockedCategoryOne() {
        return Category.builder()
              .id(id(41))
              .name("unu")
              .parent(getMockedCategoryNumbers())
              .build();
    }

    public static Category getMockedCategoryTwo() {
        return Category.builder()
              .id(id(42))
              .name("doi")
              .parent(getMockedCategoryNumbers())
              .build();
    }

    public static Category getMockedCategoryRed() {
        return Category.builder()
              .id(id(51))
              .name("rosu")
              .parent(getMockedCategoryColors())
              .build();
    }

    public static Category getMockedCategoryOrange() {
        return Category.builder()
              .id(id(52))
              .name("portocaliu")
              .parent(getMockedCategoryColors())
              .build();
    }
}
