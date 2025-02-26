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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.minutetask.casecore.annotation.ClosedRef;
import org.minutetask.casecore.annotation.IdRef;
import org.minutetask.casecore.annotation.KeyRef;
import org.minutetask.casecore.annotation.ServiceRef;
import org.minutetask.casecore.exception.ConflictException;
import org.minutetask.casecore.exception.UnexpectedException;
import org.minutetask.casecore.jpa.entity.UseCaseEntity;
import org.minutetask.casecore.jpa.entity.UseCaseKeyEntity;
import org.minutetask.casecore.jpa.repository.UseCaseKeyRepository;
import org.minutetask.casecore.jpa.repository.UseCaseRepository;
import org.minutetask.casecore.service.api.LiteralService;
import org.minutetask.casecore.service.api.UseCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

@Transactional(readOnly = true)
@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UseCaseServiceImpl implements UseCaseService {
    @Autowired
    private LiteralService literalService;

    @Autowired
    private UseCaseRepository useCaseRepository;

    @Autowired
    private UseCaseKeyRepository useCaseKeyRepository;

    @Qualifier("org.minutetask.casecore.CoreCaseConfiguration::objectMapper")
    @Autowired
    private ObjectMapper objectMapper;

    //

    private void updateUcData(UseCaseEntity useCase, Object data) {
        if (data == null) {
            return;
        }
        //
        try {
            List<Field> idFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), IdRef.class);
            for (Field idField : idFields) {
                Object idValue = FieldUtils.readField(idField, data, true);
                idValue = objectMapper.convertValue(idValue, Long.class);
                if (!Objects.equals(useCase.getId(), idValue)) {
                    throw new ConflictException();
                }
            }
            //
            Map<String, Object> parameters = new HashMap<String, Object>();
            List<Field> parameterFields = FieldUtils.getAllFieldsList(data.getClass());
            for (Field parameterField : parameterFields) {
                String parameterName = parameterField.getName();
                Object parameterValue = FieldUtils.readField(parameterField, data, true);
                parameters.put(parameterName, parameterValue);
            }
            useCase.getUseCaseData().getParameters().putAll(parameters);
            //
            List<Field> closedFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), ClosedRef.class);
            for (Field closedField : closedFields) {
                Object closedValue = FieldUtils.readField(closedField, data, true);
                closedValue = objectMapper.convertValue(closedValue, Boolean.class);
                useCase.setClosed(Boolean.TRUE.equals(closedValue));
            }
            //
            Map<Long, Object> keys = new HashMap<Long, Object>();
            List<Field> keyFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), KeyRef.class);
            for (Field keyField : keyFields) {
                String keyType = keyField.getAnnotation(KeyRef.class).value();
                Long keyTypeId = literalService.getIdFromValue(keyType);
                Object keyValue = FieldUtils.readField(keyField, data, true);
                keys.put(keyTypeId, keyValue);
            }
            useCase.getUseCaseData().getKeys().putAll(keys);
            //
            Map<Long, String> services = new HashMap<Long, String>();
            List<Field> serviceFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), ServiceRef.class);
            for (Field serviceField : serviceFields) {
                Class<?> contractClass = serviceField.getAnnotation(ServiceRef.class).value();
                Long contractClassId = literalService.getIdFromClass(contractClass);
                String serviceName = (String) FieldUtils.readField(serviceField, data, true);
                services.put(contractClassId, serviceName);
            }
            useCase.getUseCaseData().getServices().putAll(services);
        } catch (IllegalAccessException ex) {
            throw new UnexpectedException(ex);
        }
    }

    private UseCaseEntity updateUcKeys(UseCaseEntity useCase) {
        List<UseCaseKeyEntity> useCaseKeyList = new ArrayList<UseCaseKeyEntity>();
        List<UseCaseKeyEntity> useCaseKeyDiff = new ArrayList<UseCaseKeyEntity>();
        Map<Long, UseCaseKeyEntity> useCaseKeyMap = new HashMap<Long, UseCaseKeyEntity>();
        for (UseCaseKeyEntity useCaseKey : useCase.getUseCaseKeys()) {
            useCaseKeyMap.put(useCaseKey.getTypeId(), useCaseKey);
        }
        //
        if (!useCase.isClosed()) {
            for (Map.Entry<Long, Object> entry : useCase.getUseCaseData().getKeys().entrySet()) {
                Long keyTypeId = entry.getKey();
                String keyValue = objectMapper.convertValue(entry.getValue(), String.class);
                //
                if (StringUtils.isEmpty(keyValue)) {
                    // DO NOTHING
                } else if (useCaseKeyMap.containsKey(keyTypeId)) {
                    UseCaseKeyEntity entity = useCaseKeyMap.remove(keyTypeId);
                    if (!Objects.equals(keyValue, entity.getValue())) {
                        entity.setValue(keyValue);
                        //
                        useCaseKeyList.add(entity);
                        useCaseKeyDiff.add(entity);
                    } else {
                        useCaseKeyList.add(entity);
                    }
                } else {
                    UseCaseKeyEntity entity = new UseCaseKeyEntity();
                    entity.setTypeId(keyTypeId);
                    entity.setValue(keyValue);
                    entity.setUseCase(useCase);
                    //
                    useCaseKeyList.add(entity);
                    useCaseKeyDiff.add(entity);
                }
            }
        }
        //
        if (CollectionUtils.isNotEmpty(useCaseKeyDiff) || MapUtils.isNotEmpty(useCaseKeyMap)) {
            useCase.setUseCaseKeys(useCaseKeyList);
            for (Map.Entry<Long, UseCaseKeyEntity> entry : useCaseKeyMap.entrySet()) {
                useCaseKeyRepository.delete(entry.getValue());
            }
            for (UseCaseKeyEntity useCaseKey : useCaseKeyDiff) {
                useCaseKeyRepository.save(useCaseKey);
            }
            return useCaseRepository.save(useCase);
        } else {
            return useCase;
        }
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
            Map<String, Object> parameters = useCase.getUseCaseData().getParameters();
            List<Field> parameterFields = FieldUtils.getAllFieldsList(dataClass);
            for (Field parameterField : parameterFields) {
                String parameterName = parameterField.getName();
                Object parameterValue = parameters.get(parameterName);
                parameterValue = objectMapper.convertValue(parameterValue, parameterField.getType());
                FieldUtils.writeField(parameterField, data, parameterValue, true);
            }
            //
            List<Field> idFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), IdRef.class);
            for (Field idField : idFields) {
                Object idValue = objectMapper.convertValue(useCase.getId(), idField.getType());
                FieldUtils.writeField(idField, data, idValue, true);
            }
            //
            List<Field> closedFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), ClosedRef.class);
            for (Field closedField : closedFields) {
                Object closedValue = objectMapper.convertValue(useCase.isClosed(), closedField.getType());
                FieldUtils.writeField(closedField, data, closedValue, true);
            }
            //
            Map<Long, Object> keys = useCase.getUseCaseData().getKeys();
            List<Field> keyFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), KeyRef.class);
            for (Field keyField : keyFields) {
                String keyType = keyField.getAnnotation(KeyRef.class).value();
                Long keyTypeId = literalService.getIdFromValue(keyType);
                Object keyValue = keys.get(keyTypeId);
                keyValue = objectMapper.convertValue(keyValue, keyField.getType());
                FieldUtils.writeField(keyField, data, keyValue, true);
            }
            //
            Map<Long, String> services = useCase.getUseCaseData().getServices();
            List<Field> serviceFields = FieldUtils.getFieldsListWithAnnotation(data.getClass(), ServiceRef.class);
            for (Field serviceField : serviceFields) {
                Class<?> contractClass = serviceField.getAnnotation(ServiceRef.class).value();
                Long contractClassId = literalService.getIdFromClass(contractClass);
                String serviceName = services.get(contractClassId);
                FieldUtils.writeField(serviceField, data, serviceName, true);
            }
        } catch (IllegalAccessException ex) {
            throw new UnexpectedException(ex);
        }
        //
        return dataClass.cast(data);
    }

    //

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public UseCaseEntity newUseCase() {
        UseCaseEntity useCase = new UseCaseEntity();
        useCase.setClosed(false);
        useCase.setCreatedDate(LocalDateTime.now());
        return useCase;
    }

    @Override
    public UseCaseEntity getUseCase(Long id) {
        return useCaseRepository.findById(id).orElse(null);
    }

    @Override
    public UseCaseEntity getUseCase(String keyType, String keyValue) {
        Long keyTypeId = literalService.getIdFromValue(keyType);
        UseCaseKeyEntity useCaseKeyEntity = useCaseKeyRepository.findByTypeIdAndValue(keyTypeId, keyValue);
        if (useCaseKeyEntity != null) {
            return useCaseKeyEntity.getUseCase();
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    public UseCaseEntity saveUseCase(UseCaseEntity useCase) {
        useCase.applyChanges();
        useCase.setUpdatedDate(LocalDateTime.now());
        //
        useCase = useCaseRepository.save(useCase);
        return updateUcKeys(useCase);
    }

    @Override
    @Transactional
    public void deleteUseCaseById(Long id) {
        useCaseRepository.deleteById(id);
    }

    //

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public <Data> Data getUseCaseData(UseCaseEntity useCase, Class<Data> dataClass) {
        if (useCase.getSource() != null) {
            if (dataClass.isInstance(useCase.getSource())) {
                return dataClass.cast(useCase.getSource());
            } else {
                throw new ConflictException();
            }
        } else {
            Object source = getUcData(useCase, dataClass);
            useCase.setSource(source);
            return dataClass.cast(source);
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public void updateUseCaseData(UseCaseEntity useCase, Object data) {
        useCase.setSource(data);
        updateUcData(useCase, data);
    }
}
