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

print(f"WORKING_DIR: {WORKING_DIR}")
print(f"WORKSPACE: {WORKSPACE}")

s = requests.Session()
r = s.get('https://ubit-artifactory-ba.intel.com/artifactory/one-windows-local/Submissions/ifwi/', auth=HTTPBasicAuth(username, password), stream=True)
soup = BeautifulSoup(r.text, 'html.parser')

links = soup.find_all("a")
# ifwi_version
for link in links:
        if ("ifwi_arl_h_a0_pp_release_2024" in str(link)) and ("_win" not in str(link)):
                      #print(link.get('href'))
                      IFWI_VERSION = link.get('href')[0:-1]

# bios_version 
with open(WORKING_DIR + "\\IFWI_Automation\\Builder\\bios_version.txt", "r") as f:
    for line in f:
#        BIOS_VERSION = line
        BIOS_VERSION = 4134_42

# Save IFWI_VERSION to a file in the Jenkins workspace
ifwi_version_file_path = os.path.join(reports_dir, "ifwi_version.txt")
with open(ifwi_version_file_path, "w") as f:
    f.write(IFWI_VERSION)

# Save BIOS_VERSION to a file in the Jenkins workspace
bios_version_file_path = os.path.join(reports_dir, "bios_version.txt")
with open(bios_version_file_path, "w") as f:
#    f.write(BIOS_VERSION)
     f.write(4134_42)

print("BIOS_VERSION is :", BIOS_VERSION)
print("IFWI_VERSION is :" + IFWI_VERSION)
print("py ARL_IFWI_download.py ARL_H " + IFWI_VERSION + " Pre_Production")

os.chdir(WORKING_DIR + "\\IFWI_Automation\\ARL\\test\\")
dwnld_return_value = os.system("py ARL_IFWI_download.py ARL_H " + IFWI_VERSION + " Pre_Production")
# if dwnld_return_value != 0:
#       sys.exit(-1)
