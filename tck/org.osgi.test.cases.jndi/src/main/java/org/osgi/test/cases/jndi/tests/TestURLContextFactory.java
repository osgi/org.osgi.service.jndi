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
import javax.naming.NamingException;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.test.cases.jndi.provider.CTInitialContextFactory;

/**
 * 
 * 
 * A set of tests for URL operations in jndi
 * 
 * @author $Id$Date: 2009-07-08 13:20:41 -0400 (Wed, 08 Jul
 *          2009) $
 */
public class TestURLContextFactory extends JNDITestSupport {

	@Test
	public void testURLContextFactoryRegistration() throws Exception {
		// Install the bundles needed for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("urlContext1.jar"), true);
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		// Setup the standard context that we'll be using the URL to access so
		// that we have something to lookup
		String testString = "test string";
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		Context ctx = new InitialContext(env);
		Context urlCtx = new InitialContext();
		try {
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testString", testString);
			// Attempt to look up the test string
			String result = (String) urlCtx.lookup("ct://testString");
			assertThat(result).isEqualTo(testString);
		} finally {
			// Cleanup after the test
			if (ctx != null) {
				ctx.close();
			}
			if (urlCtx != null) {
				urlCtx.close();
			}
			factoryBundle.uninstall();
			testBundle.uninstall();
		}
	}

	@Test
	public void testURLContextFactoryRemoval() throws Exception {
		// Install the bundles needed for this test
		Bundle testBundle = installer.installBundle(context.getBundle().getEntry("urlContext1.jar"), true);
		Bundle factoryBundle = installer.installBundle(context.getBundle().getEntry("initialContextFactory1.jar"), true);
		// Setup the standard context that we'll be using the URL to access so
		// that we have something to lookup
		String testString = "test string";
		Hashtable<String,Object> env = new Hashtable<>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, CTInitialContextFactory.class.getName());
		Context ctx = new InitialContext(env);
		Context urlCtx = new InitialContext();
		try {
			assertThat(ctx).as("The context should not be null").isNotNull();
			ctx.bind("testString", testString);
			// Remove the url context jar
			testBundle.uninstall();
			String result = (String) urlCtx.lookup("ct://testString");
			assertThat(result).isEqualTo(testString);
		} catch (NamingException ex) {
			// We're expecting this, since they're should be no context able to
			// handle this url
			return;
		} finally {
			if (urlCtx != null) {
				urlCtx.close();
			}
			if (ctx != null) {
				ctx.close();
			}
			factoryBundle.uninstall();
		}
		failException("testURLContextFactoryRemoval failed, ", javax.naming.NamingException.class);
	}
}
