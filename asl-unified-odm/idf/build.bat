echo workspace is: %WORKSPACE%
echo working_dir is: %WORKING_DIR%
copy /Y %WORKSPACE%\\abi\\BIOS_Manifest.json %WORKING_DIR%\\IFWI_Automation\\Unified_IFWI\\ASL\\INPUT\\STATIC\\IFWI_UNZIP
cd %WORKING_DIR%\\IFWI_Automation\\Unified_IFWI\\Builder
py one_darzi.py ASL
