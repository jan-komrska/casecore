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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.minutetask.casecore.annotation.IdRef;
import org.minutetask.casecore.annotation.KeyRef;
import org.minutetask.casecore.annotation.ServiceRef;
import org.minutetask.casecore.exception.ConflictException;
import org.minutetask.casecore.exception.UnexpectedException;
import org.minutetask.casecore.jpa.entity.UseCaseEntity;
import org.minutetask.casecore.jpa.entity.UseCaseKeyEntity;
import org.minutetask.casecore.jpa.repository.UseCaseKeyRepository;
import org.minutetask.casecore.jpa.repository.UseCaseRepository;
import org.minutetask.casecore.service.api.KeyTypeService;
import org.minutetask.casecore.service.api.UseCaseManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UseCaseManagerImpl implements UseCaseManager {
    @Autowired
    private KeyTypeService keyTypeService;

    @Autowired
    private UseCaseRepository useCaseRepository;

    @Autowired
    private UseCaseKeyRepository useCaseKeyRepository;

    @Qualifier("org.minutetask.casecore.CoreCaseConfiguration::conversionServiceBean")
    @Autowired
    private ConversionService conversionService;

    private void updateUcData(UseCaseEntity useCase, Object data) {
        try {
            List<Field> idFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), IdRef.class);
            for (Field idField : idFields) {
                Object idValue = FieldUtils.readField(idField, data, true);
                idValue = conversionService.convert(idValue, Long.class);
                if (!Objects.equals(useCase.getId(), idValue)) {
                    throw new ConflictException();
                }
            }
            //
            Map<String, Object> keys = new HashMap<String, Object>();
            List<Field> keyFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), KeyRef.class);
            for (Field keyField : keyFields) {
                String keyType = keyField.getAnnotation(KeyRef.class).value();
                Object keyValue = FieldUtils.readField(keyField, data, true);
                keys.put(keyType, keyValue);
            }
            useCase.getKeys().putAll(keys);
            //
            Map<String, Object> parameters = new HashMap<String, Object>();
            List<Field> parameterFields = FieldUtils.getAllFieldsList(data.getClass());
            for (Field parameterField : parameterFields) {
                String parameterName = parameterField.getName();
                Object parameterValue = FieldUtils.readField(parameterField, data, true);
                parameters.put(parameterName, parameterValue);
            }
            useCase.getParameters().putAll(parameters);
            //
            Map<Class<?>, String> services = new HashMap<Class<?>, String>();
            List<Field> serviceFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), ServiceRef.class);
            for (Field serviceField : serviceFields) {
                Class<?> contractClass = serviceField.getAnnotation(ServiceRef.class).value();
                String serviceName = (String) FieldUtils.readField(serviceField, data, true);
                services.put(contractClass, serviceName);
            }
            useCase.getServices().putAll(services);
            //
            useCase.applyChanges();
        } catch (IllegalAccessException ex) {
            throw new UnexpectedException(ex);
        }
    }

    // TOD handle null value
    private UseCaseEntity updateUcKeys(UseCaseEntity useCase) {
        List<UseCaseKeyEntity> useCaseKeyList = new ArrayList<UseCaseKeyEntity>();
        Map<Long, UseCaseKeyEntity> useCaseKeyMap = new HashMap<Long, UseCaseKeyEntity>();
        for (UseCaseKeyEntity useCaseKey : useCase.getUseCaseKeys()) {
            useCaseKeyMap.put(useCaseKey.getType(), useCaseKey);
        }
        //
        for (Map.Entry<String, Object> entry : useCase.getKeys().entrySet()) {
            Long keyType = keyTypeService.getKeyTypeId(entry.getKey());
            String keyValue = conversionService.convert(entry.getValue(), String.class);
            if (useCaseKeyMap.containsKey(keyType)) {
                UseCaseKeyEntity entity = useCaseKeyMap.remove(keyType);
                entity.setValue(keyValue);
                //
                useCaseKeyList.add(entity);
            } else {
                UseCaseKeyEntity entity = new UseCaseKeyEntity();
                entity.setType(keyType);
                entity.setValue(keyValue);
                entity.setUseCase(useCase);
                //
                useCaseKeyList.add(entity);
            }
        }
        //
        useCase.setUseCaseKeys(useCaseKeyList);
        for (Map.Entry<Long, UseCaseKeyEntity> entry : useCaseKeyMap.entrySet()) {
            useCaseKeyRepository.delete(entry.getValue());
        }
        for (UseCaseKeyEntity useCaseKey : useCaseKeyList) {
            useCaseKeyRepository.save(useCaseKey);
        }
        return useCaseRepository.save(useCase);
    }

    private <Data> Data getUcData(UseCaseEntity useCase, Class<Data> dataClass) {
        Object data;
        try {
            data = ConstructorUtils.invokeConstructor(dataClass);
        } catch (ReflectiveOperationException ex) {
            throw new UnexpectedException(ex);
        }
        //
        try {
            Map<String, Object> parameters = useCase.getParameters();
            List<Field> parameterFields = FieldUtils.getAllFieldsList(dataClass);
            for (Field parameterField : parameterFields) {
                String parameterName = parameterField.getName();
                Object parameterValue = parameters.get(parameterName);
                parameterValue = conversionService.convert(parameterValue, parameterField.getType());
                FieldUtils.writeField(parameterField, data, parameterValue, true);
            }
            //
            Map<Class<?>, String> services = useCase.getServices();
            List<Field> serviceFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), ServiceRef.class);
            for (Field serviceField : serviceFields) {
                Class<?> contractClass = serviceField.getAnnotation(ServiceRef.class).value();
                String serviceName = services.get(contractClass);
                FieldUtils.writeField(serviceField, data, serviceName, true);
            }
            //
            Map<String, Object> keys = useCase.getKeys();
            List<Field> keyFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), KeyRef.class);
            for (Field keyField : keyFields) {
                String keyType = keyField.getAnnotation(KeyRef.class).value();
                Object keyValue = keys.get(keyType);
                keyValue = conversionService.convert(keyValue, keyField.getType());
                FieldUtils.writeField(keyField, data, keyValue, true);
            }
            //
            List<Field> idFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), IdRef.class);
            for (Field idField : idFields) {
                Object idValue = conversionService.convert(useCase.getId(), idField.getType());
                FieldUtils.writeField(idField, data, idValue, true);
            }
        } catch (IllegalAccessException ex) {
            throw new UnexpectedException(ex);
        }
        //
        return dataClass.cast(data);
    }

    @Override
    public UseCaseEntity createUseCase(Object data) {
        UseCaseEntity useCase = new UseCaseEntity();
        //
        useCase.setActive(true);
        useCase.setCreatedDate(LocalDateTime.now());
        updateUcData(useCase, data);
        useCase = useCaseRepository.save(useCase);
        //
        return updateUcKeys(useCase);
    }

    @Override
    public UseCaseEntity getUseCase(Long id) {
        return useCaseRepository.findById(id).orElse(null);
    }

    @Override
    public UseCaseEntity getUseCase(String keyType, String keyValue) {
        Long keyTypeId = keyTypeService.getKeyTypeId(keyType);
        UseCaseKeyEntity useCaseKeyEntity = useCaseKeyRepository.findByTypeAndValue(keyTypeId, keyValue);
        if (useCaseKeyEntity != null) {
            return useCaseKeyEntity.getUseCase();
        } else {
            return null;
        }
    }

    @Override
    public UseCaseEntity updateUseCase(UseCaseEntity useCase, Object data) {
        useCase.setUpdatedDate(LocalDateTime.now());
        updateUcData(useCase, data);
        useCase = useCaseRepository.save(useCase);
        //
        return updateUcKeys(useCase);
    }

    @Override
    public <Data> Data getUseCaseData(UseCaseEntity useCase, Class<Data> dataClass) {
        return getUcData(useCase, dataClass);
    }

    @Override
    public UseCaseEntity finishUseCase(UseCaseEntity useCase) {
        useCase.setActive(false);
        useCase.setFinishedDate(LocalDateTime.now());
        return useCaseRepository.save(useCase);
    }

    @Override
    public void deleteUseCase(Long id) {
        useCaseRepository.deleteById(id);
    }
}
