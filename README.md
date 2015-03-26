## Installation

### Install pip packages

```
pip install -r requirements.txt
```
Note that the above command may require root privileges (i.e. `sudo`).

## Usage

### Retrieve files from GitHub

```
fab deploy
```

#### Configuration

Edit `fabfile.py` with correct `config` variables that you would like to deploy to. Note that each host must already have this repository cloned at `~/RepoCrawler`.

### Check status on file retrieval

```
fab finished:YYYY-MM-DD,YYYY-MM-DD
```

Each server keeps track of dates they've completed checking/downloading repos for. This task will return which dates have not yet been retrieved for the given date range - first and last dates inclusive (first date is start date, second is last date).

### How many repositories have been downloaded?

```
fab stats
```