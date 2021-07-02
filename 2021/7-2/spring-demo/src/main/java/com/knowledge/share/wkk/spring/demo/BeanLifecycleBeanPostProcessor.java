package com.knowledge.share.wkk.spring.demo;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;

/**
 * 自定义 {@link BeanPostProcessor} 示例
 * @author Wangkunkun
 * @date 2021/7/1 15:25
 * @see InstantiationAwareBeanPostProcessor
 * @see BeanPostProcessor
 */
public class BeanLifecycleBeanPostProcessor implements InstantiationAwareBeanPostProcessor, DestructionAwareBeanPostProcessor {

    /**
     * Bean实例化前方法
     * @param beanClass
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
        System.out.printf("执行Bean实例化前方法postProcessBeforeInstantiation，beanName：%s \n", beanName);
        return null;
    }

    /**
     * Bean实例化后方法
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
        System.out.printf("执行Bean实例化后方法postProcessAfterInstantiation，beanName：%s \n", beanName);
        return true;
    }

    /**
     * Bean属性填充前方法
     * @param pvs
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        System.out.printf("执行Bean属性填充前方法postProcessProperties，beanName：%s \n", beanName);
        return null;
    }

    /**
     * Bean初始化前方法
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        System.out.printf("执行Bean初始化前方法postProcessBeforeInitialization，beanName：%s \n", beanName);
        return bean;
    }

    /**
     * Bean初始化后方法
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        System.out.printf("执行Bean初始化后方法postProcessAfterInitialization，beanName：%s \n", beanName);
        return bean;
    }

    /**
     * Bean销毁前阶段
     * @param bean
     * @param beanName
     * @throws BeansException
     */
    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        System.out.printf("执行Bean销毁前阶段方法postProcessBeforeDestruction，beanName：%s \n", beanName);
    }
}
