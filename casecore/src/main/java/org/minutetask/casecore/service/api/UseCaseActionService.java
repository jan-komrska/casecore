package org.minutetask.casecore.service.api;

import java.lang.reflect.Method;

import org.minutetask.casecore.jpa.entity.UseCaseActionEntity;
import org.minutetask.casecore.jpa.entity.UseCaseEntity;

// TODO
public interface UseCaseActionService {
    public UseCaseActionEntity createAction(UseCaseEntity useCase, Method method, Object[] args);

    public UseCaseActionEntity getAction(Long id);

    public void updateAction(UseCaseActionEntity action);

    public void deleteAction(Long id);
}
