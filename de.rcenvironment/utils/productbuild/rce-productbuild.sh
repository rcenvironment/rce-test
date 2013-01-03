#!/bin/bash

# Bash script for building standard RCE releases. 
# Author: Robert Mischke 
# Copyright (C) 2011-2012 DLR, Germany
#
# This script requires 2 parameters:
# - platform { win-x86, linux-x86 }
# - build type { snapshot, release }
# - the version string to tag the resulting archive with 
# - the repository to install from; if more than one, join with commas
#
# Additionally, FTP_UPLOAD_PASSWORD must contain the password 
# for the sc@ftp.dlr.de account
#

# check assumptions
if [ ! which ncftpput 2> /dev/null ]; then 
	echo "ncftpput not found"
	exit 1;
fi
if [ "empty$FTP_UPLOAD_PASSWORD" == "empty" ]; then
	echo "FTP_UPLOAD_PASSWORD not set"
	exit 2;
fi

# set common build parameters
RELEASE_PLATFORM=$1
RELEASE_TYPE=$2
VERSION_TAG=$3
RELEASE_ROOT_NAME=rcenvironment-${VERSION_TAG}

# the release group (for example, FTP or update site root path)
# FIXME derive this from VERSION_TAG or make it configurable
VERSION_GROUP=2.2

BASE_IMAGE_URL_ROOT=\
http://updates.sc.dlr.de/internal/rce/templates/2.1

UPLOAD_FTP_ROOT_DIRECTORY=\
/public/download/RCE

REPOSITORIES=$4

OUTPUT_FILENAME_ROOT=\
$RELEASE_ROOT_NAME-$RELEASE_TYPE-$RELEASE_PLATFORM-TYCHOTEST-`date +%Y%m%d-%H%M`

LATEST_RELEASE_INFOFILE=\
$RELEASE_ROOT_NAME-$RELEASE_TYPE-$RELEASE_PLATFORM-TYCHOTEST-latest.txt

# define features
COMMON_RCE_FEATURES=\
de.rcenvironment.rce.feature.base.feature.group,\
de.rcenvironment.rce.feature.branding.gui.feature.group,\
de.rcenvironment.rce.feature.branding.gui.login.feature.group,\
de.rcenvironment.rce.feature.branding.gui.workflow.feature.group,\
de.rcenvironment.rce.feature.communication.feature.group,\
de.rcenvironment.rce.feature.components.feature.group,\
de.rcenvironment.rce.feature.datamanagement.feature.group,\
de.rcenvironment.rce.feature.workflow.feature.group

WIN32_RCE_FEATURES=\
$COMMON_RCE_FEATURES,\
de.rcenvironment.rce.feature.components.win32.feature.group

NEW_FEATURES=\
de.rcenvironment.rce.platform.feature.feature.group,\
de.rcenvironment.rce.feature.core.feature.group,\
de.rcenvironment.rce.feature.core.gui.feature.group,\
de.rcenvironment.rce.feature.branding.default.feature.group,\
de.rcenvironment.rce.feature.components.examples.feature.group

# export some of these variables (split this way for easier reading)
export RELEASE_PLATFORM
export RELEASE_TYPE
export REPOSITORIES
export OUTPUT_FILENAME_ROOT

# select platform template/image
case "$RELEASE_PLATFORM" in
	'win-x86')
		export BASE_IMAGE_NAME=de.rcenvironment-2.1.0-win32-x86_SNAPSHOT20110513.zip
		;;
	'win-x86_64')
		export BASE_IMAGE_NAME=de.rcenvironment-2.1.0-win32-x86_64_SNAPSHOT20110513.zip
		;;
	'linux-x86')
		export BASE_IMAGE_NAME=de.rcenvironment-2.1.0-linux-gtk-x86_SNAPSHOT20110513.tar.gz
		;;
	'linux-x86_64')
		export BASE_IMAGE_NAME=de.rcenvironment-2.1.0-linux-gtk-x86_64_SNAPSHOT20110513.tar.gz
		;;
	*)
		echo "Unrecognized platform: $RELEASE_PLATFORM"
		exit 3
		;;
esac
		
# general win/linux defines
if [[ $RELEASE_PLATFORM == win* ]]; then
  export UNINSTALL_FEATURES=$WIN32_RCE_FEATURES
  export INSTALL_FEATURES=$NEW_FEATURES
  export BASE_IMAGE_DOWNLOAD_URL=$BASE_IMAGE_URL_ROOT/$BASE_IMAGE_NAME
  # TODO change to zip when available on server
  export OUTPUT_FILENAME=$OUTPUT_FILENAME_ROOT.tgz
else
  export UNINSTALL_FEATURES=$COMMON_RCE_FEATURES
  export INSTALL_FEATURES=$NEW_FEATURES
  export BASE_IMAGE_DOWNLOAD_URL=$BASE_IMAGE_URL_ROOT/$BASE_IMAGE_NAME
  export OUTPUT_FILENAME=$OUTPUT_FILENAME_ROOT.tgz
fi

# invoke core build
sh rce-productbuild-core.sh

if [ "$?" -ne "0" ] ; then
	echo "Error in core product build script"
	exit 1
fi

# create "latest" file
echo "$OUTPUT_FILENAME_ROOT" > $LATEST_RELEASE_INFOFILE

# upload
ncftpput -u sc -p $FTP_UPLOAD_PASSWORD ftp.dlr.de $UPLOAD_FTP_ROOT_DIRECTORY/${RELEASE_TYPE}/${VERSION_GROUP}/ target/$OUTPUT_FILENAME
ncftpput -u sc -p $FTP_UPLOAD_PASSWORD ftp.dlr.de $UPLOAD_FTP_ROOT_DIRECTORY/${RELEASE_TYPE}/${VERSION_GROUP}/ $LATEST_RELEASE_INFOFILE
