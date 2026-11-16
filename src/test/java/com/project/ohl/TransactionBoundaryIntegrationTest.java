package com.project.ohl;

import com.project.ohl.application.port.in.CategoryService;
import com.project.ohl.application.port.out.CategoryRepositoryPort;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@SpringBootTest
class TransactionBoundaryIntegrationTest extends DatabaseIntegrationTest {
    @Autowired
    CategoryService service;

    @MockitoSpyBean
    CategoryRepositoryPort categories;

    @Test void queriesRunInReadOnlyUseCaseTransactions() {
        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            assertThat(TransactionSynchronizationManager.isCurrentTransactionReadOnly()).isTrue();
            return invocation.callRealMethod();
        }).when(categories).findAll();

        assertThat(service.getAllCategories()).hasSize(2);
    }

    @Test void aFailureAfterFlushingStillRollsBackTheCompleteUseCase() {
        var category = service.getCategoryById(TestIds.CATEGORY_CHILD);

        String originalName = category.getName();

        category.setName("Changed name");

        doAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
            assertThat(TransactionSynchronizationManager.isCurrentTransactionReadOnly()).isFalse();
            invocation.callRealMethod();
            throw new InvalidRequestException("Fail after the persistence adapter flushed");
        }).when(categories).update(any(Category.class));

        assertThatThrownBy(() -> service.updateCategoryById(category, category.getId()))
                .isInstanceOf(InvalidRequestException.class);
        assertThat(service.getCategoryById(category.getId()).getName()).isEqualTo(originalName);
    }
}
