echo workspace is: %WORKSPACE%
echo working_dir is: %WORKING_DIR%
cd %WORKING_DIR%\\IFWI_Automation\\ARL\\test\\
py ARL_IFWI_download.py ARL_H %IFWI_VERSION% Pre_Production
copy /Y %WORKSPACE%\\abi\\BIOS_Manifest.json %WORKING_DIR%\\IFWI_Automation\\ARL\\ARL_H\\INPUT\\STATIC\\IFWI_UNZIP
cd %WORKING_DIR%\\IFWI_Automation\\ARL\\test\\
py ARL_one_darzi.py ARL_H
