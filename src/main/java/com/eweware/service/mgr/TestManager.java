package com.eweware.service.mgr;

import com.eweware.service.base.mgr.ManagerInterface;

/**
 * @author rk@post.harvard.edu
 *         Date: 7/10/12 Time: 3:53 PM
 */
public class TestManager implements ManagerInterface {

    public TestManager() {
        System.out.println("*** TestManager initialized ***");
    }

    @Override
    public void start() {
       System.out.println("*** TestManager started ***");
    }

    @Override
    public void shutdown() {
        System.out.println("*** TestManager shut down ***");
    }
}
