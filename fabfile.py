from __future__ import with_statement
from fabric.api import *

# These are all preset to connect to AWS machines in ~/.ssh/config
env.hosts = ['java1', 'java2', 'java3', 'java4', 'java5']

# Force parallel execution
env.parallel = True
env.use_ssh_config = True

# Configuration
config = {
    'java1': { 'start': '2010.01.01', 'end': '2010.12.31' },
    'java2': { 'start': '2011.01.01', 'end': '2011.12.31' },
    'java3': { 'start': '2012.01.01', 'end': '2012.12.31' },
    'java4': { 'start': '2013.01.01', 'end': '2013.12.31' },
    'java5': { 'start': '2014.01.01', 'end': '2014.12.31' }
}

def deploy():
  with cd('~/RepoCrawler'):
    run('git checkout cmd-line-args')
    run('git pull')
    run('java -jar repoCrawler.jar ' +
      config[env.host_string]['start'] + ' ' +
      config[env.host_string]['end'] + ' S3.properties')