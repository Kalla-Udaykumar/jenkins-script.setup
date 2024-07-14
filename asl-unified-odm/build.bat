echo workspace is: %WORKSPACE%
echo working_dir is: %WORKING_DIR%
copy /Y %WORKSPACE%\\abi\\BIOS_Manifest.json %WORKING_DIR%\\IFWI_Automation\\ASL\\INPUT\\STATIC\\IFWI_UNZIP
cd %WORKING_DIR%\\IFWI_Automation\\Builder
py one_darzi.py ASL
