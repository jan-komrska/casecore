package org.minutetask.tstapp.process;

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

import org.minutetask.casecore.UseCaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BuildFlowImpl implements BuildFlow {
    private boolean ok = false;

    @Autowired
    private UseCaseManager useCaseManager;

    @Lazy
    @Autowired
    private BuildFlow buildFlow;

    @Override
    public void run(Long caseId) {
        BuildCase buildCase = useCaseManager.getUseCase(caseId, BuildCase.class);
        log.info("build started [caseId={}, projectId={}]", buildCase.getCaseId(), buildCase.getProjectId());
        //
        log.info("sending compileProject request [caseId={}, projectId={}]", buildCase.getCaseId(), buildCase.getProjectId());
        buildFlow.compileProject(caseId);
    }

    @Override
    public void compileProject(Long caseId) {
        BuildCase buildCase = useCaseManager.getUseCase(caseId, BuildCase.class);
        log.info("compiling project [caseId={}, projectId={}]", buildCase.getCaseId(), buildCase.getProjectId());
        //
        log.info("sending packageProject request [caseId={}, projectId={}]", buildCase.getCaseId(), buildCase.getProjectId());
        buildFlow.packageProject(caseId);
    }

    @Override
    public void packageProject(Long caseId) {
        if (!ok) {
            ok = true;
            throw new IllegalStateException();
        }
        //
        BuildCase buildCase = useCaseManager.getUseCase(caseId, BuildCase.class);
        log.info("package project [caseId={}, projectId={}]", buildCase.getCaseId(), buildCase.getProjectId());
        //
        log.info("sending deployProject request [caseId={}, projectId={}]", buildCase.getCaseId(), buildCase.getProjectId());
        buildFlow.deployProject(caseId);
    }

    @Override
    public void deployProject(Long caseId) {
        BuildCase buildCase = useCaseManager.getUseCase(caseId, BuildCase.class);
        log.info("deploying project [caseId={}, projectId={}]", buildCase.getCaseId(), buildCase.getProjectId());
        //
        buildCase.setClosed(true);
        useCaseManager.saveUseCase(buildCase);
        //
        log.info("build finished [caseId={}, projectId={}]", buildCase.getCaseId(), buildCase.getProjectId());
    }
}
