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

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.minutetask.casecore.exception.UnexpectedException;
import org.minutetask.casecore.jpa.entity.UseCaseActionData;
import org.minutetask.casecore.jpa.entity.UseCaseActionEntity;
import org.minutetask.casecore.jpa.entity.UseCaseActionSource;
import org.minutetask.casecore.jpa.entity.UseCaseEntity;
import org.minutetask.casecore.jpa.repository.UseCaseActionRepository;
import org.minutetask.casecore.service.api.LiteralService;
import org.minutetask.casecore.service.api.UseCaseActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional(readOnly = true)
@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UseCaseActionServiceImpl implements UseCaseActionService {
    @Autowired
    private LiteralService literalService;

    @Autowired
    private UseCaseActionRepository useCaseActionRepository;

    @Qualifier("org.minutetask.casecore.CoreCaseConfiguration::objectMapper")
    @Autowired
    private ObjectMapper objectMapper;

    //

    private void loadActionSource(UseCaseActionEntity action) {
        if (action == null) {
            return;
        }
        if (StringUtils.isEmpty(action.getDataAsJson())) {
            action.setSource(new UseCaseActionSource());
            return;
        }
        //
        UseCaseActionData actionData;
        try {
            actionData = objectMapper.readValue(action.getDataAsJson(), UseCaseActionData.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
        //
        Class<?> serviceClass = literalService.getClassFromId(actionData.getServiceClassId());
        Method method = null;
        Object[] parameters = null;
        Class<?> lastExceptionClass = literalService.getClassFromId(actionData.getLastExceptionClassId());
        String lastExceptionMessage = actionData.getLastExceptionMessage();
        int retryCount = actionData.getRetryCount();
        //
        String methodName = actionData.getMethodName();
        if (StringUtils.isNotEmpty(methodName)) {
            Long methodClassId = actionData.getMethodClassId();
            Class<?> methodClass = literalService.getClassFromId(methodClassId);
            Class<?>[] parameterClasses = Arrays.stream(ArrayUtils.nullToEmpty(actionData.getParameterClassIds())). //
                    map(literalService::getClassFromId).toArray(Class<?>[]::new);
            parameters = new Object[parameterClasses.length];
            //
            try {
                method = methodClass.getDeclaredMethod(methodName, parameterClasses);
            } catch (NoSuchMethodException ex) {
                throw new UnexpectedException(ex);
            }
            //
            Object[] actionParameters = ArrayUtils.nullToEmpty(actionData.getParameters());
            int maxIndex = Math.min(parameterClasses.length, actionParameters.length);
            for (int index = 0; index < maxIndex; index++) {
                parameters[index] = objectMapper.convertValue(actionParameters[index], parameterClasses[index]);
            }
        }
        //
        UseCaseActionSource actionSource = new UseCaseActionSource();
        actionSource.setServiceClass(serviceClass);
        actionSource.setMethod(method);
        actionSource.setParameters(parameters);
        actionSource.setLastExceptionClass(lastExceptionClass);
        actionSource.setLastExceptionMessage(lastExceptionMessage);
        actionSource.setRetryCount(retryCount);
        //
        action.setSource(actionSource);
    }

    private void saveActionSource(UseCaseActionEntity action) {
        if (action == null) {
            return;
        }
        //
        UseCaseActionSource actionSource = action.getSource();
        if (actionSource == null || actionSource.isEmpty()) {
            action.setDataAsJson(null);
            return;
        }
        //
        Long serviceClassId = literalService.getIdFromClass(actionSource.getServiceClass());
        Long methodClassId = null;
        String methodName = null;
        Long[] parameterClassIds = null;
        Object[] parameters = actionSource.getParameters();
        Long lastExceptionClassId = literalService.getIdFromClass(actionSource.getLastExceptionClass());
        String lastExceptionMessage = actionSource.getLastExceptionMessage();
        int retryCount = actionSource.getRetryCount();
        //
        Method method = actionSource.getMethod();
        if (method != null) {
            methodClassId = literalService.getIdFromClass(method.getDeclaringClass());
            methodName = method.getName();
            parameterClassIds = Arrays.stream(ArrayUtils.nullToEmpty(method.getParameterTypes())) //
                    .map(literalService::getIdFromClass).toArray(Long[]::new);
        }
        //
        UseCaseActionData actionData = new UseCaseActionData();
        actionData.setServiceClassId(serviceClassId);
        actionData.setMethodClassId(methodClassId);
        actionData.setMethodName(methodName);
        actionData.setParameterClassIds(parameterClassIds);
        actionData.setParameters(parameters);
        actionData.setLastExceptionClassId(lastExceptionClassId);
        actionData.setLastExceptionMessage(lastExceptionMessage);
        actionData.setRetryCount(retryCount);
        //
        String dataAsJson;
        try {
            dataAsJson = objectMapper.writeValueAsString(actionData);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
        //
        action.setDataAsJson(dataAsJson);
    }

    //

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public UseCaseActionEntity newAction(UseCaseEntity useCase, boolean active) {
        UseCaseActionEntity action = new UseCaseActionEntity();
        action.setUseCase(useCase);
        action.setActive(active);
        action.setClosed(false);
        action.setCreatedDate(LocalDateTime.now());
        //
        loadActionSource(action);
        return action;
    }

    @Override
    public UseCaseActionEntity getAction(Long id) {
        UseCaseActionEntity action = useCaseActionRepository.findById(id).orElse(null);
        //
        loadActionSource(action);
        return action;
    }

    @Override
    @Transactional
    public UseCaseActionEntity persistAction(UseCaseActionEntity action) {
        if (action.getId() == null) {
            saveActionSource(action);
            return useCaseActionRepository.save(action);
        } else {
            return action;
        }
    }

    @Override
    @Transactional
    public UseCaseActionEntity saveAction(UseCaseActionEntity action) {
        if (action.getId() != null) {
            saveActionSource(action);
            return useCaseActionRepository.save(action);
        } else {
            return action;
        }
    }

    @Override
    @Transactional
    public UseCaseActionEntity deleteAction(UseCaseActionEntity action) {
        if (action.getId() != null) {
            useCaseActionRepository.delete(action);
            action.setId(null);
        }
        return action;
    }

    public List<UseCaseActionEntity> findScheduledActions(LocalDateTime targetDate) {
        List<UseCaseActionEntity> scheduledActions = ListUtils.emptyIfNull(useCaseActionRepository.findScheduledActions(targetDate));
        for (UseCaseActionEntity scheduledAction : scheduledActions) {
            loadActionSource(scheduledAction);
        }
        return scheduledActions;
    }

    //

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Class<?> getServiceClass(UseCaseActionEntity action) {
        return action.getSource().getServiceClass();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void setServiceClass(UseCaseActionEntity action, Class<?> serviceClass) {
        action.getSource().setServiceClass(serviceClass);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Method getMethod(UseCaseActionEntity action) {
        return action.getSource().getMethod();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void setMethod(UseCaseActionEntity action, Method method) {
        action.getSource().setMethod(method);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Object[] getArgs(UseCaseActionEntity action) {
        return action.getSource().getParameters();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void setArgs(UseCaseActionEntity action, Object[] args) {
        action.getSource().setParameters(args);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public boolean isPersistent(UseCaseActionEntity action) {
        return action.getSource().isPersistent();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public boolean isAsync(UseCaseActionEntity action) {
        return action.getSource().isAsync();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public String getTaskExecutor(UseCaseActionEntity action) {
        return action.getSource().getTaskExecutor();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Class<?> getLastExceptionClass(UseCaseActionEntity action) {
        return action.getSource().getLastExceptionClass();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public String getLastExceptionMessage(UseCaseActionEntity action) {
        return action.getSource().getLastExceptionMessage();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void setLastException(UseCaseActionEntity action, Throwable throwable) {
        action.getSource().setLastException(throwable);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public int getRetryCount(UseCaseActionEntity action) {
        return action.getSource().getRetryCount();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void incRetryCount(UseCaseActionEntity action) {
        action.getSource().incRetryCount();
    }
}
