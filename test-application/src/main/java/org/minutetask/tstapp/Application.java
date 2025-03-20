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
import org.minutetask.tstapp.helloworld.EmperorFlow;
import org.minutetask.tstapp.helloworld.KingFlow;
import org.minutetask.tstapp.helloworld.PersonCase;
import org.minutetask.tstapp.helloworld.PersonFlow;
import org.minutetask.tstapp.process.BuildCase;
import org.minutetask.tstapp.process.BuildFlow;
import org.minutetask.tstapp.process.BuildFlowImpl;
import org.minutetask.tstapp.simple.DocumentCase;
import org.minutetask.tstapp.simple.DocumentFlow;
import org.minutetask.tstapp.simple.PublishDocumentFlow;
import org.minutetask.tstapp.simple.ReviewDocumentFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Import(CoreCaseConfiguration.class)
@CaseCoreScan("org.minutetask.tstapp")
@EnableAsync
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    //

    @Lazy
    @Autowired
    private Application self;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private UseCaseManager useCaseManager;

    @Autowired
    private PersonFlow greetingService;

    @Autowired
    private DocumentFlow documentFlow;

    @Autowired
    private BuildFlow buildFlow;

    @Transactional
    public void helloWorld() {
        PersonCase caesar = useCaseManager.saveUseCase(new PersonCase(null, "Caesar", EmperorFlow.class));
        PersonCase charlemagne = useCaseManager.saveUseCase(new PersonCase(null, "Charlemagne", KingFlow.class));
        //
        greetingService.greeting("Caesar");
        greetingService.greeting("Charlemagne");
        //
        useCaseManager.deleteUseCase(caesar);
        useCaseManager.deleteUseCase(charlemagne);
    }

    @Transactional
    public void publishDocument() {
        DocumentCase publishCase = new DocumentCase();
        publishCase.setDocumentId(1001l);
        publishCase.setFlow(PublishDocumentFlow.class);
        publishCase = useCaseManager.saveUseCase(publishCase);
        log.info("document case: {}", publishCase.toString());
        //
        documentFlow.run(publishCase.getCaseId());
    }

    @Transactional
    public void reviewDocument() {
        DocumentCase reviewCase = new DocumentCase();
        reviewCase.setDocumentId(1002l);
        reviewCase.setFlow(ReviewDocumentFlow.class);
        reviewCase = useCaseManager.saveUseCase(reviewCase);
        log.info("document case: {}", reviewCase.toString());
        //
        documentFlow.run(reviewCase.getCaseId());
    }

    @Transactional
    public void buildProject() {
        BuildCase buildCase = new BuildCase();
        buildCase.setProjectId(2001l);
        buildCase.setFlow(BuildFlowImpl.class);
        buildCase = useCaseManager.saveUseCase(buildCase);
        //
        buildFlow.run(buildCase.getCaseId(), null);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            log.info("---");
            self.helloWorld();
            Thread.sleep(10000);
            //
            log.info("---");
            self.publishDocument();
            Thread.sleep(10000);
            //
            log.info("---");
            self.reviewDocument();
            Thread.sleep(10000);
            //
            log.info("---");
            self.buildProject();
            Thread.sleep(40000);
            //
            log.info("---");
            applicationContext.close();
        };
    }
}
