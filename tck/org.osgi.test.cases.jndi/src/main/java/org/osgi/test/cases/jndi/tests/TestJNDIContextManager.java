/*******************************************************************************
 * Copyright (c) Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0 
 *******************************************************************************/


package org.osgi.test.cases.jndi.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.spi.InitialContextFactory;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.service.jndi.JNDIContextManager;
import org.osgi.test.cases.jndi.provider.CTContext;
import org.osgi.test.cases.jndi.provider.CTInitialContextFactory;
import org.osgi.test.cases.jndi.provider.CTInitialDirContextFactory;
import org.osgi.test.cases.jndi.service.ExampleService;

/** 
 * 
 * A set of methods to test the functionality of the JNDIContextManager interface
 * 
 * @author $Id$
 * 
 */

public class TestJNDIContextManager extends JNDITestSupport {
	 
	@Test
	public void testLookupOfJREProvidedContexts() throws Exception {
		// No provider bundle needed.  The JRE is the provider
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		// Create environment to grab built in LDAP provider
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
		// Grab the context
		Context ctx = null;		
		try {
			ctx = ctxManager.newInitialContext(env);
			// Verify that we actually received the context
			assertThat(ctx).as("The context should not be null").isNotNull();
			Hashtable< ? , ? > ctxEnv = ctx.getEnvironment();
			assertThat((String)ctxEnv.get(Context.INITIAL_CONTEXT_FACTORY)).isEqualTo((String)env.get(Context.INITIAL_CONTEXT_FACTORY));
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			ungetService(ctxManager);
		}
	}

	@Test
	public void testLookupWithSpecificInitialContextFactory() throws Exception {
		// install provider bundle
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		int invokeCountBefore = CTContext.getInvokeCount();
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		// Grab the context
		Context ctx = null;
		try {
			ctx = ctxManager.newInitialContext(env);
			// Verify that we actually received the context
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testObject", new Object());
			int invokeCountAfter = CTContext.getInvokeCount();
			if (!(invokeCountAfter > invokeCountBefore)) {
				ctx.close();
				fail("The correct Context object was not found");
			}
			Object testObject = ctx.lookup("testObject");
			assertThat(testObject).isNotNull();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			ungetService(ctxManager);
		}
	}
	
	@Test
	public void testLookupWithSpecificInitialDirContextFactory() throws Exception {
		// install provider bundle
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialDirContextFactory1.jar"), true);
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		int invokeCountBefore = CTContext.getInvokeCount();
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialDirContextFactory.class.getName());
		DirContext ctx = null;
		try {
			// Grab the context
			ctx = ctxManager.newInitialDirContext(env);
			// Verify that we actually received the context
			assertThat(ctx).as("The context should not be null").isNotNull();
			BasicAttributes attrs = new BasicAttributes();
			attrs.put("testAttribute", new Object());
			ctx.bind("testObject", new Object(), attrs);
			int invokeCountAfter = CTContext.getInvokeCount();
			if (!(invokeCountAfter > invokeCountBefore)) {
				ctx.close();
				fail("the correct Context object was not found");
			}
			Object testObject = ctx.lookup("testObject");
			assertThat(testObject).isNotNull();
			Attributes returnedAttrs = ctx.getAttributes("testObject");
			assertThat(returnedAttrs).isEqualTo(attrs);
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			ungetService(ctxManager);
		}
	}
	
	@Test
	public void testDefaultLookupWithInitialContextFactory() throws Exception {
		// install provider bundle
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		int invokeCountBefore = CTContext.getInvokeCount();
		// Grab the context
		Context ctx = null;
		try {
			ctx = ctxManager.newInitialContext();
			// Verify that we actually received the context
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testObject", new Object());
			int invokeCountAfter = CTContext.getInvokeCount();
			if (!(invokeCountAfter > invokeCountBefore)) {
				ctx.close();
				fail("The correct Context object was not found");
			}
			Object testObject = ctx.lookup("testObject");
			assertThat(testObject).isNotNull();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			ungetService(ctxManager);
		}
	}
	
	@Test
	public void testDefaultLookupWithInitialDirContextFactory() throws Exception {
		// install provider bundle
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialDirContextFactory1.jar"), true);
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		int invokeCountBefore = CTContext.getInvokeCount();
		DirContext ctx = null;
		try {
			// Grab the context
			ctx = ctxManager.newInitialDirContext();
			// Verify that we actually received the context
			assertThat(ctx).as("The context should not be null").isNotNull();
			BasicAttributes attrs = new BasicAttributes();
			attrs.put("testAttribute", new Object());
			ctx.bind("testObject", new Object(), attrs);
			int invokeCountAfter = CTContext.getInvokeCount();
			if (!(invokeCountAfter > invokeCountBefore)) {
				ctx.close();
				fail("the correct Context object was not found");
			}
			Object testObject = ctx.lookup("testObject");
			assertThat(testObject).isNotNull();
			Attributes returnedAttrs = ctx.getAttributes("testObject");
			assertThat(returnedAttrs).isEqualTo(attrs);
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			ungetService(ctxManager);
		}
	}

	@Test
	public void testLookupWithInitialContextFactoryBuilder() throws Exception {
		// install provider bundle
		Bundle factoryBuilderBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactoryBuilder1.jar"), true);
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		int invokeCountBefore = CTContext.getInvokeCount();
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		// Grab the context
		Context ctx = null;
		try {
			ctx = ctxManager.newInitialContext(env);
			// Verify that we actually received the context
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testObject", new Object());
			int invokeCountAfter = CTContext.getInvokeCount();
			if (!(invokeCountAfter > invokeCountBefore)) {
				ctx.close();
				fail("The correct Context object was not found");
			}
			Object testObject = ctx.lookup("testObject");
			assertThat(testObject).isNotNull();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBuilderBundle.uninstall();
			ungetService(ctxManager);
		}
	}
	
	@Test
	public void testLookupWithInitialDirContextFactoryBuilder() throws Exception {
		// install provider bundle
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialDirContextFactoryBuilder1.jar"), true);
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		int invokeCountBefore = CTContext.getInvokeCount();
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialDirContextFactory.class.getName());
		DirContext ctx = null;
		try {
			// Grab the context
			ctx = ctxManager.newInitialDirContext(env);
			// Verify that we actually received the context
			assertThat(ctx).as("The context should not be null").isNotNull();
			BasicAttributes attrs = new BasicAttributes();
			attrs.put("testAttribute", new Object());
			ctx.bind("testObject", new Object(), attrs);
			int invokeCountAfter = CTContext.getInvokeCount();
			if (!(invokeCountAfter > invokeCountBefore)) {
				ctx.close();
				fail("the correct Context object was not found");
			}
			Object testObject = ctx.lookup("testObject");
			assertThat(testObject).isNotNull();
			Attributes returnedAttrs = ctx.getAttributes("testObject");
			assertThat(returnedAttrs).isEqualTo(attrs);
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			ungetService(ctxManager);
		}
	}
	
	@Test
	public void testDefaultLookupWithInitialContextFactoryBuilder() throws Exception {
		// install provider bundle
		Bundle factoryBuilderBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactoryBuilder1.jar"), true);
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		int invokeCountBefore = CTContext.getInvokeCount();
		// Grab the context
		Context ctx = null;
		try {
			ctx = ctxManager.newInitialContext();
			// Verify that we actually received the context
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testObject", new Object());
			int invokeCountAfter = CTContext.getInvokeCount();
			if (!(invokeCountAfter > invokeCountBefore)) {
				ctx.close();
				fail("The correct Context object was not found");
			}
			Object testObject = ctx.lookup("testObject");
			assertThat(testObject).isNotNull();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBuilderBundle.uninstall();
			ungetService(ctxManager);
		}
	}
	
	@Test
	public void testDefaultLookupWithInitialDirContextFactoryBuilder() throws Exception {
		// install provider bundle
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialDirContextFactoryBuilder1.jar"), true);
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		int invokeCountBefore = CTContext.getInvokeCount();
		DirContext ctx = null;
		try {
			// Grab the context
			ctx = ctxManager.newInitialDirContext();
			// Verify that we actually received the context
			assertThat(ctx).as("The context should not be null").isNotNull();
			BasicAttributes attrs = new BasicAttributes();
			attrs.put("testAttribute", new Object());
			ctx.bind("testObject", new Object(), attrs);
			int invokeCountAfter = CTContext.getInvokeCount();
			if (!(invokeCountAfter > invokeCountBefore)) {
				ctx.close();
				fail("the correct Context object was not found");
			}
			Object testObject = ctx.lookup("testObject");
			assertThat(testObject).isNotNull();
			Attributes returnedAttrs = ctx.getAttributes("testObject");
			assertThat(returnedAttrs).isEqualTo(attrs);
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			ungetService(ctxManager);
		}
	}
	
	@Test
	public void testLookupWithJndiPropertiesFile() throws Exception {
		// install the required bundles
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactoryWithProperties.jar"), true);
		// Grab the jNDIContextManager service
		JNDIContextManager ctxManager = getService (JNDIContextManager.class);
		int invokeCountBefore = CTContext.getInvokeCount();
		// Grab the context
		Context ctx = null;
		try {
			ctx = ctxManager.newInitialContext();
			// Verify that we actually received the context
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testObject", new Object());
			int invokeCountAfter = CTContext.getInvokeCount();
			if (!(invokeCountAfter > invokeCountBefore)) {
				ctx.close();
				fail("The correct Context object was not found");
			}
			Object testObject = ctx.lookup("testObject");
			assertThat(testObject).isNotNull();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			ungetService(ctxManager);
		}
	}
	
	@Test
	public void testLookupWithNoMatchingContext() throws Exception {
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		// Setup the environment for grabbing the specific initialContextFactory
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		// Try to grab a context from the specified initialContextFactory. This
		// should throw an exception.
		Context ctx = null;
		try {
			ctx = ctxManager.newInitialContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.lookup("testObject");
		} catch (javax.naming.NoInitialContextException ex) {
			System.out.println("javax.naming.NoInitialContextException caught in testLookupWithNoMatchingContext: SUCCESS");
			return;
		} finally {
			// If we don't get the exception, then this test fails
			if (ctx != null) {
				ctx.close();
			}
			ungetService(ctxManager);
		}
		failException("testLookupWithNoMatchingContext failed, ", javax.naming.NoInitialContextException.class);
	}
	
	@Test
	public void testDefaultLookupWithNoMatchingContext() throws Exception {
		// Install a bundle that registers a service we know we can grab
		Bundle serviceBundle = installer.installBundle(context.getBundle().getEntry("service1.jar"), true);
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		// Try to grab a context.  There's none installed so we should get back one capable
		// of only performing URL lookups.
		Context ctx = null;
		try {
			ctx = ctxManager.newInitialContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			ExampleService service = (ExampleService) ctx.lookup("osgi:service/org.osgi.test.cases.jndi.service.ExampleService");
			assertThat(service).isNotNull();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			serviceBundle.uninstall();
			ungetService(ctxManager);
		}
	}
	
	@Test
	public void testInitialContextFactoryBuilderExceptionHandling() throws Exception {
		// Install the test bundle
		Bundle factoryBuilderBundle = installer.installBundle(context.getBundle().getEntry("exceptionalInitialContextFactoryBuilder1.jar"), true);
		// Grab the JNDIContextmanager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		int invokeCountBefore = CTContext.getInvokeCount();
		// Grab the context
		Context ctx = null;
		try {
			ctx = ctxManager.newInitialContext();
			// Verify that we actually received the context
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testObject", new Object());
			int invokeCountAfter = CTContext.getInvokeCount();
			if (!(invokeCountAfter > invokeCountBefore)) {
				ctx.close();
				fail("The correct Context object was not found");
			}
			Object testObject = ctx.lookup("testObject");
			assertThat(testObject).isNotNull();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBuilderBundle.uninstall();
			ungetService(ctxManager);
		}
	}
	
	@Test
	public void testProviderUnregistration() throws Exception {
		// Install a bundle for grabbing a context
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		// Setup the environment
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		// Grab the context
		Context ctx = null;
		try {
			ctx = ctxManager.newInitialContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testObject", new Object());
			// Remove the bundle containing the provider.  The backing for the context should be removed as well.
			factoryBundle.uninstall();
			@SuppressWarnings("unused")
			Object obj = ctx.lookup("testObject");
		} catch (javax.naming.NoInitialContextException ex) {
			// This is what we're expecting to receive.
			System.out.println("javax.naming.NoInitialContextException caught in testProviderUnregistration: SUCCESS");
			return;
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			if (factoryBundle.getState() == Bundle.INSTALLED) {
				factoryBundle.uninstall();
			}
			ungetService(ctxManager);
		}
		
		failException("testProviderUnregistration failed", javax.naming.NoInitialContextException.class);
	}
	
	@Test
	public void testProviderUnregistrationWithBuilder() throws Exception {
		// Install a bundle for grabbing a context
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactoryBuilder1.jar"), true);
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		// Setup the environment
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		// Grab the context
		Context ctx = null;
		try {
			ctx = ctxManager.newInitialContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testObject", new Object());
			// Remove the bundle containing the provider.  The backing for the context should be removed as well.
			factoryBundle.uninstall();
			@SuppressWarnings("unused")
			Object obj = ctx.lookup("testObject");
		} catch (javax.naming.NoInitialContextException ex) {
			// This is what we're expecting to receive.
			System.out.println("javax.naming.NoInitialContextException caught in testProviderRegistrationWithBuilder: SUCCESS");
			return;	
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			if (factoryBundle.getState() == Bundle.INSTALLED) {
				factoryBundle.uninstall();
			}
			ungetService(ctxManager);
		}
		
		failException("testProviderUnregistrationWithBuilder failed", javax.naming.NoInitialContextException.class);
	}
	
	@Test
	public void testContextRebinding() throws Exception {
		// Install a bundle for grabbing a context
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		// Setup the environment
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		// Grab a context
		Context ctx = null;
		try {
			ctx = ctxManager.newInitialContext(env);
			// Verify that we actually received the context
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testObject", new Object());
			// Remove the bundle containing the provider.  The backing for the context should be removed as well.
			factoryBundle.uninstall();
			@SuppressWarnings("unused")
			Object obj = ctx.lookup("testObject");
		} catch (javax.naming.NoInitialContextException ex) {
			// do nothing here
		}
		
		// Install a bundle that can be used to rebind the context
		Bundle builderBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactoryBuilder1.jar"), true);
		// See if the rebinding occurs as it should
		try {
			@SuppressWarnings("unused")
			Object obj = ctx.lookup("testObject");
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			builderBundle.uninstall();
			ungetService(ctxManager);
		}
	}
	
	@Test
	public void testUngetContextManager() throws Exception {
		// Install a bundle for grabbing a context
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		// Setup the environment
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		// Grab a context
		Context ctx = null;
		try {
			ctx = ctxManager.newInitialContext(env);
			// Verify that we actually received the context
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testObject", new Object());
			ungetService(ctxManager);
			@SuppressWarnings("unused")
			Object obj = ctx.lookup("testObject");
		} catch (OperationNotSupportedException ex) {
			System.out.println("javax.naming.OperationNotSupportedException caught in testUngetContextManager: SUCCESS");
			return;
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
		}
		
		failException("testUngetContextManager failed", javax.naming.OperationNotSupportedException.class);
	}
	
	@Test
	public void testServiceRanking() throws Exception {
		// Install the necessary bundles
		Bundle factoryBundle1 = installer.installBundle(context.getBundle().getEntry("initialContextFactory2.jar"), true);
		Bundle factoryBundle2 = installer.installBundle(context.getBundle().getEntry("initialContextFactory3.jar"), true);
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		// Use the default context to grab one of the factories and make sure
		// it's the right one
		Context ctx = null;
		try {
			ctx = ctxManager.newInitialContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			InitialContextFactory ctf = (InitialContextFactory) ctx.lookup("osgi:service/javax.naming.spi.InitialContextFactory");
			// Let's grab a context instance and check the environment
			Hashtable< ? , ? > ctxEnv = ctf.getInitialContext(null)
					.getEnvironment();
			if (!ctxEnv.containsKey("test1")) {
				fail("The right context was not returned");
			}
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle1.uninstall();
			factoryBundle2.uninstall();
			ungetService(ctxManager);
		}
	}

	@Test
	public void testServiceRankingOnContextCreation() throws Exception {
		//Install the necessary bundles
		Bundle factoryBundle2 = installer.installBundle(context.getBundle().getEntry("initialContextFactory3.jar"), true);
		Bundle factoryBundle1 = installer.installBundle(context.getBundle().getEntry("initialContextFactory2.jar"), true);
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		Context ctx = null;
		try {
			ctx = ctxManager.newInitialContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testObject", new Object());
			Hashtable< ? , ? > ctxEnv = ctx.getEnvironment();
			if (!ctxEnv.containsKey("test1")) {
				fail("The right context was not returned");
			}
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle1.uninstall();
			factoryBundle2.uninstall();
			ungetService(ctxManager);
		}
	}
}
