import os
import sys
import json
import requests
from requests.auth import HTTPBasicAuth
from bs4 import BeautifulSoup

username = os.getenv('CREDS_USR')
password = os.getenv('CREDS_PSW')
WORKING_DIR = os.getenv('WORKING_DIR')
WORKSPACE = os.getenv('WORKSPACE')

# Create the directory if it doesn't exist
reports_dir = os.path.join(WORKSPACE, "abi", "reports")
os.makedirs(reports_dir, exist_ok=True)

print(WORKING_DIR)
print(WORKSPACE)

s = requests.Session()
r = s.get('https://ubit-artifactory-ba.intel.com/artifactory/one-windows-local/Submissions/ifwi/', auth=HTTPBasicAuth(username, password), stream=True)
soup = BeautifulSoup(r.text, 'html.parser')

links = soup.find_all("a")
# ifwi_version 
for link in links:
        if ("adl_n_win11_mr_blue_ifwi_2022_ww52_4_01" in str(link)) and ("_v" not in str(link)):
                    #print(link.get('href'))
                    IFWI_VERSION = link.get('href')[0:-1]

# bios_version					
with open(WORKING_DIR + "\\IFWI_Automation\\Builder\\bios_version_internal.txt", "r") as f:
    for line in f:
        BIOS_VERSION = line

# Save IFWI_VERSION to a file in the Jenkins workspace
ifwi_version_file_path = os.path.join(reports_dir, "ifwi_version.txt")
with open(ifwi_version_file_path, "w") as f:
    f.write(IFWI_VERSION)

# Save BIOS_VERSION to a file in the Jenkins workspace
bios_version_file_path = os.path.join(reports_dir, "bios_version.txt")
with open(bios_version_file_path, "w") as f:
    f.write(BIOS_VERSION)

print("IFWI_VERSION is :" + IFWI_VERSION)
print("py ASL_IFWI_download_unzip.py " + IFWI_VERSION + " Pre_Production" + " Production")

os.chdir(WORKING_DIR + "\IFWI_Automation\Builder")
dwnld_return_value = os.system("py ASL_IFWI_download_unzip.py " + IFWI_VERSION + " Pre_Production" + " Production")
if dwnld_return_value != 0:
	sys.exit(-1)
