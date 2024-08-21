#!/bin/bash
#######################################################################################
## This script is use to copy jammy content in noble artifcatory.
#######################################################################################
## Primary Author: Patel, Tejas
## Passing arguments 1: <jammy dir path>
## How to run nobleppa.sh: sudo ./nobleppa.sh ../../../jammy/jammy/20240818-0024
#######################################################################################

#Validate passing arguments
if [ $# -le 0 ]; then
    echo "ERROR: Illegal number of parameters"
	usage
fi

jammyDebFolderPath="$1/pool/*"

#loop for main, internal, non-free, multimedia
for p in $jammyDebFolderPath
do
	folderName=`echo "$p" | awk -F/ '{print $NF}'`
	path="$p/*"
	if [ -d "$p" ]; then
		#loop for deb package folder
		for q in $path
		do
			echo "2 loop $q"
			if [ -d "$q" ]; then
				#echo $q
				#loop for deb package name folder
				path="$q/*"
				for r in $path
				do
					echo "3 loop $r"
					#loop for deb packages
					for i in $r/*.dsc; do [ -e "$i" ] || continue; echo $i && reprepro -C $folderName includedsc jammy $i ; done
					for i in $r/*.deb; do [ -e "$i" ] || continue; echo $i && reprepro -C $folderName includedeb jammy $i ; done
					for i in $r/*.ddeb; do [ -e "$i" ] || continue; echo $i && reprepro -C $folderName includeddeb jammy $i ; done
				done
			fi
		done
	fi
done
