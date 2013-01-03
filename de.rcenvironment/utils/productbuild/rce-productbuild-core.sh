#!/bin/bash

# Common bash script for building RCE and derived products.
# Author: Robert Mischke 
# Copyright (C) 2011-2012 DLR, Germany
#
# NOTE: the current version of this script requires 
# a pre-existing RCE release (BASE_IMAGE) 
#
# Required environment variables are:
# - RELEASE_PLATFORM
# - BASE_IMAGE_NAME
# - BASE_IMAGE_DOWNLOAD_URL
# - REPOSITORIES
# - UNINSTALL_FEATURES
# - INSTALL_FEATURES
# - OUTPUT_FILENAME_ROOT (filename without extension)
#
# If the optional parameter CONFIGURATION_INPUT_FOLDER is set, the specified
# configuration folder is used instead of the default configuration.
#

# if a product build root is defined, go there
if [ "empty$PRODUCT_BUILD_ROOT" != "empty" ]; then
  cd $PRODUCT_BUILD_ROOT 
fi

# get current work directory from pwd to normalize it
WORKDIR_ROOT=`pwd`
TEMP_SUBDIR=$WORKDIR_ROOT/temp
OUTPUT_SUBDIR=$WORKDIR_ROOT/target
echo "Using work directory root $WORKDIR_ROOT"

# download eclipse image if not present yet
wget -nc http://updates.sc.dlr.de/internal/eclipse/mirror/images/eclipse-java-galileo-SR2-linux-gtk.tar.gz
# download base image if not present yet
wget -nc $BASE_IMAGE_DOWNLOAD_URL

# clean up and prepare environment
echo "Cleaning work area..."
rm -rf $TEMP_SUBDIR
mkdir -p $TEMP_SUBDIR
mkdir -p $OUTPUT_SUBDIR

cd $TEMP_SUBDIR

# extract 
echo "Extracting builder..."
tar -xf $WORKDIR_ROOT/eclipse-java-galileo-SR2-linux-gtk.tar.gz

echo "Extracting product template..."
# note: double brackets are required for pattern matching
if [[ $RELEASE_PLATFORM == win* ]]
then
  unzip $WORKDIR_ROOT/$BASE_IMAGE_NAME
else
  tar -xf $WORKDIR_ROOT/$BASE_IMAGE_NAME
fi

if [ "$?" -ne "0" ]
then
	echo "Failed to extract base image"
	exit 1
fi

# set local parameters
P2_DIRECTOR_ECLIPSE=$TEMP_SUBDIR/eclipse/eclipse
# resolve path as P2 director fails on relative paths
TARGET_INSTALLATION_DIR=$TEMP_SUBDIR/rce

# perform modifications
mv $TARGET_INSTALLATION_DIR/rce.ini $TARGET_INSTALLATION_DIR/eclipse.ini
# copy EPL license file; this is a quick fix for RCE releases, and will do nothing if the file is not present in the initial directory
cp $WORKDIR_ROOT/epl-v10.html $TARGET_INSTALLATION_DIR/ 

echo "Installing release features..."
$P2_DIRECTOR_ECLIPSE -noSplash -application org.eclipse.equinox.p2.director -destination $TARGET_INSTALLATION_DIR -repository $REPOSITORIES -uninstallIU $UNINSTALL_FEATURES -installIU $INSTALL_FEATURES

# remove old plugin and feature files; not done by p2 director
# NOTE: this must be adapted when the base image changes
find $TARGET_INSTALLATION_DIR -name '*2011051200*' -print0 | xargs -0 rm -rf

# if a custom configuration path is set, clear the configuration directory and copy the files
if [ "empty$CONFIGURATION_INPUT_FOLDER" != "empty" ]
then
  echo "Using custom CONFIGURATION_INPUT_FOLDER $CONFIGURATION_INPUT_FOLDER"
  # delete old RCE configuration files
  rm -rf $TARGET_INSTALLATION_DIR/configuration/de.rcenvironment.*
  # copy custom configuration 
  cp -R $CONFIGURATION_INPUT_FOLDER/* $TARGET_INSTALLATION_DIR/configuration
  # delete .svn folders that were copied from the checkout area  
  find . -name ".svn" -print0 | xargs -0 rm -rf
else
  echo "No custom CONFIGURATION_INPUT_FOLDER set, keeping default files" 
fi

# create final archive
# TODO use zip for win32 as soon it is available on all build nodes
tar -czf $OUTPUT_SUBDIR/$OUTPUT_FILENAME_ROOT.tgz rce

# preserve log files, if any
cp $TEMP_SUBDIR/*.log $WORKDIR_ROOT 

# cleanup
cd $WORKDIR_ROOT
# NOTE: "rm -rf" considered safe with relative path of "temp" 
rm $TEMP_SUBDIR -rf
