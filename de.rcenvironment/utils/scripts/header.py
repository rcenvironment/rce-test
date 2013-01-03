import glob
import os
import sys
import time

def searchForJava(dirname):
	searchPath = os.path.join(dirname, "*.java")
	toConvert = glob.glob(searchPath)
	return toConvert

def scanDir(path):
	toConvertList.extend(searchForJava(path))
	scanFolderList = os.listdir(path)
	for f in scanFolderList:
		file = os.path.join(path, f)
		if os.path.isdir(file):
			scanDir(file)
	return toConvertList
	
def write(file, string):
	try:
		f = open(file, "w")
		try:
			f.write('')
			f.write(string)
		finally:
			f.close()
	except IOError:
		return "false"
	return "true"
	
def getCorrectHeader(file):
	try:	
		f = open(file, "r")
		try:
			completeFile = ""
			completeFile = f.read()
		finally:
			f.close()
	except IOError:
		return "false"  	
	completeFile = completeFile.split("package",1)
	if len(completeFile) == 2:
		completeFile[0] = """/*
 * Copyright (C) 2006-""" + str(time.localtime().tm_year) + """ DLR, Fraunhofer SCAI, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

"""
		return "package".join(completeFile)
	return "false"

def removeLineInclude(completeFile, value):
	completeFile = completeFile.split("\n")
	i = 0
	found = 0
	for string in completeFile:
		if string.find(value) > -1:
			found = 1
			del completeFile[i]
			if (i == len(completeFile)):
				break
		i = i + 1
	if found == 1:
		completeFile = "\n".join(completeFile)
		return completeFile
	return "null"

def getCorrectJavaDoc(file):
	try:	
		f = open(file, "r")
		try:
			completeFile = ""
			completeFile = f.read()
		finally:
			f.close()
	except IOError:
		return "false"
	completeFile = removeLineInclude(completeFile, "$LastChangedRevision")
	return completeFile

def convertList(type):
	# start for each File the "type" specific conversion
	result = ""
	for javaFile in toConvertList:
		if type == "Header":
			stringFile = getCorrectHeader(javaFile)
		if type == "JavaDoc":
			stringFile = getCorrectJavaDoc(javaFile)
		if stringFile == "null": 							# Der File enthaelt die Value nicht
			result = "null"
		elif stringFile != "false": 					# Value wurde erfolgreich entfernt
			if write(javaFile, stringFile) == "true":
				result = "true"
		else:
			result = "false"										# Der File konnte nicht geoeffnet werden
			
		#save results in the dictionary
		try:
			resultDict.keys().index(javaFile)
			resultDict[javaFile][type] = result
		except ValueError:
			resultDict[javaFile] = {type : result}
	
def showList():
	i = 0
	for s in toConvertList:
		i = i + 1
	print i, "Files to Convert\n-------------------\n"

def check(type):
	print "Handle ", type, "!\n"
	convertList(type)
	for dict in resultDict.values():
		if dict[type] == "false":
			print "The ", type, " couldn't be handled in the following files:\n"
			print "### BEGIN LIST ###"
			break
	convertCount = 0
	i = 0
	for dict in resultDict.values():
		if dict[type] == "false":
			print resultDict.keys()[i]
		if dict[type] == "true":
			convertCount = convertCount + 1
		i = i + 1
	for dict in resultDict.values():
		if dict[type] == "false":
			print "### END LIST ###\n"
			print convertCount, " files wrote!\n-------------------\n"
			break
	else:
		print type, " Succeeded!\n"		
		print convertCount, " files wrote!\n-------------------\n"
		
### Main ###

print "\n### Control Java Header (and JavaDoc) ###\n\nSet new header with year\nand delete line with\n\"$LastChangedRevision\"\n-----------------------------------------\n"

if len(sys.argv) == 1:
	var = raw_input("Enter the directory to check: ")
else:
	var = sys.argv[1]
if var == "" or var == "exit" or var == "quit":
	sys.exit()

toConvertList = []
toConvertList = scanDir(var)

resultDict = ({})
showList()
check("Header")
check("JavaDoc")