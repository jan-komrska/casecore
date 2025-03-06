package org.minutetask.tstapp;

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

import org.minutetask.casecore.CaseCoreScan;
import org.minutetask.casecore.CoreCaseConfiguration;
import org.minutetask.casecore.UseCaseManager;
import org.minutetask.tstapp.simple.DocumentCase;
import org.minutetask.tstapp.simple.DocumentFlow;
import org.minutetask.tstapp.simple.PublishDocumentFlow;
import org.minutetask.tstapp.simple.ReviewDocumentFlow;
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
    private DocumentFlow documentFlow = null;

    public void publishDocument() {
        DocumentCase publishCase = new DocumentCase();
        publishCase.setDocumentId(1001l);
        publishCase.setFlow(PublishDocumentFlow.class);
        publishCase = useCaseManager.saveUseCase(publishCase);
        log.info("document case: {}", publishCase.toString());
        //
        documentFlow.run(publishCase.getCaseId());
        //
        publishCase = useCaseManager.refreshUseCase(publishCase);
        //
        documentFlow.pageUploaded(publishCase.getPageUrl(), 0, "OK");
    }

    public void reviewDocument() {
        DocumentCase reviewCase = new DocumentCase();
        reviewCase.setDocumentId(1001l);
        reviewCase.setReviewDocumentId(1001l);
        reviewCase.setFlow(ReviewDocumentFlow.class);
        reviewCase = useCaseManager.saveUseCase(reviewCase);
        log.info("document case: {}", reviewCase.toString());
        //
        documentFlow.run(reviewCase.getCaseId());
        //
        reviewCase = useCaseManager.refreshUseCase(reviewCase);
        //
        documentFlow.reviewFinished(reviewCase.getReviewDocumentId(), 10, "OK");
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            log.info("Let's start flow:");
            //
            publishDocument();
            reviewDocument();
            //
            log.info("OK");
        };
    }
}
