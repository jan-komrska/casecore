package org.minutetask.casecore.service.impl;

/*-
 * ========================LICENSE_START=================================
 * org.minutetask.casecore:casecore
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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Service;

@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class AsyncTaskExecutorProvider {
    private static final String DEFAULT_TASK_EXECUTOR = "taskExecutor";

    private volatile String defaultExecutorName = null;
    private volatile SimpleAsyncTaskExecutor internalExecutor = null;

    @Autowired
    private ApplicationContext applicationContext;

    //

    public AsyncTaskExecutor getAsyncTaskExecutor(String executorName) {
        if (StringUtils.isNotEmpty(executorName)) {
            return applicationContext.getBean(executorName, AsyncTaskExecutor.class);
        } else if (StringUtils.isNotEmpty(defaultExecutorName)) {
            return applicationContext.getBean(defaultExecutorName, AsyncTaskExecutor.class);
        } else if (internalExecutor != null) {
            return internalExecutor;
        } else {
            synchronized (this) {
                if (StringUtils.isEmpty(defaultExecutorName) && (internalExecutor == null)) {
                    String[] executorNames = applicationContext.getBeanNamesForType(AsyncTaskExecutor.class);
                    executorNames = ArrayUtils.nullToEmpty(executorNames);
                    //
                    if (executorNames.length == 1) {
                        defaultExecutorName = executorNames[0];
                    } else if (ArrayUtils.contains(executorNames, DEFAULT_TASK_EXECUTOR)) {
                        defaultExecutorName = DEFAULT_TASK_EXECUTOR;
                    } else {
                        internalExecutor = new SimpleAsyncTaskExecutor();
                    }
                }
            }
            //
            return getAsyncTaskExecutor(executorName);
        }
    }
}
