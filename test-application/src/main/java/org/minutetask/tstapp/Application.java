package org.minutetask.tstapp;

import org.minutetask.casecore.CaseCoreScan;

/*-
 * ========================LICENSE_START=================================
 * casecore-test-application
 * %%
 * Copyright (C) 2025 Jan Komrska
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

import org.minutetask.casecore.CoreCaseConfiguration;
import org.minutetask.casecore.UseCaseManager;
import org.minutetask.tstapp.simple.DocumentCase;
import org.minutetask.tstapp.simple.DocumentContract;
import org.minutetask.tstapp.simple.PublishDocumentImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Import(CoreCaseConfiguration.class)
@CaseCoreScan("org.minutetask.tstapp")
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    //

    @Autowired
    private UseCaseManager useCaseManager = null;

    @Autowired
    private DocumentContract documentContract = null;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            log.info("Let's start flow:");
            //
            DocumentCase documentCase = new DocumentCase();
            documentCase.setDocumentId("document-1");
            documentCase.setContract(PublishDocumentImpl.class);
            documentCase = useCaseManager.saveUseCase(documentCase);
            log.info("document case: {}", documentCase.toString());
            //
            documentContract.run(documentCase.getCaseId());
            //
            documentCase = useCaseManager.refreshUseCase(documentCase);
            log.info("document case: {}", documentCase.toString());
            //
            documentContract.pageUploaded(documentCase.getPageUrl());
            //
            log.info("OK");
        };
    }
}
