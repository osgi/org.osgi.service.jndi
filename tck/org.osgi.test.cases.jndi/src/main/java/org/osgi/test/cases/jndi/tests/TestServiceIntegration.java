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

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceException;
import org.osgi.framework.ServiceReference;
import org.osgi.test.cases.jndi.service.ExampleService;

/**
 * 
 * 
 * A set of tests for jndi interaction with the service registry
 * 
 * @author $Id$Date: 2009-07-08 14:07:59 -0400 (Wed, 08 Jul
 *          2009) $
 */
public class TestServiceIntegration extends JNDITestSupport {

	@Test
	public void testServiceLookup() throws Exception {
		// Install the bundle needed for this test
		Bundle serviceBundle = installer.installBundle(context.getBundle().getEntry("service1.jar"), true);
		// Grab the default initialContext so we can access the service registry
		Context ctx = null;
		try {
			ctx = new InitialContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			// Lookup the example service
			ExampleService service = (ExampleService) ctx.lookup("osgi:service/org.osgi.test.cases.jndi.service.ExampleService");
			// Verify that we actually got the service
			assertThat(service).isNotNull();
			service.testMethod();
			// Cleanup after the test completes
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			serviceBundle.uninstall();
		}
	}
	
	@Test
	public void testServiceLookupWithRebinding() throws Exception {
		// Install the bundle needed for this test
		Bundle serviceBundle = installer.installBundle(context.getBundle().getEntry("service1.jar"), true);
		ExampleService service = null;
		// Grab the default initialContext so we can access the service registry
		Context ctx = null;
		try {
			ctx = new InitialContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			// Lookup the example service
			service = (ExampleService) ctx.lookup("osgi:service/org.osgi.test.cases.jndi.service.ExampleService");
			// Verify that we actually got the service
			assertThat(service).isNotNull();
			service.testMethod();
			// Remove the service from the registry
			serviceBundle.uninstall();
			// Try to access the service.  This should throw an exception
			service.testMethod();
		} catch (ServiceException ex) {
			// Ignore this for now.  It's expected.
			System.out.println("Caught ServiceException in testServiceLookupWithRebinding and ignored it as it was an expected error.");
		}
		
		// Reinstall a bundle containing the service used by the proxy class
		installer.installBundle(context.getBundle().getEntry("service1.jar"), true);
		// See if the rebinding occurs as it should.
		try {
			service.testMethod();
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			
			if (serviceBundle.getState() == Bundle.INSTALLED) {
				serviceBundle.uninstall();
			}
		}
	}
	
	@Test
	public void testServiceLookupWithoutRebinding() throws Exception {
		// Install the bundle needed for this test
		Bundle serviceBundle = installer.installBundle(context.getBundle().getEntry("service1.jar"), true);
		// Grab the default initialContext so we can access the service registry
		Context ctx = null;
		try {
			ctx = new InitialContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			// Lookup the example service
			ExampleService service = (ExampleService) ctx.lookup("osgi:service/org.osgi.test.cases.jndi.service.ExampleService");
			// Verify that we actually got the service
			assertThat(service).isNotNull();
			service.testMethod();
			// Remove the service from the registry
			serviceBundle.uninstall();
			// Try to access the service.  This should throw an exception.
			service.testMethod();
		} catch (ServiceException ex) {
			if (ex.getType() != ServiceException.UNREGISTERED) {
				fail("testServiceLookupWithoutRebinding failed with wrong ServiceException type.  ServiceException was not of type UNREGISTERED.");
				return;
			}
			System.out.println("org.osgi.framework.ServiceException caught in testServiceLookupWithoutRebinding: SUCCESS");
			return;
		} finally {
			if (ctx != null) {
				ctx.close();
			}

			if (serviceBundle.getState() == Bundle.INSTALLED) {
				serviceBundle.uninstall();
			}
		}
		
		failException("testServiceLookupWithoutRebinding failed", org.osgi.framework.ServiceException.class);
	}
	
	@Test
	public void testMultipleServiceLookupWithListBindings() throws Exception {
		// Install the bundle needed for this test
		Bundle serviceBundle = installer.installBundle(context.getBundle().getEntry("service2.jar"), true);
		// Grab the default initialContext so we can access the service registry
		Context ctx = null;
		Context serviceListContext = null;
		try {
			ctx = new InitialContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			// Lookup the matching services
			serviceListContext = (Context) ctx.lookup("osgi:servicelist/org.osgi.test.cases.jndi.service.ExampleService");
			// Verify we received a context
			assertThat(serviceListContext).as("The context should not be null").isNotNull();
			// Check that the services we were expecting were found using the listBindings method
			ServiceReference< ? >[] expectedServices = serviceBundle
					.getRegisteredServices();
			for (int i=0; i < expectedServices.length; i++) {
				NamingEnumeration<Binding> retrievedServices = serviceListContext
						.listBindings("");
				boolean found = false;
				while (retrievedServices.hasMoreElements()) {
					Binding serviceBinding = retrievedServices.nextElement();
					String bindingName = serviceBinding.getName();
					if (bindingName.equals(expectedServices[i].getProperty("service.id").toString())) {
						found = true;
					}
				}
				if (!found) {
					fail("We expected a service with service.id=" + expectedServices[i].getProperty("service.id").toString() + " but none was found.");
				}
			}
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			if (serviceListContext != null) {
				serviceListContext.close();
			}
			serviceBundle.uninstall();
		}
		
	}
	
	@Test
	public void testMultipleServiceLookupWithList() throws Exception {
		// Install the bundle needed for this test
		Bundle serviceBundle = installer.installBundle(context.getBundle().getEntry("service2.jar"), true);
		// Grab the default initialContext so we can access the service registry
		Context ctx = null;
		Context serviceListContext = null;
		try {
			ctx = new InitialContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			// Lookup the matching services
			serviceListContext = (Context) ctx.lookup("osgi:servicelist/org.osgi.test.cases.jndi.service.ExampleService");
			// Verify we received the context
			assertThat(serviceListContext).as("The context should not be null").isNotNull();
			// Check that the service we were expecting were found using the list method
			ServiceReference< ? >[] expectedServices = serviceBundle
					.getRegisteredServices();
			for (int i=0; i < expectedServices.length; i++) {
				NamingEnumeration<NameClassPair> retrievedServices = serviceListContext
						.list("");
				boolean found = false;
				while (retrievedServices.hasMoreElements()) {
					NameClassPair servicePair = retrievedServices.nextElement();
					String pairName = servicePair.getName();
					if (pairName.equals(expectedServices[i].getProperty("service.id").toString())) {
						found = true;
					}
				}
				if (!found) {
					fail("We expected a service with service.id=" + expectedServices[i].getProperty("service.id").toString() + " but none was found.");
				}
			}
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			if (serviceListContext != null) {
				serviceListContext.close();
			}
			serviceBundle.uninstall();
		}
	}
	
	@Test
	public void testMultipleServiceLookupWithRebinding() throws Exception {
		// Install the bundle needed for this test
		Bundle serviceBundle = installer.installBundle(context.getBundle().getEntry("service1.jar"), true);
		// Grab the default initialContext so we can access the service registry
		Context ctx = null;
		Context serviceListContext = null;
		ExampleService service = null;
		try {
			ctx = new InitialContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			// Lookup the matching services
			serviceListContext = (Context) ctx.lookup("osgi:servicelist/org.osgi.test.cases.jndi.service.ExampleService");
			// Verify we received the context
			assertThat(serviceListContext).as("The context should not be null").isNotNull();
			// Get the service.id for the registered service we expect to get from serviceListContext
			ServiceReference< ? >[] expectedServices = serviceBundle
					.getRegisteredServices();
			ServiceReference< ? > exampleServiceReference = expectedServices[0];
			// Lookup the service in the returned serviceListContext
			service = (ExampleService) serviceListContext.lookup(((Long)exampleServiceReference.getProperty("service.id")).toString());
			// Remove the service that is being proxied
			serviceBundle.uninstall();
			// Try to call a method for this class
			service.testMethod();	
		} catch (ServiceException ex) {
			if (ex.getType() != ServiceException.UNREGISTERED) {
				fail("testMultipleServiceLookupWithRebinding failed with wrong ServiceException type.  ServiceException was not of type UNREGISTERED.");
				return;
			}
			// As long as we get the correct exception, do nothing else since there is still more testing.
		} 
		
		// Reinstall the service bundle
		installer.installBundle(context.getBundle().getEntry("service1.jar"), true);
		
		try {
			// Try to use the proxied service again.  This service should not have been rebound so we should still get an exception
			service.testMethod();
		} catch (ServiceException ex) {
			if (ex.getType() != ServiceException.UNREGISTERED) {
				fail("testMultipleServiceLookupWithRebinding failed with wrong ServiceException type.  ServiceException was not of type UNREGISTERED.");
				return;
			}
			System.out.println("org.osgi.framework.ServiceException caught in testMultipleServiceLookupWithRebinding: SUCCESS");
			return;
		} finally {
			if (ctx != null) {
				ctx.close();
			}
			if (serviceListContext != null) {
				serviceListContext.close();
			}
			if (serviceBundle.getState() == Bundle.INSTALLED) {
				serviceBundle.uninstall();
			}
		}
		
		failException("testMultipleServiceLookupWithRebinding failed", org.osgi.framework.ServiceException.class);
	}

	@Test
	public void testBundleContextLookup() throws Exception {
		// Grab the default initialContext so we can access the service registry
		Context ctx = null;
		try {
			ctx = new InitialContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			// Lookup the bundle context
			BundleContext bundleCtx = (BundleContext) ctx.lookup("osgi:framework/bundleContext");
			assertThat(bundleCtx).as("The bundle context should not be null").isNotNull();
			assertThat(context).isEqualTo(bundleCtx);
		} finally {
			if (ctx != null) {
				ctx.close();
			}
		}
	}
	
	@Test
	public void testServiceNameProperty() throws Exception {
		// Install the bundles need for this test
		Bundle serviceBundle = installer.installBundle(context.getBundle().getEntry("service1.jar"), true);
		// Grab the default InitialContext so we can access the service registry
		Context ctx = null;
		try {
			ctx = new InitialContext();
			assertThat(ctx).as("The context should not be null").isNotNull();
			// Lookup the example service using the service name
			ExampleService service = (ExampleService) ctx.lookup("osgi:service/ExampleService");
			// Verify that we actually got the service
			assertThat(service).isNotNull();
		} finally {
			// Cleanup after the test completes
			if (ctx != null) {
				ctx.close();
			}
			serviceBundle.uninstall();
		}
	}

}
