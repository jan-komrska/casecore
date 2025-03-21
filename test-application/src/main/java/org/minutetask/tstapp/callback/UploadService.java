package org.minutetask.tstapp.callback;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UploadService {
    @Autowired
    private UploadServiceCallback callback;

    @Async
    public void uploadPage(String pageUrl, String pageContent) {
        log.info("upload of page {} started", pageUrl);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            // DO NOTHING
        }
        log.info("upload of page {} finished", pageUrl);
        //
        callback.pageUploaded(pageUrl, 0, "OK");
    }
}
