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
        Method method = null;
        Object[] parameters = null;
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
        actionSource.setServiceClass(literalService.getClassFromId(actionData.getServiceClassId()));
        actionSource.setMethod(method);
        actionSource.setParameters(parameters);
        actionSource.setLastExceptionClass(literalService.getClassFromId(actionData.getLastExceptionClassId()));
        actionSource.setLastExceptionMessage(actionData.getLastExceptionMessage());
        actionSource.setRetryCount(actionData.getRetryCount());
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
        Long methodClassId = null;
        String methodName = null;
        Long[] parameterClassIds = null;
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
        actionData.setServiceClassId(literalService.getIdFromClass(actionSource.getServiceClass()));
        actionData.setMethodClassId(methodClassId);
        actionData.setMethodName(methodName);
        actionData.setParameterClassIds(parameterClassIds);
        actionData.setParameters(actionSource.getParameters());
        actionData.setLastExceptionClassId(literalService.getIdFromClass(actionSource.getLastExceptionClass()));
        actionData.setLastExceptionMessage(actionSource.getLastExceptionMessage());
        actionData.setRetryCount(actionSource.getRetryCount());
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
    @Transactional(readOnly = true)
    public UseCaseActionEntity getAction(Long id) {
        UseCaseActionEntity action = useCaseActionRepository.findById(id).orElse(null);
        //
        loadActionSource(action);
        return action;
    }

    @Override
    @Transactional
    public UseCaseActionEntity lockAction(UseCaseActionEntity action) {
        return useCaseActionRepository.getLockedEntityById(action.getId());
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

    @Transactional(readOnly = true)
    public List<UseCaseActionEntity> findScheduledActions(LocalDateTime targetDate) {
        List<UseCaseActionEntity> scheduledActions = ListUtils.emptyIfNull(useCaseActionRepository.findScheduledActions(targetDate));
        for (UseCaseActionEntity scheduledAction : scheduledActions) {
            loadActionSource(scheduledAction);
        }
        return scheduledActions;
    }
}
