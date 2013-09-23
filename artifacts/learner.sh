#!/bin/bash

v1="dkproTheme.jar"
v2="themeOffice.jar"
v3="dkpro.ukp.topic.structure.jar"
input="output/file.xml"
output="output/file2.xml"
language="english"
modal="factored"
rule="config/Rules_EN.xml"

parameters="-learn $rule $input $output"

#java -jar themeOffice.jar $parameters

java -jar $v3 $parameters
