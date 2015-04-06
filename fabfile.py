from __future__ import with_statement
from fabric.api import *
from datetime import date, timedelta
import boto

# 'java#' machines are preset to connect to AWS machines in ~/.ssh/config
# Can change these 
env.roledefs.update({
    'aws': ['java1', 'java2', 'java3', 'java4', 'java5']
})

env.use_ssh_config = True

config = {
    'java1': { 'start': '2010.01.01', 'end': '2010.12.31' },
    'java2': { 'start': '2011.01.01', 'end': '2011.12.31' },
    'java3': { 'start': '2012.01.01', 'end': '2012.12.31' },
    'java4': { 'start': '2013.01.01', 'end': '2013.12.31' },
    'java5': { 'start': '2014.01.01', 'end': '2014.12.31' }
}

@task
@parallel
@roles('aws')
def deploy():
  with cd('~/RepoCrawler'):
    run('git checkout master')
    run('git pull')
    run('java -jar repoCrawler.jar ' +
      config[env.host_string]['start'] + ' ' +
      config[env.host_string]['end'] + ' S3.properties')

# Return statistics for file downloaded
@task
def stats():
  print "Connecting to S3..."
  s3 = boto.connect_s3()
  bucket = s3.get_bucket('umich-dbgroup')
  repo_iterator = bucket.list(prefix="cjbaik/java-corpus/", delimiter="/")
  file_iterator = bucket.list(prefix="cjbaik/java-corpus/")

  repo_index = 0

  print "Iterating through repositories..."
  for key in repo_iterator:
    repo_index += 1
    if (repo_index % 1000) == 0:
      print "Found " + str(repo_index) + " repositories so far..."

  file_index = 0
  file_size = 0

  print "Iterating through files..."
  for key in file_iterator:
    file_index += 1
    file_size += key.size
    if (key.size > 1000000):
      files_exceeding_mb += "\t" + str(key) + "\n"
    if (file_index % 5000) == 0:
      print "Found " + str(file_index) + " files so far..."

  print "Total repositories found: " + str(repo_index)
  print "Total number of files found: " + str(file_index)
  print "Total file size: " + str(file_size)

# Check if RepoCrawler is finished for a date range
@task
def finished(begin_date_string=None, end_date_string=None):
  if begin_date_string is None or end_date_string is None:
    print 'Example execution: fab finished:2010-01-10,2010-12-31'
    return

  begin_date_year, begin_date_month, begin_date_day = begin_date_string.split('-')
  end_date_year, end_date_month, end_date_day = end_date_string.split('-')

  begin_date = date(int(begin_date_year), int(begin_date_month), int(begin_date_day))
  end_date = date(int(end_date_year), int(end_date_month), int(end_date_day))

  finished_dates = []
  dates_from_servers = execute(retrieve_finished_from_servers)

  for server, dates_per_server in dates_from_servers.iteritems():
    finished_dates = finished_dates + dates_per_server.splitlines()

  finished_dates.sort()

  if len(finished_dates) < 0:
    raise Exception('No dates retrieved from server.')

  missing_dates = []

  current_date = begin_date
  while True:
    current_date = current_date + timedelta(days=1)
    if current_date > end_date:
        break
    if current_date.isoformat() not in finished_dates:
      missing_dates.append(current_date)

  if len(missing_dates) == 0:
    print 'RepoCrawler has saved all results for the range: ' + begin_date.isoformat() + ' to ' + end_date.isoformat()
  else:
    print 'Dates that are missing from the range: ' + begin_date.isoformat() + ' to ' + end_date.isoformat()
    for missing_date in missing_dates:
      print missing_date.isoformat()

# Helper task for `finished`
@task
@parallel
@roles('aws')
def retrieve_finished_from_servers():
  with cd('~/RepoCrawler'):
    stdout = run('cat finished.log')
  return stdout
