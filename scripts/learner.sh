#!/bin/bash

v1="dkproTheme.jar"
v2="themeOffice.jar"
v3="dkpro.ukp.topic.structure.jar"
input="input/file.xml"
output="output/file2.xml"
language="german"
modal="factored"
rule="Rules_DE.xml"

parameters="-learn $rule $input $output"

#java -jar themeOffice.jar $parameters

java -jar $v3 $parameters
