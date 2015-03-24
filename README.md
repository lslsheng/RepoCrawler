## Usage

### Install Fabric

```
pip install fab
```
Note that above command may require root privileges.

### Configuration

Edit `fabfile.py` with correct `env.hosts` and `config` variables that you would like to deploy to. Note that each host must already have this repository cloned at `~/RepoCrawler`.

### Execution

```
fab deploy
```
