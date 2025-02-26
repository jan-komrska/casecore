package org.minutetask.casecore.service.impl;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import org.minutetask.casecore.jpa.entity.UseCaseActionEntity;
import org.minutetask.casecore.jpa.entity.UseCaseEntity;
import org.minutetask.casecore.service.api.LiteralService;
import org.minutetask.casecore.service.api.UseCaseActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@Scope(value = BeanDefinition.SCOPE_SINGLETON)
public class UseCaseActionServiceImpl implements UseCaseActionService {
    @Autowired
    private LiteralService literalService;

    @Override
    public UseCaseActionEntity newAction(UseCaseEntity useCase) {
        UseCaseActionEntity action = new UseCaseActionEntity();
        action.setClosed(false);
        action.setCreatedDate(LocalDateTime.now());
        return action;
    }

    @Override
    public UseCaseActionEntity getAction(Long id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void persistAction(UseCaseActionEntity action) {
        // TODO Auto-generated method stub

    }

    @Override
    public void saveAction(UseCaseActionEntity action) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteAction(UseCaseActionEntity action) {
        // TODO Auto-generated method stub

    }

    @Override
    public Class<?> getActionServiceClass(UseCaseActionEntity action) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setActionServiceClass(UseCaseActionEntity action, Class<?> serviceClass) {
        // TODO Auto-generated method stub

    }

    @Override
    public Method getActionMethod(UseCaseActionEntity action) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setActionMethod(UseCaseActionEntity action, Method method) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object[] getActionArgs(UseCaseActionEntity action) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setActionArgs(UseCaseActionEntity action, Object[] args) {
        // TODO Auto-generated method stub
    }
}
