# This is an example script for showing how to use RCE specific commands in python 
#
# = Using RCE variables =
# 	If the component has defined in- and outputs, e.g. an input "x" as double and an output "y" as double,
#	the python script is able to use them just as normal python variables:
#
#	 y = 2 * x
#
# 	With this code, all incoming input values for "x" from RCE are doubled and then put back into the workflow 
# 	as output "y". 
#
#
# = Using a Data Management file reference = 
#	When creating a file in a python script, you have to use the DM to send this file to another component.
#	For this, a File Reference output variable must be added, e.g. "outgoingFile". 
#	Creating and sending a file:
#   
#	filename = "outgoing.txt"
#	file = open(filename, "w")
#   file.write("Example file")
#   file.close()
#   _dm_["outgoingFile"] = filename  # This sends the file to the RCE output
#
#
#  For incoming files, the _dm_ command does not give the filename but the actual file:
#
#   file = _dm_["incomingFile"]
#
#

 
sys.stderr.write('Python script was not configured')