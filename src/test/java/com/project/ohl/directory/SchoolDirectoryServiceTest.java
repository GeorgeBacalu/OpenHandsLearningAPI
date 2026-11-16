package com.project.ohl.directory;

import com.project.ohl.application.port.out.SchoolDirectoryPort;
import com.project.ohl.application.service.SchoolDirectoryServiceImpl;
import com.project.ohl.domain.exception.InvalidRequestException;
import com.project.ohl.domain.model.SchoolDirectory;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class SchoolDirectoryServiceTest {
    private final SchoolDirectoryPort directory = mock(SchoolDirectoryPort.class);
    private final SchoolDirectoryServiceImpl service = new SchoolDirectoryServiceImpl(directory);

    @Test void rejectsInvalidPaginationBeforeAccessingPersistence() {
        var parent = UUID.randomUUID();

        assertThatThrownBy(() -> service.cities(parent, "", -1, 50)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.schools(parent, "", 0, 0)).isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> service.schools(parent, "", 0, 201)).isInstanceOf(InvalidRequestException.class);

        verifyNoInteractions(directory);
    }

    @Test void rejectsIncompleteImportsBeforeWritingAnything() {
        assertThatThrownBy(() -> service.importDirectory(new SchoolDirectory(List.of(), List.of(), List.of())))
                .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(directory);
    }

    @Test void searchesOnlyTheRequestedParentAndPage() {
        var parent = UUID.randomUUID();

        service.cities(parent, "Cluj", 20, 200);
        service.schools(parent, "School", 0, 1);

        verify(directory).cities(parent, "Cluj", 20, 200);
        verify(directory).schools(parent, "School", 0, 1);
    }
}
