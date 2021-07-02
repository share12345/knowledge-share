package com.knowledge.share.wkk.spring.demo;

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Spring Bean生命周期示例
 * @author Wangkunkun
 * @date 2021/7/1 15:21
 */
public class BeanLifecycleDemo {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();

        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(applicationContext);
        reader.loadBeanDefinitions("classpath:META-INF/bean-lifecycle-demo.xml");
        applicationContext.refresh();

        User user = applicationContext.getBean("user", User.class);
        //applicationContext.getBeanFactory().destroyBean(user);
        //user = applicationContext.getBean("user", User.class);
        System.out.println(user);
        applicationContext.close();
    }
}
