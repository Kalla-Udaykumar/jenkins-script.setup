echo workspace is: %WORKSPACE%
echo working_dir is: %WORKING_DIR%
copy /Y %WORKSPACE%\\abi\\BIOS_Manifest.json %WORKING_DIR%\\IFWI_Automation\\ARL\\ARL_H\\INPUT\\STATIC\\IFWI_UNZIP
cd %WORKING_DIR%\\IFWI_Automation\\ARL\\test\\
py ARL_one_darzi.py ARL_H ifwi_arl_h_a0_pp_release_2024ww17.2.02 Pre_Production
