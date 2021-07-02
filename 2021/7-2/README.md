# Spring Bean生命周期

## 概述

今天要来讲解Spring Bean的生命周期。

生命周期就是在不同的阶段有哪些API可以进行处理或者说是回调，可以看作是钩子。

首先，需要明白一些概念：

* 什么是Bean：在Spring IoC容器中管理Object的对象都可以说是Bean。这个比较笼统，有些虽然注册到Spring IoC容器中，但是它的生命周期是不受Spring IoC管理的。

大致的可以将Bean的生命周期分成四个阶段：实例化、属性填充、初始化和销毁。

创建一个对象出来叫做实例化，给一个对象的属性进行赋值的过程叫属性填充，执行一个对象定义的初始化方法的过程叫初始化，

Bean生命周期触发的时机：

在创建Bean实例时会触发，比如BeanFactory.getBean获取一个Bean的实例，如果这个bean还没有被创建，就会创建bean实例，并且走生命周期阶段。

和Bean生命周期相关的核心API和方法：

* org.springframework.beans.factory.config.BeanPostProcessor：初始化相关的回调接口
* org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor：实例化和属性赋值相关相关的回调接口

## ApplicationContext和BeanFactory的关系

BeanFactory是底层的IoC容器，提供了IoC的基本能力；ApplicationContext继承了BeanFactory这个接口，但是它又在内部组合了BeanFactory，ApplicationContext实现了BeanFactory的全部能力，相当于BeanFactory的超集，并且提供了更多的特性，比如：AOP更好的组合，资源处理，事件的发布等。

## Spring Bean实例化前阶段

首先是Bean生命周期的第一个阶段，称为实例化前阶段。

在这个阶段的，会调用
`InstantiationAwareBeanPostProcessor#postProcessBeforeInstantiation`方法，这个方法的返回值是一个Object对象，如果返回的对象不为null，就不会执行bean接下来的实例化操作，用这个方法返回的对象作为Bean实例化的对象。

实现细节：org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#resolveBeforeInstantiation

具体代码细节：spring-beans/src/main/java/org/springframework/beans/factory/support/AbstractAutowireCapableBeanFactory.java:505

## Spring Bean实例化阶段

实例化阶段就是开始new一个对象出来，主要有两种方式：

* 传统实例化方式，就是使用Java反射获取无参的构造器，创建一个对象。
* 构造器依赖注入实例化方式，这种方式就需要获取到指定的构造器，不能在使用默认的无参构造器了。比如`<bean id="user" class="xxx"> <constructor-arg name="id" value="1" /> </bean>`

实现细节：org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#initializeBean(String, Object, RootBeanDefinition)

这部分的源码分析下次再分析了，没看呢。

## Bean实例化后阶段

Bean实例化后阶段，对应的方法就是`InstantiationAwareBeanPostProcessor#postProcessAfterInstantiation`.

这一步是对Bean进行赋值的最前一步操作，如果返回值为false，就跳过后面的赋值阶段。可以在这个阶段对Bean的属性进行赋值，然后跳过后面的属性赋值。

实现细节：AbstractAutowireCapableBeanFactory#populateBean

## Spring Bean属性赋值前阶段

在属性赋值前，可以通过InstantiationAwareBeanPostProcessor接口改变属性值。

这里有两个方法都都提供了这种能力，分别是：

* postProcessPropertyValues是5.0之前的；
* 5.1之后的postProcessProperties：这个方法如果返回null，表示不对属性值进行修改，按照原先的属性值进行Bean实例的属性填充。

实现细节：AbstractAutowireCapableBeanFactory#populateBean

## Spring Bean Aware 接口回调阶段

Spring提供了Aware系列接口，通过实现不同的Aware接口，可以在回调的时候注入内建的Bean：

* BeanNameAware 获取当前 Bean 的名称
* BeanClassLoaderAware 获取加载当前 Bean Class 的 ClassLoader
* BeanFactoryAware 获取 IoC 容器 - BeanFactory
* EnvironmentAware 获取 Environment 对象
* ResourceLoaderAware 获取资源加载器 对象 - ResourceLoader
* ApplicationEventPublisherAware 获取 ApplicationEventPublisher 对象，用于 Spring 事件
* MessageSourceAware 获取 MessageSource 对象，用于 Spring 国际化
* ApplicationContextAware 获取 Spring 应用上下文 - ApplicationContext 对象
* EmbeddedValueResolverAware 获取 StringValueResolver 对象，用于占位符处理

如果创建的Bean实例实现了对应的一个或多个接口，会在这个阶段将对应的BeanName、BeanClassLoader、BeanFactory等信息通过set方法
大致了解下有这些回调就好了，没必要都记住。BeanNameAware、BeanClassLoaderAware、BeanFactoryAware它们三个的回调时机是在BeanFactory中触发的，剩下的Aware回调都属于ApplicationContext中触发。

BeanFactory触发Aware的实现方法：`org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#invokeAwareMethods`

详细参考：spring-beans/src/main/java/org/springframework/beans/factory/support/AbstractAutowireCapableBeanFactory.java:1788

ApplicationContext中触发Aware的实现方法是在BeanPostProcessor中实现的，具体参考：`org.springframework.context.support.ApplicationContextAwareProcessor#postProcessBeforeInitialization`

## Spring Bean初始化前阶段

初始化前阶段的方法：`org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization`。

方法的返回值是一个实例对象，如果返回的不为空，就使用返回的bean对象替换原来的bean实例对象，通过这个方法可以对bean进行代理增强，返回代理的bean实例对象。

很多开源框架对Spring进行整合，如果要实现Bean的增强都是在这一步实现的，比如Dubbo中对@Reference的处理，参考类`DubboConsumerAutoConfiguration`。

实现细节：`org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#applyBeanPostProcessorsBeforeInitialization`

## Bean的初始化阶段

这个阶段就是执行Bean自定义的初始化方法，这个方法有三种方式可以定义：

* @PostConstruct标注的方法
* 实现InitializingBean接口的afterPropertiesSet() 方法
* 自定义初始化方法，比如在xml中生命类的时候指定`<bean id="user" class="xxx" init-method="init" />`。

实现细节：
`org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#invokeInitMethods`
`org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#postProcessBeforeInitialization`

这里需要注意的是@PostConstruct执行的时机和其他两个执行的时机不同。

## Bean的初始化后阶段

Bean的初始化后阶段对应方法为：`org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization`

这个方法和初始化前阶段`postProcessBeforeInitialization`方法类似，也是返回一个Bean对象，如果返回不为null，就替换原来的Bean对象。

实现细节：`AbstractAutowireCapableBeanFactory#applyBeanPostProcessorsAfterInitialization`

Spring AOP的就是在这个阶段对类进行代理的。

## Spring Bean初始化完成阶段

这个阶段是创建Bean的最后一个生命周期了，方法为`SmartInitializingSingleton#afterSingletonsInstantiated`，这个阶段并不一定都会触发，只有是单例并且非延迟加载的bean才会执行这个生命周期回调。

实现细节：`org.springframework.beans.factory.config.ConfigurableListableBeanFactory#preInstantiateSingletons`

为什么？

可以看一个这个被调用的源码：

调用栈为：
`org.springframework.context.support.AbstractApplicationContext#refresh` ->
`org.springframework.context.support.AbstractApplicationContext#finishBeanFactoryInitialization` ->
`org.springframework.context.support.AbstractApplicationContext#finishBeanFactoryInitialization` ->
`org.springframework.beans.factory.support.DefaultListableBeanFactory#preInstantiateSingletons`

所以只有在Spring的应用上下文第一次refresh的才会调用，而且这个调用只能初始化单例非延迟的Bean。

## Spring Bean销毁前阶段

Bean销毁前阶段方法：`DestructionAwareBeanPostProcessor#postProcessBeforeDestruction`，在Bean实例被销毁前会进行调用，在这个阶段只能通知到Bean被销毁了，实际上并不能做什么改变。

可以调用AbstractBeanFactory#destroyBean(java.lang.String, java.lang.Object)主动销毁Bean实例，销毁Bean只是调用Bean的销毁方法，Bean并不会被GC回收，也不会从IOC容器中除移。当容器关闭时的销毁，会将IoC容器中除移Bean。

实现细节：`org.springframework.beans.factory.support.DisposableBeanAdapter#destroy`

## Spring Bean销毁阶段

和Spring Bean初始化阶段一样，Spring也提供了销毁阶段的三个回调方式：

* @PreDestroy标注方法
* 实现DisposableBean接口的destroy()方法
* 自定义销毁方法

实现细节：

`org.springframework.beans.factory.support.DisposableBeanAdapter#destroy`
`org.springframework.beans.factory.annotation.InitDestroyAnnotationBeanPostProcessor#postProcessBeforeDestruction`