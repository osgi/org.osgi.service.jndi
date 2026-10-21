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
import javax.naming.InitialContext;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.spi.InitialContextFactory;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.test.cases.jndi.provider.CTContext;
import org.osgi.test.cases.jndi.provider.CTInitialContextFactory;
import org.osgi.test.cases.jndi.provider.CTInitialDirContextFactory;

/**
 * 
 * 
 * A set of tests for the access and use of InitialContextFactory and
 * InitialContextFactoryBuilder instances
 * 
 * @author $Id$Date: 2009-07-07 15:38:11 -0400 (Tue, 07 Jul
 *          2009) $
 */
public class TestInitialContextFactory extends JNDITestSupport {

	@Test
	public void testSpecificInitialContextFactory() throws Exception {
		// Install the bundles needed for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		int invokeCountBefore = CTContext.getInvokeCount();
		// Setup the environment for grabbing the specific initialContextFactory
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		// Grab the initialContext
		Context ctx = null;
		try {
			ctx =  new InitialContext(env);
			// Verify that we actually received the InitialContext
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
			// Cleanup after the test completes
			if (ctx != null) {
				ctx.close();
			}
			testBundle.uninstall();
		}
	}
	
	@Test
	public void testSpecificInitialDirContextFactory() throws Exception {
		// Install the bundles needed for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("initialDirContextFactory1.jar"), true);
		int invokeCountBefore = CTContext.getInvokeCount();
		// Setup the environment for grabbing the specific initialDirContextFactory
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialDirContextFactory.class.getName());
		// Grab the initialDirContext)
		DirContext ctx = null;
		try {
			ctx = new InitialDirContext(env);
			// Verify that we actually receive the InitialContext
			assertThat(ctx).as("The context should not be null").isNotNull();
			BasicAttributes attrs = new BasicAttributes("testAttribute", new Object());
			ctx.bind("testObject", new Object(), attrs);
			int invokeCountAfter = CTContext.getInvokeCount();
			if (!(invokeCountAfter > invokeCountBefore)) {
				ctx.close();
				fail("The correct Context object was not found");
			}
			Object testObject = ctx.lookup("testObject");
			Attributes testAttrs = ctx.getAttributes("testObject");
			assertThat(testObject).isNotNull();
			assertThat(testAttrs).isNotNull();
		} finally {
			// Cleanup after the test completes
			if (ctx != null) {
				ctx.close();
			}
			testBundle.uninstall();
		}
	}

	@Test
	public void testUnspecifiedInitialContextFactory() throws Exception {
		// Install the bundles needed for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		// We don't setup the environment because we want to see if the
		// appropriate context factory is returned even if it isn't specified
		int invokeCountBefore = CTContext.getInvokeCount();
		Context ctx = null;
		try {
			ctx = new InitialContext();
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
			// Cleanup after the test completes
			if (ctx != null) {
				ctx.close();
			}
			testBundle.uninstall();
		}
	}

	@Test
	public void testUnspecifiedInitialDirContextFactory() throws Exception {
		// Install the bundles needed for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("initialDirContextFactory1.jar"), true);
		// We don't setup the environment because we want to see if the
		// appropriate context factory is returned even if it isn't specified
		int invokeCountBefore = CTContext.getInvokeCount();
		DirContext ctx = null;
		try {
			ctx = new InitialDirContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			BasicAttributes attrs = new BasicAttributes("testAttributes", new Object());
			ctx.bind("testObject", new Object(), attrs);
			int invokeCountAfter = CTContext.getInvokeCount();
			if (!(invokeCountAfter > invokeCountBefore)) {
				ctx.close();
				fail("the correct Context object was not found");
			}
			Object testObject = ctx.lookup("testObject");
			Attributes testAttrs = ctx.getAttributes("testObject");
			assertThat(testObject).isNotNull();
			assertThat(testAttrs).isNotNull();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			testBundle.uninstall();
		}
	}
	
	@Test
	public void testInitialContextFactoryBuilderWithFactory() throws Exception {
		// Install the bundles needed for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactoryBuilder1.jar"), true);
		// Try to get an initialContext object using the builder
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		int invokeCountBefore = CTContext.getInvokeCount();
		Context ctx = null;
		try {
			ctx = new InitialContext(env);
			// Verify that we actually received the InitialContext
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
			// Cleanup after the test completes
			if (ctx != null) {
				ctx.close();
			}
			testBundle.uninstall();
		}
	}
	
	@Test
	public void testInitialDirContextFactoryBuilderWithFactory() throws Exception {
		// Install the bundles needed for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("initialDirContextFactoryBuilder1.jar"), true);
		// Try to get an initialContext object using the builder
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialDirContextFactory.class.getName());
		int invokeCountBefore = CTContext.getInvokeCount();
		DirContext ctx = null;
		try {
			ctx = new InitialDirContext(env);
			// Verify that we actuall received the InitialDirContext
			assertThat(ctx).as("The context should not be null").isNotNull();
			BasicAttributes attrs = new BasicAttributes("testAttributes", new Object());
			ctx.bind("testObject", new Object(), attrs);
			int invokeCountAfter = CTContext.getInvokeCount();
			if (!(invokeCountAfter > invokeCountBefore)) {
				ctx.close();
				fail("The correct Context object was not found");
			}
			Object testObject = ctx.lookup("testObject");
			Attributes testAttrs = ctx.getAttributes("testObject");
			assertThat(testObject).isNotNull();
			assertThat(testAttrs).isNotNull();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			testBundle.uninstall();
		}
	}
	
	@Test
	public void testInitialContextFactoryBuilderWithNoFactory() throws Exception {
		// Install the bundles needed for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactoryBuilder1.jar"), true);
		// Try to get an initialContext object using the builder
		int invokeCountBefore = CTContext.getInvokeCount();
		Context ctx = null;
		try {
			ctx = new InitialContext();
			// Verify that we actually received an InitialContext
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
			// Cleanup after the test completes
			if (ctx != null) {
				ctx.close();
			}
			testBundle.uninstall();
		}
	}
	
	@Test
	public void testInitialDirContextFactoryBuilderWithNoFactory() throws Exception {
		// Install the bundles needed for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("initialDirContextFactoryBuilder1.jar"), true);
		// try to get an initialDirContext object using the builder
		int invokeCountBefore = CTContext.getInvokeCount();
		DirContext ctx = null;
		try {
			ctx = new InitialDirContext();
			// Verify that we actually received an initialDirContext
			assertThat(ctx).as("The context should not be null").isNotNull();
			BasicAttributes attrs = new BasicAttributes("testAttributes", new Object());
			ctx.bind("testObject", new Object(), attrs);
			int invokeCountAfter = CTContext.getInvokeCount();
			if (!(invokeCountAfter > invokeCountBefore)) {
				ctx.close();
				fail("The correct Context object was not found");
			}
			Object testObject = ctx.lookup("testObject");
			Attributes testAttrs = ctx.getAttributes("testObject");
			assertThat(testObject).isNotNull();
			assertThat(testAttrs).isNotNull();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			testBundle.uninstall();
		}
	}
	
	@Test
	public void testInitialContextFactoryFromPropertiesFile() throws Exception {
		// Install bundle containing the initialContextFactory we are configuring via the jndi.properties file
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactoryWithProperties.jar"), true);
		int invokeCountBefore = CTContext.getInvokeCount();
		Context ctx = new InitialContext();
		try {
			// Verify that we actually received the InitialContext
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
			// Cleanup after the test completes
			if (ctx != null) {
				ctx.close();
			}
			
			factoryBundle.uninstall();
		}
	}

	@Test
	public void testInitialContextFactoryRemoval() throws Exception {
		// Install the bundle that has the test provider implementations
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		// Uninstall the bundle now so the provider implementations are
		// unregistered
		testBundle.uninstall();
		// Try to get a context using the just removed initialContextFactory
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		Context ctx = null;
		try {
			ctx = new InitialContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testObject", new Object());
		} catch (javax.naming.NoInitialContextException ex) {
			return;
		} finally {
			// If we don't get the exception, then this test fails
			if (ctx != null) {
				ctx.close();
			}
		}
		failException("testInitialContextFactoryRemoval failed, ", javax.naming.NoInitialContextException.class);
	}

	@Test
	public void testInitialContextFactoryBuilderRemoval() throws Exception {
		// Install the bundle that has the test provider implementations
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactoryBuilder1.jar"), true);
		// Uninstall the bundle now so the provider implementations are
		// unregistered
		testBundle.uninstall();
		// Try to grab the initialContextFactory. We should get a
		// NullPointerException
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		Context ctx = null;
		try {
			ctx = new InitialContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testObject", new Object());
		} catch (javax.naming.NoInitialContextException ex) {
			return;
		} finally {
			// If we don't get the exception, then this test fails
			if (ctx != null) {
				ctx.close();
			}
		}
		failException("testInitialContextFactoryBuilderRemoval failed, ", javax.naming.NoInitialContextException.class);
	}

	@Test
	public void testNoInitialContextFound() throws Exception {
		// Setup the environment for grabbing the specific initialContextFactory
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		// Try to grab a context from the specified initialContextFactory. This
		// should throw an exception.
		Context ctx = null;
		try {
			ctx = new InitialContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			@SuppressWarnings("unused")
			Object testObject = ctx.lookup("testObject");
		} catch (javax.naming.NoInitialContextException ex) {
			return;
		} finally {
			// If we don't get the exception, then this test fails
			if (ctx != null) {
				ctx.close();
			}
		}
		failException("testNoInitialContextFound failed, ", javax.naming.NoInitialContextException.class);
	}

	@Test
	public void testServiceRanking() throws Exception {
		// Install the necessary bundles
		Bundle factoryBundle1 = installer.installBundle(context.getBundle().getEntry("initialContextFactory2.jar"), true);
		Bundle factoryBundle2 = installer.installBundle(context.getBundle().getEntry("initialContextFactory3.jar"), true);
		// Use the default context to grab one of the factories and make sure
		// it's the right one
		Context ctx = null;
		try {
			ctx = new InitialContext();
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
		}
	}

	@Test
	public void testServiceRankingOnContextCreation() throws Exception {
		//Install the necessary bundles
		Bundle factoryBundle2 = installer.installBundle(context.getBundle().getEntry("initialContextFactory3.jar"), true);
		Bundle factoryBundle1 = installer.installBundle(context.getBundle().getEntry("initialContextFactory2.jar"), true);
		Context ctx = null;
		try {
			ctx = new InitialContext();
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
		}
	}
}
