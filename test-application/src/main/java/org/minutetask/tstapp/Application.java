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

import org.minutetask.casecore.CoreCaseConfiguration;
import org.minutetask.casecore.jpa.entity.UseCaseEntity;
import org.minutetask.casecore.service.api.UseCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Import(CoreCaseConfiguration.class)
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    //

    @Autowired
    private UseCaseService useCaseService = null;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            System.out.println("Let's start flow:");
            //
            CreateEncounterCase createEncounter1 = new CreateEncounterCase();
            createEncounter1.setPersonId("personId-1");
            createEncounter1.setEncounterId("encounterId-1");
            createEncounter1.setAction("action-1");
            UseCaseEntity useCaseEntity1 = useCaseService.createUseCase(createEncounter1);
            //
            CreateEncounterCase createEncounter2 = useCaseService.getUseCaseData(useCaseEntity1, CreateEncounterCase.class);
            createEncounter2.setId(null);
            createEncounter2.setTcn("tcn-2.1");
            createEncounter2.setPersonId("personId-2.1");
            createEncounter2.setEncounterId("encounterId-2.1");
            log.info("record: " + createEncounter2);
            UseCaseEntity useCaseEntity2 = useCaseService.createUseCase(createEncounter2);
            createEncounter2.setId(useCaseEntity2.getId());
            createEncounter2.setTcn("tcn-2.2");
            createEncounter2.setAction("action-2.2");
            log.info("record: " + createEncounter2);
            useCaseEntity2 = useCaseService.updateUseCase(useCaseEntity2, createEncounter2);
            //
            CreateEncounterCase createEncounter3 = useCaseService.getUseCaseData(useCaseEntity1, CreateEncounterCase.class);
            createEncounter3.setId(null);
            createEncounter3.setTcn("tcn-3.1");
            createEncounter3.setPersonId("personId-3.1");
            createEncounter3.setEncounterId("encounterId-3.1");
            UseCaseEntity useCaseEntity3 = useCaseService.createUseCase(createEncounter3);
            useCaseService.finishUseCase(useCaseEntity3);
            //
            CreateEncounterCase createEncounter4 = useCaseService.getUseCaseData(useCaseEntity1, CreateEncounterCase.class);
            createEncounter4.setId(null);
            createEncounter4.setPersonId("personId-4.1");
            createEncounter4.setEncounterId("encounterId-4.1");
            UseCaseEntity useCaseEntity4 = useCaseService.createUseCase(createEncounter4);
            useCaseService.finishUseCase(useCaseEntity4);
            useCaseService.deleteUseCase(useCaseEntity4.getId());
            //
            System.out.println("OK");
        };
    }
}
