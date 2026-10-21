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

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.test.cases.jndi.provider.CTDirObjectFactory;
import org.osgi.test.cases.jndi.provider.CTInitialContextFactory;
import org.osgi.test.cases.jndi.provider.CTInitialDirContextFactory;
import org.osgi.test.cases.jndi.provider.CTObjectFactory;
import org.osgi.test.cases.jndi.provider.CTReference;
import org.osgi.test.cases.jndi.provider.CTTestObject;

/**
 * 
 * 
 * A set of tests for the access and use of ObjectFactory and
 * ObjectFactoryBuilder instances
 * 
 * @author $Id$Date: 2009-07-08 12:50:57 -0400 (Wed, 08 Jul
 *          2009) $
 */
public class TestObjectFactory extends JNDITestSupport {

	
	@Test
	public void testReferenceableLookup() throws Exception {
		// Install the bundles required for this test
		Bundle contextFactoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		Bundle objectFactoryBundle = installer.installBundle(context.getBundle().getEntry("objectFactory1.jar"), true);
		// Create an appropriate context, create a referenceable object, then try to resolve the referenceable object
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		Context ctx = null;
		try { 
			ctx = new InitialContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			CTTestObject ref = new CTTestObject("pass");
			ctx.bind("referenceable", ref);
			CTTestObject obj = (CTTestObject) ctx.lookup("referenceable");
			assertThat(obj.getValue()).isEqualTo(ref.getValue());
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			contextFactoryBundle.uninstall();
			objectFactoryBundle.uninstall();
		}
	}
	
	@Test
	public void testReferenceableLookupWithAttributes() throws Exception {
		// Install the bundles required for this test
		Bundle contextFactoryBundle = installer.installBundle(context.getBundle().getEntry("initialDirContextFactory1.jar"), true);
		Bundle objectFactoryBundle = installer.installBundle(context.getBundle().getEntry("dirObjectFactory1.jar"), true);
		// Create an appropriate context, create a referenceable object, then try to resolve the referenceable object
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialDirContextFactory.class.getName());
		DirContext ctx = null;
		try {
			ctx = new InitialDirContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			CTTestObject ref = new CTTestObject("pass", CTDirObjectFactory.class.getName());
			BasicAttributes attrs = new BasicAttributes("testAttributes", new Object());
			ctx.bind("referenceable", ref, attrs);
			CTTestObject obj = (CTTestObject) ctx.lookup("referenceable");
			Attributes testAttributes = ctx.getAttributes("referenceable");
			assertThat(ref.getValue()).isEqualTo(obj.getValue());
			assertThat(attrs).isEqualTo(testAttributes);
		} finally {
			if (ctx != null) {
				ctx.close();
			} 
			objectFactoryBundle.uninstall();
			contextFactoryBundle.uninstall();
		}
	}
	
	@Test
	public void testReferenceLookup() throws Exception {
		// Install the bundles required for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("objectFactory1.jar"), true);
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		// Create an appropriate context, store a reference, then attempt to
		// correctly lookup the reference.
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		Context ctx = null;
		try {
			ctx = new InitialContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			CTReference reference = new CTReference(CTTestObject.class.getName(), CTObjectFactory.class.getName());
			ctx.bind("reference", reference);
			CTTestObject obj = (CTTestObject) ctx.lookup("reference");
			assertThat(obj).isNotNull();
		} finally {
			// Cleanup after the test completes
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			testBundle.uninstall();
		}
	}
	
	@Test
	public void testReferenceLookupWithAttributes() throws Exception {
		// Install the bundles required for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("dirObjectFactory1.jar"), true);
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialDirContextFactory1.jar"), true);
		// Create an appropriate context, store a reference, then attempt to
		// correctly lookup the reference
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialDirContextFactory.class.getName());
		DirContext ctx = null;
		try {
			ctx = new InitialDirContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			CTReference reference = new CTReference(CTTestObject.class.getName(), CTDirObjectFactory.class.getName());
			BasicAttributes attrs = new BasicAttributes("testAttributes", new Object());
			ctx.bind("reference", reference, attrs);
			CTTestObject obj = (CTTestObject) ctx.lookup("reference");
			Attributes testAttributes = ctx.getAttributes("reference");
			assertThat(obj).isNotNull();
			assertThat(attrs).isEqualTo(testAttributes);
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			testBundle.uninstall();
		}
	}
	
	@Test
	public void testReferenceLookupWithNoFactory() throws Exception {
		// Install the bundles required for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("objectFactory1.jar"), true);
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		// Create an appropriate context, store a reference, then attempt
		// to correctly lookup the reference.
		Context ctx = null;
		try {
			ctx = new InitialContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			CTReference reference = new CTReference(CTTestObject.class.getName());
			ctx.bind("reference", reference);
			CTTestObject obj = (CTTestObject) ctx.lookup("reference");
			assertThat(obj).isNotNull();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			testBundle.uninstall();
		}
	}
	
	@Test
	public void testReferenceLookupWithNoFactoryAndAttributes() throws Exception {
		// Install the bundles required for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("dirObjectFactory1.jar"), true);
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialDirContextFactory1.jar"), true);
		// Create an appropriate context, store a reference, then attempt
		// to correctly lookup the reference
		DirContext ctx = null;
		try {
			ctx = new InitialDirContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			CTReference reference = new CTReference(CTTestObject.class.getName());
			BasicAttributes attrs = new BasicAttributes("testAttributes", new Object());
			ctx.bind("reference", reference, attrs);
			CTTestObject obj = (CTTestObject) ctx.lookup("reference");
			Attributes testAttributes = ctx.getAttributes("reference");
			assertThat(obj).isNotNull();
			assertThat(attrs).isEqualTo(testAttributes);
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			testBundle.uninstall();
		}
	}

	@Test
	public void testReferenceLookupWithBuilder() throws Exception {
		// Install the bundles required for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("objectFactoryBuilder1.jar"), true);
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		// Create an appropriate context, store a reference, then attempt to
		// correctly lookup the reference.
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		Context ctx = null;
		try {
			ctx = new InitialContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			CTReference reference = new CTReference(CTTestObject.class.getName());
			ctx.bind("reference", reference);
			CTTestObject obj = (CTTestObject) ctx.lookup("reference");
			assertThat(obj).isNotNull();
		} finally {
			// Cleanup after the test completes
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			testBundle.uninstall();
		}
	}
	
	@Test
	public void testReferenceLookupWithBuilderAndAttributes() throws Exception {
		// Install the bundles required for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("dirObjectFactoryBuilder1.jar"), true);
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialDirContextFactory1.jar"), true);
		// Create an appropriate context, store a reference, then attempt to
		// correctly lookup the reference.
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialDirContextFactory.class.getName());
		DirContext ctx = null;
		try {
			ctx = new InitialDirContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			CTReference reference = new CTReference(CTTestObject.class.getName());
			BasicAttributes attrs = new BasicAttributes("testAttributes", new Object());
			ctx.bind("reference", reference, attrs);
			CTTestObject obj = (CTTestObject) ctx.lookup("reference");
			Attributes testAttributes = ctx.getAttributes("reference");
			assertThat(obj).isNotNull();
			assertThat(attrs).isEqualTo(testAttributes);
		} finally {
			// Cleanup after the test completes
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			testBundle.uninstall();
		}
	}
	
	@Test
	public void testReferenceLookupWithBuilderAndNoFactory() throws Exception {
		// Install the bundles required for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("objectFactoryBuilder1.jar"), true);
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		// Create an appropriate context, store a reference, then attempt to
		// correctly lookup the reference.
		Context ctx = null;
		try {
			ctx = new InitialContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			CTReference reference = new CTReference(CTTestObject.class.getName());
			ctx.bind("reference", reference);
			CTTestObject obj = (CTTestObject) ctx.lookup("reference");
			assertThat(obj).isNotNull();
		} finally {
			// Cleanup after the test completes
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			testBundle.uninstall();
		}
	}
	
	@Test
	public void testReferenceLookupWithBuilderAndNoFactoryAndAttributes() throws Exception {
		// Install the bundles required for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("dirObjectFactoryBuilder1.jar"), true);
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialDirContextFactory1.jar"), true);
		// Create an appropriate context, store a reference, then attempt to
		// correctly lookup the reference.
		DirContext ctx = null;
		try {
			ctx = new InitialDirContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			CTReference reference = new CTReference(CTTestObject.class.getName());
			BasicAttributes attrs = new BasicAttributes("testAttributes", new Object());
			ctx.bind("reference", reference, attrs);
			CTTestObject obj = (CTTestObject) ctx.lookup("reference");
			Attributes testAttributes = ctx.getAttributes("reference");
			assertThat(obj).isNotNull();
			assertThat(attrs).isEqualTo(testAttributes);
		} finally {
			// Cleanup after the test completes
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
			testBundle.uninstall();
		}
	}
	
	@Test
	public void testObjectFactoryRemoval() throws Exception {
		// Install the bundle that has the test provider implementations
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("objectFactory1.jar"), true);
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		// Uninstall the bundle now so the provider implementations are
		// unregistered
		testBundle.uninstall();
		// Try to grab the ObjectFactory. We should get a NullPointerException
		Context ctx = null;
		try {
			ctx = new InitialContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			CTReference reference = new CTReference(CTTestObject.class.getName(), CTObjectFactory.class.getName());
			ctx.bind("reference", reference);
			// If no object factory is available for use, we should just return the reference.
			CTReference ref = (CTReference)ctx.lookup("reference");
			assertThat(ref).isNotNull();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
		}
	}

	@Test
	public void testObjectFactoryBuilderRemoval() throws Exception {
		// Install the bundle that has the test provider implementations
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("objectFactoryBuilder1.jar"), true);
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		// Uninstall the bundle now so the provider implementations are
		// unregistered
		testBundle.uninstall();
		// Try to grab the ObjectFactory. We should get a NullPointerException
		Context ctx = null;
		try {
			ctx = new InitialContext(env);
			assertThat(ctx).as("The context should not be null").isNotNull();
			CTReference reference = new CTReference(CTTestObject.class.getName(), CTObjectFactory.class.getName());
			ctx.bind("reference", reference);
			// If no object factory is available for use, we should just return the reference.
			CTReference ref = (CTReference)ctx.lookup("reference");
			assertThat(ref).isNotNull();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
		}
	}
}
