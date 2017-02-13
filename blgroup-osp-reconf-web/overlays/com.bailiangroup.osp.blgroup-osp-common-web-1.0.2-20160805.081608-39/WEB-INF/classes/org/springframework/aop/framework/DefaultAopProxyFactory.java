/*
 * Copyright 2002-2012 the original author or authors.
 *
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
 */

package org.springframework.aop.framework;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.springframework.aop.SpringProxy;

/**
 * Default {@link AopProxyFactory} implementation,
 * creating either a CGLIB proxy or a JDK dynamic proxy.
 *
 * <p>Creates a CGLIB proxy if one the following is true
 * for a given {@link AdvisedSupport} instance:
 * <ul>
 * <li>the "optimize" flag is set
 * <li>the "proxyTargetClass" flag is set
 * <li>no proxy interfaces have been specified
 * </ul>
 *
 * <p>Note that the CGLIB library classes have to be present on
 * the class path if an actual CGLIB proxy needs to be created.
 *
 * <p>In general, specify "proxyTargetClass" to enforce a CGLIB proxy,
 * or specify one or more interfaces to use a JDK dynamic proxy.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 12.03.2004
 * @see AdvisedSupport#setOptimize
 * @see AdvisedSupport#setProxyTargetClass
 * @see AdvisedSupport#setInterfaces
 */
@SuppressWarnings("serial")
public class DefaultAopProxyFactory implements AopProxyFactory, Serializable {


	public AopProxy createAopProxy(AdvisedSupport config) throws AopConfigException {
		if (config.isOptimize() || config.isProxyTargetClass() || hasNoUserSuppliedProxyInterfaces(config)) {
			Class targetClass = config.getTargetClass();
			if (targetClass == null) {
				throw new AopConfigException("TargetSource cannot determine target class: " +
						"Either an interface or a target is required for proxy creation.");
			}
			if (targetClass.isInterface()) {
				return new JdkDynamicAopProxy(config);
			}
			return CglibProxyFactory.createCglibProxy(config);
		}
		else {
			return new JdkDynamicAopProxy(config);
		}
	}

	/**
	 * Determine whether the supplied {@link AdvisedSupport} has only the
	 * {@link org.springframework.aop.SpringProxy} interface specified
	 * (or no proxy interfaces specified at all).
	 */
	private boolean hasNoUserSuppliedProxyInterfaces(AdvisedSupport config) {
		Class[] interfaces = config.getProxiedInterfaces();
		return (interfaces.length == 0 || (interfaces.length == 1 && SpringProxy.class.equals(interfaces[0])));
	}


	/**
	 * Inner factory class used to just introduce a CGLIB dependency
	 * when actually creating a CGLIB proxy.
	 */
	private static class CglibProxyFactory {

		/*public static AopProxy createCglibProxy(AdvisedSupport advisedSupport) {
			return new CglibAopProxy(advisedSupport);
		}*/
	
		/**
		 * Reimplementation createCglibProxy for the class which doesn't have any interface
		 * @Methods Name createCglibProxy
		 * @Create In 2014-12-18 By feelyn
		 * @param advisedSupport
		 * @return AopProxy
		 */
		public static AopProxy createCglibProxy(AdvisedSupport advisedSupport) {
			
			CglibAopProxy c2aop = new CglibAopProxy(advisedSupport);
			
			Object obj;
			try {
				obj = advisedSupport.getTargetSource().getTarget();
				if (null == obj) {
					throw new Exception("错误：找不到目标对象！");
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
	
			Constructor[] cstructs = obj.getClass().getDeclaredConstructors();
			if (cstructs.length == 1) {
				Constructor cstruct = cstructs[0];
				Class[] clazz = cstruct.getParameterTypes();
	
				if (clazz.length == 1) {
					Enhancer enhancer = new Enhancer();
					enhancer.setSuperclass(clazz[0]);
					enhancer.setCallback(new MethodInterceptorImpl());
	
					c2aop.setConstructorArguments(new Object[] { enhancer.create() }, clazz);
				}
			}
	
			return c2aop;
		}
	}
	
	private static class MethodInterceptorImpl implements MethodInterceptor {
		
		public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
				throws Throwable {
			proxy.invokeSuper(obj, args);
			return null;
		}
	}

}
