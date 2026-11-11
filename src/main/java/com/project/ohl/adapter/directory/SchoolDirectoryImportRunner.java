package com.project.ohl.adapter.directory;

import com.project.ohl.application.port.out.SchoolDirectoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.school-directory.import", havingValue = "true")
public class SchoolDirectoryImportRunner implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(SchoolDirectoryImportRunner.class);
    private final SchoolDirectoryPort directory;

    public SchoolDirectoryImportRunner(SchoolDirectoryPort directory) {
        this.directory = directory;
    }

    @Override
    public void run(ApplicationArguments arguments) throws Exception {
        var data = new GovernmentSchoolDirectoryClient().download();
        directory.importDirectory(data);
        LOG.info("Imported Romania directory: {} counties, {} localities, {} schools",
                data.counties().size(), data.cities().size(), data.schools().size());
    }
}
