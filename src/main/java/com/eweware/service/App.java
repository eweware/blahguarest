package com.eweware.service;

import com.sun.jersey.api.core.PackagesResourceConfig;

/**
 * @author rk@post.harvard.edu
 *
 * REST Jersey resource configuration
 */
public class App extends PackagesResourceConfig {

	public App() {
        // All REST resource classes must be in the following package:
		super("com.eweware.service.rest.resource");
	}
}
