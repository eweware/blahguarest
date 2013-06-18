#!/usr/bin/python

'''
rk 6/18/2013 Created

Gets latest sources from git.
Ensures configuration is for QA.
Builds the package.
Deploys the war to the QA server.
'''

import subprocess
import pexpect,getpass, os, sys
import myPxssh
import time
import re

def main(argv=None):

    try:
        print 'Pulling latest from git'
        c = pexpect.spawn('git pull')
        print 'git pull'
        c.expect('Username .*: ')
        c.sendline('rk@eweware.com')
        print c.after
        c.expect('Password .*: ')
        c.sendline('Roothekoo1!')
        print c.after
        index = c.expect(['Already .*','remote: Counting .*', pexpect.TIMEOUT])
        if index == 0:
            print c.after
            print 'All up to date'
        elif index == 1:
            while (True):
                line = c.readline()
                if line == '': break
                print line.strip("\r\n")
            print 'New git version fetched'
        elif index == 2:
            print 'Timeout out waiting for git pull to complete'
            print 'Try again or build manually'
            return 1
        print 'Building war...'
        subprocess.Popen(['sh','-c', 'cd', '..'], stdout=subprocess.PIPE).communicate()[0]
        c = pexpect.spawn('mvn -f ../pom.xml clean package')
        while (True):
            line = c.readline()
            if line == '': break
            print line.strip("\r\n")

    except Exception as e:
        print str(e)
        return 2

    if True:
        print 'Checking configuration...'
        configFilepath = '../src/main/webapp/WEB-INF/conf/blahguaConfiguration.properties'
        config = open(configFilepath)
        for prop in config:
            s = re.search('^blagua.run.mode=(.+?)$', prop)
            if s:
                runMode = s.group(1)
                if runMode.lower() != 'qa':
                    print 'Config file not set for QA release!'
                    print "Change line 'blahgua.run.mode="+runMode+"' to 'blahgua.run.mode=qa'"
                    print 'Then try again.'
                    return 1
                else:
                    print 'Checked config file and confirmed that run mode is QA'

    if True:
        try:
            war = '../target/api-0.9.0-SNAPSHOT.war'
            print 'Pushing '+war+' to QA REST server...'
            keypair = os.environ['OREGON']
            response = subprocess.Popen(['scp','-i', keypair, war, 'ec2-user@qa.rest.blahgua.com:/home/ec2-user/ROOT.war'], stdout=subprocess.PIPE).communicate()[0]
            print response

            s = myPxssh.pxssh()
            s.login ('qa.rest.blahgua.com', 'ec2-user', keypair_pathname=keypair)

            print 'Stopping tomcat service...'
            s.sendline('sudo service tomcat7 stop')
            s.prompt()
            print s.before
            time.sleep(5)
            print 'Removing current version from container...'
            s.sendline('sudo rm -rf /usr/share/tomcat7/webapps/ROOT*')
            s.prompt()
            print s.before
            print 'Copying new war to container...'
            s.sendline('sudo mv ROOT.war /usr/share/tomcat7/webapps/.')
            s.prompt()
            print s.before
            print 'Setting war contents ownership to tomcat...'
            s.sendline('sudo chown tomcat:tomcat /usr/share/tomcat7/webapps/ROOT.war')
            s.prompt()
            print 'Starting tomcat service...'
            s.sendline('sudo service tomcat7 start')
            s.prompt()
            print s.before
            print 'Waiting about one and a half minutes before showing startup log...'
            for i in range(9):
                incr = 10
                print str(90 - i*incr)+' seconds left...'
                time.sleep(incr)
            s.sendline('sudo tail -n200 /var/log/tomcat7/catalina.out')
            s.prompt()
            print s.before
            print 'Done!'
            return 0

        except Exception as e:
            print str(e)
            return 2
    else:
        return 1

    return 0

if __name__ == "__main__":
    sys.exit(main())
