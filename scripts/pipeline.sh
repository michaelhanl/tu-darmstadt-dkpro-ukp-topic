#!/bin/bash

## set working dir to main (pwd)?!
cd ..

## set runtime parameters 
## in "all" mode the rule file is automatically detected, if there are rule files in the "config"
## directory with the respective language extension ("EN" or "DE").
v3="all_intellij.jar"
input="input"
output="output"
modal="factored"
rule="config/Rules_EN.xml"

parameters="-all english -modal $modal -inDir $input -outDir $output -toFile"

java -jar $v3 $parameters


## java -cp annolab.jar org.annolab.tre.cli.Main Rules_EN.xml file.xml
## java -cp annolab.jar org.annolab.tre.cli.LearnMain Rules_EN.xml file.xml out.xml
