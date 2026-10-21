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


package org.osgi.test.cases.jndi.secure.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.directory.BasicAttributes;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.jndi.JNDIConstants;
import org.osgi.service.jndi.JNDIContextManager;
import org.osgi.service.jndi.JNDIProviderAdmin;
import org.osgi.test.cases.jndi.secure.provider.CTContext;
import org.osgi.test.cases.jndi.secure.provider.CTDirObjectFactory;
import org.osgi.test.cases.jndi.secure.provider.CTInitialContextFactory;
import org.osgi.test.cases.jndi.secure.provider.CTObjectFactory;
import org.osgi.test.cases.jndi.secure.provider.CTReference;
import org.osgi.test.cases.jndi.secure.provider.CTTestObject;

/** 
 * @author $Id$
 */
public class TestJNDISecurity extends JNDITestSupport {
	
	@Test
	public void testLookupAccessRestrictions() throws Exception {
		// Grab the context manager
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		Context ctx = null;
		try {
			// Grab a default context for looking up services
			ctx = ctxManager.newInitialContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			// Try to grab a service that we shouldn't be allowed to grab.
			@SuppressWarnings("unused")
			Object testObject = ctx.lookup("osgi:service/org.osgi.service.permissionadmin.PermissionAdmin");
		} catch (javax.naming.NameNotFoundException ex) {
			System.out.println("javax.naming.NameNotFoundException caught in testLookupAccessRestrictions: SUCCESS");
			return;
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			ungetService(ctxManager);
		}

		failException("testLookupAccessRestrictions failed, ", javax.naming.NameNotFoundException.class);
	}
	
	@Test
	public void testBundleContextAccessRestrictions() throws Exception {
		// Install the bundle necessary for this test
		Bundle contextBundle = installer.installBundle(context.getBundle().getEntry("inaccessibleBundleContext.jar"), true);
		// Grab the context manager
		Context ctx = null;
		Hashtable<String,Object> env = new Hashtable<>();
		BundleContext bundleCtx = (BundleContext) getService(Object.class,
				"(tb=inaccessibleBundleContext)");
		env.put(JNDIConstants.BUNDLE_CONTEXT, bundleCtx);
		try {
			// Grab a default context for looking up the bundle context
			ctx = new InitialContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			//Try to grab the bundle context.  We don't have permissions for it so this should fail.
			@SuppressWarnings("unused")
			Object testObject = ctx.lookup("osgi:framework/bundleContext");
		} catch (javax.naming.NameNotFoundException ex) {
			System.out.println("javax.naming.NameNotFoundException caught in testBundleContextAccessRestrictions: SUCCESS");
			return;
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			ungetService(bundleCtx);
			contextBundle.uninstall();
		}
		
		failException("testBundleContextAccessRestrictions failed, ", javax.naming.NameNotFoundException.class);
	}
	
	@Test
	public void testSecureLookupWithSpecificInitialContextFactory() throws Exception {
		// Install the bundle necessary for this test
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("factoryBundle.jar"), true);
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
	public void testSecureLookupWithInitialContextFactoryBuilder() throws Exception {
		// Install the bundle necessary for this test
		Bundle builderBundle = installer.installBundle(context.getBundle().getEntry("builderBundle.jar"), true);
		// Grab the JNDIContextManager service
		JNDIContextManager ctxManager = getService(JNDIContextManager.class);
		int invokeCountBefore = CTContext.getInvokeCount();
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
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
			builderBundle.uninstall();
			ungetService(ctxManager);
		}
	}
	
	@Test
	public void testSecureGetObjectInstanceWithFactoryName() throws Exception {
		// Install the bundle necessary for this test
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("factoryBundle.jar"), true);
		// Grab the JNDIProviderAdmin service
		JNDIProviderAdmin ctxAdmin = getService(JNDIProviderAdmin.class);
		try {
			// Create a reference object we can use for testing.
			CTReference ref = new CTReference(CTTestObject.class.getName(), CTObjectFactory.class.getName());
			// Resolve the reference
			CTTestObject testObject = (CTTestObject) ctxAdmin.getObjectInstance(ref, null, null, null);
			assertThat(testObject).isNotNull();
		} finally {
			factoryBundle.uninstall();
			ungetService(ctxAdmin);
		}
	}
	
	@Test
	public void testSecureGetObjectInstanceWithFactoryNameAndAttributes() throws Exception {
		// Install the bundle necessary for this test
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("factoryBundle.jar"), true);
		// Grab the JNDIProviderAdmin service
		JNDIProviderAdmin ctxAdmin = getService(JNDIProviderAdmin.class);
		try {
			BasicAttributes attrs = new BasicAttributes();
			attrs.put("testAttribute", new Object());
			// Create a reference object we can use for testing
			CTReference ref = new CTReference(CTTestObject.class.getName(), CTDirObjectFactory.class.getName());
			// Resolve the reference
			CTTestObject testObject = (CTTestObject) ctxAdmin.getObjectInstance(ref, null, null, null, attrs);
			assertThat(attrs).isEqualTo(testObject.getAttributes());
		} finally {
			factoryBundle.uninstall();
			ungetService(ctxAdmin);
		}
	}
	
	@Test
	public void testSecureGetObjectInstanceWithBuilder() throws Exception {
		// Install the bundle necessary for this test
		Bundle builderBundle = installer.installBundle(context.getBundle().getEntry("builderBundle.jar"), true);
		// Grab the JNDIProviderAdmin service
		JNDIProviderAdmin ctxAdmin = getService(JNDIProviderAdmin.class);
		try {
			// Create a reference object we can use for testing
			CTReference ref = new CTReference(CTTestObject.class.getName(), CTObjectFactory.class.getName());
			// Resolve the reference
			CTTestObject testObject = (CTTestObject) ctxAdmin.getObjectInstance(ref, null, null, null);
			assertThat(testObject).isNotNull();
		} finally {
			builderBundle.uninstall();
			ungetService(ctxAdmin);
		}
	}
}
