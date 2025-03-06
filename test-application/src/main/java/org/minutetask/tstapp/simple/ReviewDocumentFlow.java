package org.minutetask.tstapp.simple;

import java.util.UUID;

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
import org.minutetask.casecore.annotation.IdRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReviewDocumentFlow implements DocumentFlow {
    @Autowired
    private UseCaseManager useCaseManager;

    @Override
    public void run(Long id) {
        DocumentCase documentCase = useCaseManager.getUseCase(id, DocumentCase.class);
        log.info("started case [caseId={}]", documentCase.getCaseId());
        useCaseManager.saveUseCase(documentCase);
        //
        documentCase.setPageUrl("page-" + UUID.randomUUID().toString());
        useCaseManager.saveUseCase(documentCase);
        //
        log.info("sending internal-publish request [documentId={}, pageUrl={}]", //
                documentCase.getDocumentId(), documentCase.getPageUrl());
        // send internal publish request
    }

    @Override
    public void pageUploaded(@IdRef String pageUrlAsIdRef, int pageState, String message) {
        DocumentCase documentCase = useCaseManager.getUseCase(pageUrlAsIdRef, DocumentCase.class);
        log.info("received internal-publish response [documentId={}, pageUrl={}, pageState={}]", //
                documentCase.getDocumentId(), documentCase.getPageUrl(), pageState);
        //
        log.info("sending internal-review request [documentId={}, pageUrl={}]", //
                documentCase.getDocumentId(), documentCase.getPageUrl());
        // send review request
    }

    @Override
    public void reviewFinished(@IdRef String pageUrlAsIdRef, int score, String message) {
        DocumentCase documentCase = useCaseManager.getUseCase(pageUrlAsIdRef, DocumentCase.class);
        log.info("received internal-review response [documentId={}, pageUrl={}, score={}]", //
                documentCase.getDocumentId(), documentCase.getPageUrl(), score);
        //
        documentCase.setClosed(true);
        useCaseManager.saveUseCase(documentCase);
        //
        log.info("finished case [caseId={}]", documentCase.getCaseId());
    }
}
