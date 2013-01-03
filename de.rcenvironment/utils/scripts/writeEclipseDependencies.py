#!/usr/bin/env python
# This script writes dependencies list for eclipse target

import os

file = open("dependencies.list", "w")
file.write("<dependencies>\n")
for f in os.listdir("."):
    if (f.endswith('.jar')):
        parts = f.split("_")
        file.write("    <dependency>\n")
        file.write("        <groupId>targetplatform</groupId>\n")
        file.write("        <artifactId>" + parts[0] + "</artifactId>\n")
        file.write("        <version>" + parts[1].split(".jar")[0] + "</version>\n")
        file.write("        <scope>system</scope>\n")
        file.write("        <systemPath>${tp.location}/eclipse36/plugins/" + f + "</systemPath>\n")
        file.write("    </dependency>\n")

file.write("</dependencies>\n")