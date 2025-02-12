package org.minutetask.casecore.service.impl;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.minutetask.casecore.annotation.Key;
import org.minutetask.casecore.annotation.ServiceRef;

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

import org.minutetask.casecore.jpa.entity.UseCaseEntity;
import org.minutetask.casecore.jpa.repository.KeyTypeRepository;
import org.minutetask.casecore.jpa.repository.UseCaseRepository;
import org.minutetask.casecore.service.api.UseCaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

//TODO
@Transactional
@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UseCaseManagerImpl implements UseCaseManager {
    private static final TypeReference<Map<String, Object>> PARAMETERS_REFERENCE = //
            new TypeReference<Map<String, Object>>() {
            };

    @Autowired
    private KeyTypeRepository keyTypeRepository;

    @Autowired
    private UseCaseRepository useCaseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public UseCaseEntity createUseCase(Object data) {
        UseCaseEntity useCase = new UseCaseEntity();
        //
        List<Field> parameterFields = FieldUtils.getAllFieldsList(data.getClass());
        Map<String, Object> parameters = new HashMap<String, Object>();
        for (Field parameterField : parameterFields) {
            String parameterName = parameterField.getName();
            Object parameterValue = FieldUtils.getField(data.getClass(), parameterField.getName(), true);
            parameters.put(parameterName, parameterValue);
        }
        useCase.setParameters(parameters);
        //
        List<Field> keyFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), Key.class);
        Map<String, String> keys = new HashMap<String, String>();
        for (Field keyField : keyFields) {
            String keyType = keyField.getAnnotation(Key.class).type();
            Object keyValue = FieldUtils.getField(data.getClass(), keyField.getName(), true);
            parameters.put(keyType, (String) keyValue);
        }
        useCase.setParameters(parameters);
        //ConvertU
        //
        List<Field> serviceRefFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), ServiceRef.class);
        FieldUtils.getField(getClass(), null);
        //
        useCase = useCaseRepository.save(useCase);
        return useCase;
    }

    @Override
    public UseCaseEntity getUseCase(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UseCaseEntity getUseCase(String keyType, String keyValue) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void updateUseCase(UseCaseEntity useCase, Object data) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteUseCase(Long id) {
        useCaseRepository.deleteById(id);
    }
}
