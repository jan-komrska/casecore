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
import org.minutetask.casecore.UseCaseManager;
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
    private UseCaseManager useCaseManager = null;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            System.out.println("Let's start flow:");
            //
            CreateEncounterCase createEncounter1 = new CreateEncounterCase();
            createEncounter1.setTcn("tcn-1.1");
            createEncounter1.setPersonId("personId-1");
            createEncounter1.setEncounterId("encounterId-1");
            createEncounter1.setAction("action-1");
            useCaseManager.saveUseCase(createEncounter1);
            log.info("request-1: " + createEncounter1.toString());
            //
            CreateEncounterCase createEncounter2 = useCaseManager.getUseCase(createEncounter1.getId(), CreateEncounterCase.class);
            createEncounter2.setId(null);
            createEncounter2.setTcn("tcn-2.1");
            createEncounter2.setPersonId("personId-2.1");
            createEncounter2.setEncounterId("encounterId-2.1");
            useCaseManager.saveUseCase(createEncounter2);
            createEncounter2.setTcn("tcn-2.2");
            createEncounter2.setAction("action-2.2");
            useCaseManager.saveUseCase(createEncounter2);
            log.info("request-2: " + createEncounter2.toString());
            //
            CreateEncounterCase createEncounter3 = useCaseManager.getUseCase(createEncounter1.getId(), CreateEncounterCase.class);
            createEncounter3.setId(null);
            createEncounter3.setClosed(true);
            createEncounter3.setTcn("tcn-3.1");
            createEncounter3.setPersonId("personId-3.1");
            createEncounter3.setEncounterId("encounterId-3.1");
            useCaseManager.saveUseCase(createEncounter3);
            log.info("request-3: " + createEncounter3.toString());
            //
            CreateEncounterCase createEncounter4 = useCaseManager.getUseCase(createEncounter1.getId(), CreateEncounterCase.class);
            createEncounter4.setId(null);
            createEncounter4.setTcn(null);
            createEncounter4.setClosed(true);
            createEncounter4.setPersonId("personId-4.1");
            createEncounter4.setEncounterId("encounterId-4.1");
            useCaseManager.saveUseCase(createEncounter4);
            useCaseManager.deleteUseCase(createEncounter4.getId());
            log.info("request-4: " + createEncounter4.toString());
            //
            System.out.println("OK");
        };
    }
}
