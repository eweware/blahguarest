#!/usr/bin/python

import subprocess
import pexpect,getpass, os

# Pull data from git
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
elif index == 1:
    while (True):
        line = c.readline()
        if line == '': break
        print line.strip("\r\n")
    subprocess.Popen(['sh','-c', 'cd', '..'], stdout=subprocess.PIPE).communicate()[0]
    print 'Building...'
    c = pexpect.spawn('mvn -f ../pom.xml clean package')
    while (True):
        line = c.readline()
        if line == '': break
        print line.strip("\r\n")
    print 'Done!'
elif index == 2:
    print 'Timeout out waiting for git pull to complete'


