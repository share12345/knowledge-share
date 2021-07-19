# Spring Bean配置元信息

## 上期回顾

在开始之前先和大家回顾下上期我们说的Spirng Bean生命周期都讲了哪些内容：

* 什么是Bean的生命周期？

  Bean的生命周期就是在Spring IoC容器中创建一个Bean时，在它的不同阶段可以出发不同的回调方法。

* Bean生命周期都有哪些阶段？

  实例化、属性填充、初始化、销毁。

* 和Bean生命周期相关的核心API有哪些，它们都提供了哪些接口？

  BeanPostProcessor、InstantiationAwareBeanPostProcessor、DestructionAwareBeanPostProcessor
  InstantiationAwareBeanPostProcessor继承了BeanPostProcesor，所以我们在实现的时候实现这个接口基本就可以了。它提供了Bean实例化前阶段回调、实例化后阶段、属性赋值前、初始化前阶段、初始化后阶段。DestructionAwareBeanPostProcessor提供了销毁前阶段回调。

上次有说到一个初始化完成阶段回调的接口SmartInitializingSingleton#afterSingletonsInstantiated
，最近看到一个它的应用，在ribbon中，添加一个注解@LoadBalanced就可以实现RestTemplate的负载均衡能力，它就是通过实现SmartInitializingSingleton#afterSingletonsInstantiated方法，做的手脚，在方法中对当前应用上下文中所有的被注解@LoadBalanced标注的RestTemplate对象，添加拦截器，在拦截器中实现一个负载均衡调用方式。

参考代码：

```java
public class LoadBalancerAutoConfiguration {

   @LoadBalanced
   @Autowired(required = false)
   private List<RestTemplate> restTemplates = Collections.emptyList();
   
   @Bean
   public SmartInitializingSingleton loadBalancedRestTemplateInitializer(
           final List<RestTemplateCustomizer> customizers) {
       return new SmartInitializingSingleton() {
           @Override
           public void afterSingletonsInstantiated() {
               for (RestTemplate restTemplate : LoadBalancerAutoConfiguration.this.restTemplates) {
                   for (RestTemplateCustomizer customizer : customizers) {
                       customizer.customize(restTemplate);
                   }
               }
           }
       };
   }
  // ....
}
```

## Bean的配置元信息介绍

这次开始和大家分享的内容是Spring Bean配置元信息的一些内容。

既然叫做元信息肯定是用来描述bean的，因为在Spring中，肯定是用来描述被Spring IoC容器所托管的Bean的。

核心类是BeanDefinition，它是描述Bean配置元信息的接口，基本上被Spring IoC容器托管的Bean都会先被解析成BeanDefinition的实现类，然后再通过BeanDefinition进行Bean的创建操作。

接口BeanDefinition主要包含的属性有：

* Bean的class名称
* Bean行为描述：作用域、是否延迟、自动绑定的模式、是否主要的Bean、初始化方法、销毁方法、
* Bean的属性配置：`MutablePropertyValues getPropertyValues();`
* Bean所依赖的其他Bean

| 属性                     | 说明                                          |
| ------------------------ | --------------------------------------------- |
| Class                    | Bean 全类名，必须是具体类，不能用抽象类或接口 |
| Name                     | Bean 的名称或者 ID                            |
| Scope                    | Bean 的作用域（如：singleton、prototype 等）  |
| Constructor arguments    | Bean 构造器参数（用于依赖注入）               |
| Properties               | Bean 属性设置（用于依赖注入）                 |
| Autowiring mode          | Bean 自动绑定模式（如：通过名称 byName）      |
| Lazy initialization mode | Bean 延迟初始化模式（延迟和非延迟）           |
| Initialization method    | Bean 初始化回调方法名称                       |
| Destruction              | method Bean 销毁回调方法名称                  |

## BeanDefinition的实现类

在Spring中提供的实现方式有三种，分别是GenericBeanDefinition、RootBeanDefinition、AnnotatedBeanDefinition。

* GenericBeanDefinition：通用型的BeanDefinition，是默认的BeanDefinition实现方式，除了标准实现外，还提供了parentName属性，可以配置Parent BeanDefinition。在xml中声明的Bean都会被解析成GenericBeanDefinition。

* RootBeanDeinition：没有parent的BeanDefinition或者合并后的BeanDefinition。它的setParentName方法不能调用，会抛出异常。

* AnnotatedBeanDefinition：注解标注的BeanDefinition，

  有三个实现分别是AnnotatedGenericBeanDefinition：基于注解的方式读取BeanDefinition，ConfigurationClassBeanDefinition：基于configclass的方式读取BeanDefinition，ScannedGenericBeanDefinition基于包扫描的方式读取BeanDefinition。

## BeanDefinition的来源

BeanDefinition的来源方式有哪些呢？

* 通过xml资源的方式解析与注册BeanDefinition，比如在注解出现之前我们常用的xml声明bean实例的方式。
* 通过properties资源解析与注册BeanDefinition，用的很少了。
* 通过API的方式构造出BeanDefinition，然后再注册，用的也很少。
* 通过Java注解的方式进行BeanDefinition的解析与注册。

## BeanDefinition的注册

当BeanDefinitin的信息被构建出来时，肯定需要一个保存的地方，因为后面创建Bean实例的时候需要用到，BeanDefinitionRegistry提供了BeanDefinition的注册接口。

核心API：org.springframework.beans.factory.support.BeanDefinitionRegistry。

它提供的接口：

* registerBeanDefinition：注册BeanDefinition
* removeBeanDefinition：删除BeanDefinition
* getBeanDefinition：根据BeanName获取对应的BeanDefinition
* containsBeanDefinition：判断是否包含指定BeanName的BeanDefinition的信息
* getBeanDefinitionNames
* getBeanDefinitionCount

BeanDefinitionRegistry的实现类

BeanDefinitionRegistry的实现类主要是DefaultListableBeanFactory，它使用的ConcurrentHashMap来保存注册的BeanDefinition，key是beanName，value值就是BeanDefinition。

之前有讲过ApplicationContext和BeanFactory的关系，ApplicationContext的ioc能力是委托给BeanFactory实现的。ApplicationContext也实现了BeanDefinitionRegistry接口，它的提供的BeanDefinition的注册能力也是委托给BeanFactory来完成的。

可以参考代码:`org.springframework.context.support.GenericApplicationContext#registerBeanDefinition`

```java
@Override
public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) throws BeanDefinitionStoreException {

  this.beanFactory.registerBeanDefinition(beanName, beanDefinition);
}
```

## Bean配置元信息底层实现

Spring BeanDefinition解析方式主要有三种，分别是XML资源解析，实现类是`XmlBeanDefinitionReader`；Properties资源解析，实现类是`PropertiesBeanDefinitionReader`；Java注解解析，实现类是`AnnotatedBeanDefinitionReader`。其中`XmlBeanDefinitionReader`和`PropertiesBeanDefinitionReader`都是基于资源的解析方式，都实现了`AbstractBeanDefinitionReader`，`AnnotatedBeanDefinitionReader`的解析方式和资源无关，所以并没有实现`AbstractBeanDefinitionReader`。

## 通过Java注解BeanDefinition解析与注册

目前都是通过注解的方式来声明Bean了，所以我们来主要分析一下这块是什么实现的。

前置知识：BeanDefinitionRegistryPostProcessor，提供了一个回调方法，这个方法在BeanFactory被创建之后执行，可以实现对BeanDefinition的添加。

**与这个实现相关的类有：**

* ClassPathBeanDefinitionScanner：从指定的包中扫描类，解析出BeanDefinition并注册。
* ComponentScanAnnotationParser：它是用来解析@ComponentScan注解，然后调用ClassPathBeanDefinitionScanner进行类扫描注册。
* ConfigurationClassParser：对@Component、@ComponentScan、@Import、@ImportResource解析
* ConfigurationClassPostProcessor：用来引导ConfigurationClassParser
* AnnotationConfigUtils：注册ConfigurationClassPostProcessor
* AnnotatedBeanDefinitionReader：通过注解从将class注册为BeanDefinition

**核心方法：**

org.springframework.context.annotation.AnnotationConfigUtils#registerAnnotationConfigProcessors(org.springframework.beans.factory.support.BeanDefinitionRegistry, java.lang.Object)

org.springframework.context.annotation.ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry

org.springframework.context.annotation.ComponentScanAnnotationParser#parse

org.springframework.context.annotation.ClassPathBeanDefinitionScanner#doScan

org.springframework.context.annotation.AnnotationConfigUtils#processCommonDefinitionAnnotations(org.springframework.beans.factory.annotation.AnnotatedBeanDefinition)

**基于@ComponentScan注解进行BeanDefinition解析与注册的执行流程：**

1）AnnotationConfigReactiveWebServerApplicationContext被创建时会初始化AnnotatedBeanDefinitionReader
2）AnnotatedBeanDefinitionReader在创建时会调用AnnotationConfigUtils#registerAnnotationConfigProcessors方法，注册ConfigurationClassPostProcessor
3）在应用上下文启动方法AbstractApplicationContext#refresh中调用ConfigurationClassPostProcessor#postProcessBeanDefinitionRegistry

在这个方法例算是正式开始执行BeanDefinition解析了。

4）首先会从已经注册的BeanDefinition中获取配置类并将配置类进行排序

什么样的类定义是配置类？可以参考方法：org.springframework.context.annotation.ConfigurationClassUtils#checkConfigurationClassCandidate

被Component、ComponentScan、Import、ImportResource、Configuration注解标注的类就是配置类。

5）设置一个名称生成器，这个就是在生命Bean的时候如果没有知道name可以通过这个生成器生成一个默认name。

6）构建ConfigurationClassParser对象，利用ConfigurationClassParser对象解析配置类

7.1）根据配置类上的@Conditional注解决定是否跳过当前的配置类
7.2）判断配置类是否已经被处理过了，如果处理过，并且是导入的就不处理了。否则继续处理。
7.3）从配置类中获取ComponentScans注解的信息，使用ClassPathBeanDefinitionScanner解析ComponentScans注解，从ComponentScans中获取包路径`basePackages`，扫描包路径下的class，生成BeanDefinition并注册。

8）解析完成后，从解析器ConfigurationClassParser中获取新的配置类，并使用ConfigurationClassBeanDefinitionReader，解析并注册配置类中声明的Bean定义。

9）检查新注册的BeanDefinition是否有配置类还没执行，如果有继续重复7、8
