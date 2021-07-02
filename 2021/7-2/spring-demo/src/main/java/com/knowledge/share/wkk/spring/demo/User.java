package com.knowledge.share.wkk.spring.demo;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 用户信息
 * @author Wangkunkun
 * @date 2021/7/1 14:42
 */
public class User implements BeanNameAware, BeanFactoryAware, BeanClassLoaderAware, InitializingBean,  DisposableBean {

    private Long id;

    private String name;

    private String beanName;

    @PostConstruct
    public void postConstruct() {
        System.out.printf("执行Bean初始化方法postConstruct，beanName: %s \n", beanName);
    }

    /**
     * user自定义初始化方法
     */
    public void init() {
        System.out.printf("执行Bean初始化方法init，beanName: %s \n", beanName);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.printf("执行Bean初始化方法afterPropertiesSet，beanName: %s \n", beanName);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        System.out.println("BeanClassLoaderAware接口回调");
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("BeanFactoryAware接口回调");
    }


    @Override
    public void setBeanName(String name) {
        System.out.println("BeanNameAware接口回调");
        this.beanName = name;
    }


    @Override
    public void destroy() throws Exception {
        System.out.printf("执行Bean销毁阶段方法destroy，beanName: %s \n", beanName);
    }

    public void customDestroy() {
        System.out.printf("执行Bean销毁阶段方法customDestroy，beanName: %s \n", beanName);
    }

    @PreDestroy
    public void preDestroy() {
        System.out.printf("执行Bean销毁阶段方法preDestroy，beanName: %s \n", beanName);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", beanName='" + beanName + '\'' +
                '}';
    }

    public User(Long id, String name, String beanName) {
        this.id = id;
        this.name = name;
        this.beanName = beanName;
    }
}
