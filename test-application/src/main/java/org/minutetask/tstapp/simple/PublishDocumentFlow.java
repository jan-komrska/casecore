package org.minutetask.tstapp.simple;

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

import java.util.UUID;

import org.minutetask.casecore.UseCaseManager;
import org.minutetask.casecore.annotation.IdRef;
import org.minutetask.casecore.annotation.ImplementationRef;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ImplementationRef
public class PublishDocumentFlow implements DocumentFlow, UploadServiceCallback {
    @Autowired
    private UseCaseManager useCaseManager;

    @Autowired
    private UploadService uploadService;

    @Override
    public void run(Long caseId) {
        DocumentCase documentCase = useCaseManager.getUseCase(caseId, DocumentCase.class);
        log.info("started case [caseId={}]", documentCase.getCaseId());
        //
        documentCase.setPageUrl("page-" + UUID.randomUUID().toString());
        useCaseManager.saveUseCase(documentCase);
        //
        log.info("sending publish request [documentId={}, pageUrl={}]", //
                documentCase.getDocumentId(), documentCase.getPageUrl());
        uploadService.uploadPage(documentCase.getPageUrl(), "...");
    }

    @Override
    public void pageUploaded(@IdRef String pageUrlAsCaseId, int pageState, String message) {
        DocumentCase documentCase = useCaseManager.getUseCase(pageUrlAsCaseId, DocumentCase.class);
        log.info("received publish response [documentId={}, pageUrl={}, pageState={}]", //
                documentCase.getDocumentId(), documentCase.getPageUrl(), pageState);
        //
        documentCase.setClosed(true);
        useCaseManager.saveUseCase(documentCase);
        //
        log.info("finished case [caseId={}]", documentCase.getCaseId());
    }
}
