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

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.minutetask.casecore.ActionContext;
import org.minutetask.casecore.UseCaseManager;
import org.minutetask.casecore.annotation.IdRef;
import org.minutetask.casecore.exception.BadRequestException;
import org.minutetask.casecore.exception.NotFoundException;
import org.minutetask.casecore.exception.UnexpectedException;
import org.minutetask.casecore.jpa.entity.UseCaseEntity;
import org.minutetask.casecore.service.api.UseCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UseCaseManagerImpl implements UseCaseManager {
    @Autowired
    private UseCaseService useCaseService;

    @Qualifier("org.minutetask.casecore.CoreCaseConfiguration::conversionServiceBean")
    @Autowired
    private ConversionService conversionService;

    //

    private Long getUseCaseId(Object useCase) {
        List<Field> idFields = FieldUtils.getFieldsListWithAnnotation(useCase.getClass(), IdRef.class);
        if (CollectionUtils.isEmpty(idFields)) {
            throw new BadRequestException();
        }
        //
        try {
            Object idValue = FieldUtils.readField(idFields.get(0), useCase, true);
            return conversionService.convert(idValue, Long.class);
        } catch (IllegalAccessException ex) {
            throw new UnexpectedException(ex);
        }
    }

    private void setUseCaseId(Object useCase, Long idValue) {
        List<Field> idFields = FieldUtils.getFieldsListWithAnnotation(useCase.getClass(), IdRef.class);
        if (CollectionUtils.isEmpty(idFields)) {
            throw new BadRequestException();
        }
        //
        try {
            for (Field idField : idFields) {
                FieldUtils.writeField(idField, useCase, idValue, true);
            }
        } catch (IllegalAccessException ex) {
            throw new UnexpectedException(ex);
        }
    }

    //

    @Override
    public <UseCase> UseCase getUseCase(Long id, Class<UseCase> useCaseClass) {
        UseCaseEntity useCaseEntity = useCaseService.getUseCase(id);
        if (useCaseEntity != null) {
            return useCaseService.getUseCaseData(useCaseEntity, useCaseClass);
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    public <UseCase> UseCase getUseCase(ActionContext actionContext, Class<UseCase> useCaseClass) {
        return getUseCase(actionContext.getUseCaseId(), useCaseClass);
    }

    @Override
    public <UseCase> UseCase getUseCase(String keyType, String keyValue, Class<UseCase> useCaseClass) {
        UseCaseEntity useCaseEntity = useCaseService.getUseCase(keyType, keyValue);
        if (useCaseEntity != null) {
            return useCaseService.getUseCaseData(useCaseEntity, useCaseClass);
        } else {
            throw new NotFoundException();
        }
    }

    @Override
    @Transactional
    public void saveUseCase(Object useCase) {
        Long useCaseId = getUseCaseId(useCase);
        if (useCaseId != null) {
            UseCaseEntity useCaseEntity = useCaseService.getUseCase(useCaseId);
            useCaseService.updateUseCaseData(useCaseEntity, useCase);
            useCaseService.updateUseCase(useCaseEntity);
        } else {
            UseCaseEntity useCaseEntity = useCaseService.createUseCase();
            setUseCaseId(useCase, useCaseEntity.getId());
            useCaseService.updateUseCaseData(useCaseEntity, useCase);
            useCaseService.updateUseCase(useCaseEntity);
        }
    }

    @Override
    @Transactional
    public void deleteUseCase(Long id) {
        useCaseService.deleteUseCase(id);
    }
}
